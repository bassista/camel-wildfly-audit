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

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ContextName("camel-context")
public class CamelConfiguration extends RouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Inject
  @ContextName("camel-context")
  private CamelContext camelContext;
  
  @Override
  public void configure() throws Exception {
    
    from("direct:postAudit")
      .onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.INFO, log, "Error processing audit log: [${exception.message}]")
        .setBody().groovy("javax.ws.rs.core.Response.status(500).entity(null).build()")
      .end()
      .log(LoggingLevel.INFO, log, "Processing audit log: [${body}]")
      .transacted("PROPAGATION_REQUIRED")
      .enrich("direct:decodeDataField", new DecodedDataEnrichmentStrategy())
      .to("jpa:org.apache.camel.examples.AuditLog")
      .setBody().groovy("javax.ws.rs.core.Response.status(200).entity(null).build()")
    ;
    
    from("direct:decodeDataField")
      .setBody().simple("${body.data}")
      .unmarshal().base64()
    ;
    
    from("quartz2:audit/daily_notification?cron={{camel.quartz.cron}}&stateful=true")
      .toF("jpa:org.apache.camel.examples.AuditLog?query=select o from org.apache.camel.examples.AuditLog o where o.createdTime between '%s' and '%s'", Instant.now().truncatedTo(ChronoUnit.DAYS), Instant.now().truncatedTo(ChronoUnit.DAYS).plus(Period.ofDays(1)))
      .log(LoggingLevel.INFO, log, "Sending [${body.size()}] audit logs to notification service")
      .marshal().json(JsonLibrary.Jackson)
      .convertBodyTo(String.class)
      .setHeader("NotificationRecipients").simple("${properties:notification.recipients}")
      .setBody().groovy("new org.apache.camel.examples.NotificationRequest('recipients': request.headers['NotificationRecipients'].split(','), 'notification': request.body)")
      .to("bean:notificationService?method=sendNotification(${body})")
    ;
  }
}
