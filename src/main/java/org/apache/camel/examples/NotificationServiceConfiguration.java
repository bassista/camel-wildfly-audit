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

import java.net.MalformedURLException;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import org.apache.camel.PropertyInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NotificationServiceConfiguration {

  private static final Logger log = LoggerFactory.getLogger(NotificationServiceConfiguration.class);

  @PropertyInject("notification.wsdl-url")
  private String notificationWsdlUrl;
  
  @ApplicationScoped
  @Produces
  @Named("notificationService")
  private NotificationService notificationService() throws MalformedURLException {
    Service service = Service.create(new URL(notificationWsdlUrl), new QName("http://camel.apache.org/examples", "NotificationService"));
    return service.getPort(NotificationService.class);
  }
}
