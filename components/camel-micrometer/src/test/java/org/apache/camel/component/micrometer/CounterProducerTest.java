/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.apache.camel.component.micrometer.MicrometerConstants.HEADER_COUNTER_DECREMENT;
import static org.apache.camel.component.micrometer.MicrometerConstants.HEADER_COUNTER_INCREMENT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CounterProducerTest {

    private static final String METRICS_NAME = "metrics.name";
    private static final Double INCREMENT = 100000D;
    private static final Double DECREMENT = 91929199D;

    @Mock
    private CamelContext camelContext;

    @Mock
    private MicrometerEndpoint endpoint;

    @Mock
    private Exchange exchange;

    @Mock
    private MeterRegistry registry;

    @Mock
    private Counter counter;

    @Mock
    private Message in;

    private CounterProducer producer;

    @Before
    public void setUp() {
        endpoint = mock(MicrometerEndpoint.class);
        producer = new CounterProducer(endpoint);
        when(endpoint.getRegistry()).thenReturn(registry);
        when(registry.counter(METRICS_NAME, Tags.empty())).thenReturn(counter);
        when(exchange.getIn()).thenReturn(in);
    }

    @Test
    public void testCounterProducer() {
        assertThat(producer.getEndpoint().equals(endpoint), is(true));
    }

    @Test
    public void testProcessWithIncrementOnly() {
        Object action = null;
        when(endpoint.getIncrement()).thenReturn(INCREMENT);
        when(endpoint.getDecrement()).thenReturn(null);
        when(in.getHeader(HEADER_COUNTER_INCREMENT, INCREMENT, Double.class)).thenReturn(INCREMENT);
        producer.doProcess(exchange, METRICS_NAME, Tags.empty());
    }

    @Test
    public void testProcessWithDecrementOnly() {
        Object action = null;
        when(endpoint.getIncrement()).thenReturn(null);
        when(endpoint.getDecrement()).thenReturn(DECREMENT);
        when(in.getHeader(HEADER_COUNTER_DECREMENT, DECREMENT, Double.class)).thenReturn(DECREMENT);
        producer.doProcess(exchange, METRICS_NAME, Tags.empty());
    }

    @Test
    public void testDoProcessWithIncrementAndDecrement() {
        when(endpoint.getIncrement()).thenReturn(INCREMENT);
        when(endpoint.getDecrement()).thenReturn(DECREMENT);
        when(in.getHeader(HEADER_COUNTER_INCREMENT, INCREMENT, Double.class)).thenReturn(INCREMENT);
        when(in.getHeader(HEADER_COUNTER_DECREMENT, DECREMENT, Double.class)).thenReturn(DECREMENT);

        producer.doProcess(exchange, METRICS_NAME, Tags.empty());
    }

    @Test
    public void testProcessWithOutIncrementAndDecrement() {
        Object action = null;
        when(endpoint.getIncrement()).thenReturn(null);
        when(endpoint.getDecrement()).thenReturn(null);
        producer.doProcess(exchange, METRICS_NAME, Tags.empty());
    }

    @Test
    public void testProcessOverridingIncrement() {
        when(endpoint.getIncrement()).thenReturn(INCREMENT);
        when(endpoint.getDecrement()).thenReturn(DECREMENT);
        when(in.getHeader(HEADER_COUNTER_INCREMENT, INCREMENT, Double.class)).thenReturn(INCREMENT + 1);
        when(in.getHeader(HEADER_COUNTER_DECREMENT, DECREMENT, Double.class)).thenReturn(DECREMENT);
        producer.doProcess(exchange, METRICS_NAME, Tags.empty());
    }

    @Test
    public void testProcessOverridingDecrement() {
        Object action = null;
        when(endpoint.getIncrement()).thenReturn(null);
        when(endpoint.getDecrement()).thenReturn(DECREMENT);
        when(in.getHeader(HEADER_COUNTER_DECREMENT, DECREMENT, Double.class)).thenReturn(DECREMENT - 1);
        producer.doProcess(exchange, METRICS_NAME, Tags.empty());
    }
}
