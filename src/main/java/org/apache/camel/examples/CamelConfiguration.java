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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.properties.PropertiesComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ContextName("camel-context")
public class CamelConfiguration extends RouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Inject
  @ContextName("camel-context")
  private CamelContext camelContext;
  
  @PostConstruct
  private void initializePropertyPlaceholder() {
    PropertiesComponent properties = camelContext.getComponent("properties", PropertiesComponent.class);

    List<String> propertyLocations = new ArrayList<>();
    propertyLocations.add("classpath:/application.properties;optional=true");
    propertyLocations.add("file:${user.home}/application.properties;optional=true");
    propertyLocations.add("file:${camel.config.location};optional=true");
    if (System.getProperty("camel.config.locations") != null) {
      for (String location : System.getProperty("camel.config.locations").split(",")) {
        propertyLocations.add("file:" + location + ";optional=true");
      }
    }
    propertyLocations.add("file:${env:CAMEL_CONFIG_LOCATION};optional=true");
    if (System.getenv("CAMEL_CONFIG_LOCATIONS") != null) {
      for (String location : System.getenv("CAMEL_CONFIG_LOCATIONS").split(",")) {
        propertyLocations.add("file:" + location + ";optional=true");
      }
    }
    properties.setLocations(propertyLocations);
    
    Properties overrideProperties = new Properties();
    overrideProperties.putAll(System.getenv());
    overrideProperties.putAll(System.getProperties());
    properties.setOverrideProperties(overrideProperties);
  }

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
    
    /*
    from("quartz2:audit/daily_notification?cron={{camel.quartz.cron}}?&stateful=true")
      .log(LoggingLevel.INFO, log, "Sending audit logs to notification service")
    ;
    */
  }
}
