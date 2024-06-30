/*
 * Copyright 2024 LY Corporation

 * LY Corporation licenses this file to you under the Apache License,
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
package com.linecorp.armeria.common.nacos;

import java.util.regex.Pattern;

import com.linecorp.armeria.common.annotation.UnstableApi;

/**
 * Sets properties for building a Nacos client.
 */
@UnstableApi
public interface NacosConfigSetters<SELF extends NacosConfigSetters<SELF>> {
    String DEFAULT_NACOS_API_VERSION = "v2";
    Pattern NACOS_API_VERSION_PATTERN = Pattern.compile("^v[0-9][-._a-zA-Z0-9]*$");

    /**
     * Sets the namespace ID to query or register instances.
     *
     * @param namespaceId the namespace ID.
     */
    SELF namespaceId(String namespaceId);

    /**
     * Sets the group name to query or register instances.
     *
     * @param groupName the group name.
     */
    SELF groupName(String groupName);

    /**
     * Sets the cluster name to query or register instances.
     *
     * @param clusterName the cluster name.
     */
    SELF clusterName(String clusterName);

    /**
     * Sets the app name to query or register instances.
     *
     * @param app app name.
     */
    SELF app(String app);

    /**
     * Sets the specified Nacos's API version.
     * @param nacosApiVersion the version of Nacos API service, default: {@value
     *                         NacosConfigSetters#DEFAULT_NACOS_API_VERSION}
     */
    SELF nacosApiVersion(String nacosApiVersion);

    /**
     * Sets the username and password pair for Nacos's API.
     * Please refer to the
     * <a href=https://nacos.io/en-us/docs/v2/guide/user/auth.html>Nacos Authentication Document</a>
     * for more details.
     *
     * @param username the username for access Nacos API, default: {@code null}
     * @param password the password for access Nacos API, default: {@code null}
     */
    SELF authorization(String username, String password);
}
