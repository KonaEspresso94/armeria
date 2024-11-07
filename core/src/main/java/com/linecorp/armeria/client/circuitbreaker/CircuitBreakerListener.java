/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.client.circuitbreaker;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * The listener interface for receiving {@link CircuitBreaker} events.
 */
public interface CircuitBreakerListener {

    /**
     * Returns a new {@link CircuitBreakerListener} that collects metric with the specified
     * {@link MeterRegistry}. The returned listener will generate the following metrics:
     * <table>
     * <caption>metrics that will be generated by this class</caption>
     * <tr>
     *   <th>metric name</th>
     *   <th>description</th>
     * </tr>
     * <tr>
     *   <td>
     *     {@code armeria.client.circuit.breaker.requests{name="<circuit breaker name>",result="success"}}
     *   </td>
     *   <td>The number of successful requests in the counter time window.</td></tr>
     * <tr>
     *   <td>
     *     {@code armeria.client.circuit.breaker.requests{name="<circuit breaker name>",result="failure"}}
     *   </td>
     *   <td>The number of failed requests in the counter time window.</td></tr>
     * <tr>
     *   <td>
     *     {@code armeria.client.circuit.breaker.transitions{name="<circuit breaker name>",state="CLOSED"}}
     *   </td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#CLOSED}.</td></tr>
     * <tr>
     *   <td>
     *     {@code armeria.client.circuit.breaker.transitions{name="<circuit breaker name>",state="OPEN"}}
     *   </td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#OPEN}.</td>
     * </tr>
     * <tr>
     *   <td>
     *     {@code armeria.client.circuit.breaker.transitions{name="<circuit breaker name>",state="HALF_OPEN"}}
     *   </td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#HALF_OPEN}.</td>
     * </tr>
     * <tr>
     *   <td>
     *     {@code armeria.client.circuit.breaker.transitions{name="<circuit breaker name>",state="FORCED_OPEN"}}
     *   </td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#FORCED_OPEN}.</td>
     * </tr>
     * <tr>
     *   <td>{@code armeria.client.circuit.breaker.rejected.requests{name="<circuit breaker name>"}}</td>
     *   <td>The number of requests rejected by the circuit breaker.</td>
     * </tr>
     * </table>
     */
    static CircuitBreakerListener metricCollecting(MeterRegistry registry) {
        return metricCollecting(registry, "armeria.client.circuit.breaker");
    }

    /**
     * Returns a new {@link CircuitBreakerListener} that collects metric with the specified
     * {@link MeterRegistry} and {@link Meter} name. The returned listener will generate the following metrics:
     * <table>
     * <caption>metrics that will be generated by this class</caption>
     * <tr>
     *   <th>metric name</th>
     *   <th>description</th>
     * </tr>
     * <tr>
     *   <td>{@code <name>.requests{name="<circuit breaker name>",result="success"}}</td>
     *   <td>The number of successful requests in the counter time window.</td>
     * </tr>
     * <tr>
     *   <td>{@code <name>.requests{name="<circuit breaker name>",result="failure"}}</td>
     *   <td>The number of failed requests in the counter time window.</td>
     * </tr>
     * <tr>
     *   <td>{@code <name>.transitions{name="<circuit breaker name>",state="CLOSED"}}</td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#CLOSED}.</td>
     * </tr>
     * <tr>
     *   <td>{@code <name>.transitions{name="<circuit breaker name>",state="OPEN"}}</td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#OPEN}.</td>
     * </tr>
     * <tr>
     *   <td>{@code <name>.transitions{name="<circuit breaker name>",state="HALF_OPEN"}}</td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#HALF_OPEN}.</td>
     * </tr>
     * <tr>
     *   <td>{@code <name>.transitions{name="<circuit breaker name>",state="FORCED_OPEN"}}</td>
     *   <td>The number of circuit breaker state transitions to {@link CircuitState#FORCED_OPEN}.</td>
     * </tr>
     * <tr>
     *   <td>{@code <name>.rejected.requests{name="<circuit breaker name>"}}</td>
     *   <td>The number of requests rejected by the circuit breaker.</td>
     * </tr>
     * </table>
     */
    static CircuitBreakerListener metricCollecting(MeterRegistry registry, String name) {
        return new MetricCollectingCircuitBreakerListener(registry, name);
    }

    /**
     * Invoked when the circuit breaker is initialized.
     */
    default void onInitialized(String circuitBreakerName, CircuitState initialState) throws Exception {
        onStateChanged(circuitBreakerName, initialState);
    }

    /**
     * Invoked when the circuit state is changed.
     */
    void onStateChanged(String circuitBreakerName, CircuitState state) throws Exception;

    /**
     * Invoked when the circuit breaker's internal {@link EventCount} is updated.
     *
     * @deprecated Use {@link #onEventCountUpdated(String, com.linecorp.armeria.common.util.EventCount)}
     *             instead.
     */
    @Deprecated
    void onEventCountUpdated(String circuitBreakerName, EventCount eventCount) throws Exception;

    /**
     * Invoked when the circuit breaker's internal {@link com.linecorp.armeria.common.util.EventCount} is
     * updated.
     */
    default void onEventCountUpdated(String circuitBreakerName,
                                     com.linecorp.armeria.common.util.EventCount eventCount) throws Exception {}

    /**
     * Invoked when the circuit breaker rejects a request.
     */
    void onRequestRejected(String circuitBreakerName) throws Exception;
}
