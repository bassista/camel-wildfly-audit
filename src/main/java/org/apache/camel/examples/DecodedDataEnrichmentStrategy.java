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

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class DecodedDataEnrichmentStrategy implements AggregationStrategy {

  @Override
  public Exchange aggregate(Exchange original, Exchange resource) {
    AuditLog originalBody = original.getIn().getBody(AuditLog.class);
    originalBody.setData(resource.getIn().getBody(String.class));
    return original;
  }
}
