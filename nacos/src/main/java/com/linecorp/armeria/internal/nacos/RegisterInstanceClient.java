/*
 * Copyright 2024 LINE Corporation
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
package com.linecorp.armeria.internal.nacos;

import static java.util.Objects.requireNonNull;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.annotation.Nullable;

/**
 * A Nacos client that is responsible for
 * <a href="https://nacos.io/en-us/docs/v2/guide/user/open-api.html">Nacos Open-Api - Register instance</a>.
 */
final class RegisterInstanceClient {

    static RegisterInstanceClient of(NacosClient nacosClient, @Nullable LoginClient loginClient,
                                     String nacosApiVersion) {
        return new RegisterInstanceClient(nacosClient, loginClient, nacosApiVersion);
    }

    private final WebClient webClient;

    @Nullable
    private final LoginClient loginClient;

    private final String instanceApiPath;

    RegisterInstanceClient(NacosClient nacosClient, @Nullable LoginClient loginClient,
                           String nacosApiVersion) {
        webClient = nacosClient.nacosWebClient();
        this.loginClient = loginClient;
        instanceApiPath = '/' + nacosApiVersion + "/ns/instance";
    }

    /**
     * Registers a service into the Nacos.
     */
    HttpResponse register(String serviceName, String ip, int port, int weight, @Nullable String namespaceId,
                          @Nullable String groupName, @Nullable String clusterName, @Nullable String app) {
        requireNonNull(serviceName, "serviceName");
        requireNonNull(ip, "ip");

        final String params = NacosClientUtil.queryParams(namespaceId, groupName, serviceName, clusterName,
                                                           null, app, ip, port, weight)
                                              .toQueryString();

        if (loginClient == null) {
            return webClient.prepare()
                            .post(instanceApiPath)
                            .content(MediaType.FORM_DATA, params)
                            .execute();
        } else {
            return HttpResponse.of(
                    loginClient.login()
                               .thenApply(accessToken -> {
                                   final String paramsWithToken = params + "&accessToken=" + accessToken;
                                   return webClient.prepare()
                                            .post(instanceApiPath)
                                            .content(MediaType.FORM_DATA, paramsWithToken)
                                            .execute();
                               })
            );
        }
    }

    /**
     * De-registers a service from the Nacos.
     */
    HttpResponse deregister(String serviceName, String ip, int port, int weight, @Nullable String namespaceId,
                            @Nullable String groupName, @Nullable String clusterName, @Nullable String app) {
        requireNonNull(serviceName, "serviceName");
        requireNonNull(ip, "ip");

        final String params = NacosClientUtil.queryParams(namespaceId, groupName, serviceName, clusterName,
                                                           null, app, ip, port, weight)
                                              .toQueryString();

        if (loginClient == null) {
            return webClient.prepare()
                            .delete(instanceApiPath)
                            .content(MediaType.FORM_DATA, params)
                            .execute();
        } else {
            return HttpResponse.of(
                    loginClient.login()
                               .thenApply(accessToken -> {
                                   final String paramsWithToken = params + "&accessToken=" + accessToken;
                                   return webClient.prepare()
                                            .delete(instanceApiPath)
                                            .content(MediaType.FORM_DATA, paramsWithToken)
                                            .execute();
                               })
            );
        }
    }
}
