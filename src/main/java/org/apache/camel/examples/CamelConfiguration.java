/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.examples;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CamelConfiguration extends RouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);
  
  @Autowired
  private ApiProperties apiProperties;

  @Override
  public void configure() throws Exception {
    
    fromF("cxfrs:%s?bindingStyle=SimpleConsumer&resourceClasses=org.apache.camel.examples.AuditService&features=#cxfFeatures&providers=#cxfProviders", apiProperties.getBasePath())
      .description("auditService", "Audit Service API", null)
      .routingSlip(simple("direct:${headers[operationName]}"))
    ;
    
    from("direct:postAudit")
      .onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.INFO, log, "Error processing audit log: [${exception.message}]")
        .setBody().groovy("javax.ws.rs.core.Response.status(500).entity(null).build()")
      .end()
      .log(LoggingLevel.INFO, log, "Processing audit log: [${body}]")
      .filter().simple("${body.type} in 'FAILED_BATCH,FAILED_ITEM'")
        .wireTap("direct:notify").end()
      .end()
      .to("jpa:org.apache.camel.examples.AuditLog?entityManagerFactory=#entityManagerFactory&transactionManager=#transactionManager")
      .setBody().groovy("javax.ws.rs.core.Response.status(200).entity(null).build()")
    ;
    
    from("direct:notify")
      .log(LoggingLevel.INFO, log, "Sending notification: [${body}]")
      .marshal().json(JsonLibrary.Jackson)
      .to("jms:topic:notification?connectionFactory=#pooledJmsConnectionFactory")
    ;
  }
}
