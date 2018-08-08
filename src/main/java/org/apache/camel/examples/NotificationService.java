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

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(name = "NotificationService",
            serviceName = "NotificationService",
            portName = "NotificationServiceSoapBinding",
            targetNamespace = "http://camel.apache.org/examples")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface NotificationService {
  
  public void sendNotification(@WebParam(name = "notificationRequest") NotificationRequest notificationRequest);
}
