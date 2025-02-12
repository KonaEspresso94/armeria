/*
 * Copyright 2023 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.xds;

import static com.linecorp.armeria.xds.XdsType.ROUTE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import com.linecorp.armeria.common.annotation.Nullable;

import io.envoyproxy.envoy.config.core.v3.ConfigSource;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.Route.ActionCase;
import io.envoyproxy.envoy.config.route.v3.RouteAction;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.grpc.Status;

final class RouteResourceNode extends AbstractResourceNode<RouteSnapshot> {

    private final List<ClusterSnapshot> clusterSnapshotList = new ArrayList<>();

    private final Set<Integer> pending = new HashSet<>();
    private final ClusterSnapshotWatcher snapshotWatcher = new ClusterSnapshotWatcher();

    RouteResourceNode(@Nullable ConfigSource configSource, String resourceName,
                      XdsBootstrapImpl xdsBootstrap, @Nullable ResourceHolder primer,
                      SnapshotWatcher<RouteSnapshot> parentWatcher, ResourceNodeType resourceNodeType) {
        super(xdsBootstrap, configSource, ROUTE, resourceName, primer, parentWatcher, resourceNodeType);
    }

    @Override
    public void doOnChanged(ResourceHolder update) {
        final RouteResourceHolder holder = (RouteResourceHolder) update;
        clusterSnapshotList.clear();
        pending.clear();
        final RouteConfiguration routeConfiguration = holder.resource();
        int index = 0;
        for (VirtualHost virtualHost: routeConfiguration.getVirtualHostsList()) {
            for (Route route: virtualHost.getRoutesList()) {
                if (route.getActionCase() != ActionCase.ROUTE) {
                    continue;
                }
                final RouteAction routeAction = route.getRoute();
                final String cluster = routeAction.getCluster();

                // add a dummy element to the index list so that we can call List.set later
                // without incurring an IndexOutOfBoundException when a snapshot is updated
                clusterSnapshotList.add(null);
                pending.add(index);
                final ClusterResourceNode node =
                        new ClusterResourceNode(null, cluster, xdsBootstrap(),
                                                holder, snapshotWatcher, virtualHost, route,
                                                index++, ResourceNodeType.DYNAMIC);
                children().add(node);
                xdsBootstrap().subscribe(node);
            }
        }
        if (children().isEmpty()) {
            parentWatcher().snapshotUpdated(new RouteSnapshot(holder, Collections.emptyList()));
        }
    }

    @Override
    public RouteResourceHolder currentResourceHolder() {
        return (RouteResourceHolder) super.currentResourceHolder();
    }

    private class ClusterSnapshotWatcher implements SnapshotWatcher<ClusterSnapshot> {

        @Override
        public void snapshotUpdated(ClusterSnapshot newSnapshot) {
            final RouteResourceHolder current = currentResourceHolder();
            if (current == null) {
                return;
            }
            if (!Objects.equals(current, newSnapshot.holder().primer())) {
                return;
            }
            clusterSnapshotList.set(newSnapshot.index(), newSnapshot);
            pending.remove(newSnapshot.index());
            // checks if all clusters for the route have reported a snapshot
            if (!pending.isEmpty()) {
                return;
            }
            parentWatcher().snapshotUpdated(
                    new RouteSnapshot(current, ImmutableList.copyOf(clusterSnapshotList)));
        }

        @Override
        public void onError(XdsType type, Status status) {
            parentWatcher().onError(type, status);
        }

        @Override
        public void onMissing(XdsType type, String resourceName) {
            parentWatcher().onMissing(type, resourceName);
        }
    }
}
