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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.EntityManager;
import org.apache.camel.component.jpa.JpaComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

@ApplicationScoped
public class JpaComponentConfiguration {

  private static final Logger log = LoggerFactory.getLogger(JpaComponentConfiguration.class);

  @Produces
  @Named("jpa")
  public JpaComponent jpaComponent(PlatformTransactionManager transactionManager, EntityManager entityManager) {
    JpaComponent component = new JpaComponent();
    component.setTransactionManager(transactionManager);
    component.setEntityManagerFactory(entityManager.getEntityManagerFactory());
    return component;
  }
}
