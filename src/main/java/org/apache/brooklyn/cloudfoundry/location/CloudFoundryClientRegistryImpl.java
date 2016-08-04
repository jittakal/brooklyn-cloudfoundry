/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.brooklyn.cloudfoundry.location;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.brooklyn.cloudfoundry.location.paas.PaasLocationConfig;
import org.apache.brooklyn.util.core.config.ConfigBag;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;

public class CloudFoundryClientRegistryImpl implements CloudFoundryClientRegistry {

    public static final CloudFoundryClientRegistryImpl INSTANCE = new CloudFoundryClientRegistryImpl();

    protected CloudFoundryClientRegistryImpl() {
    }

    @Override
    public CloudFoundryOperations getCloudFoundryClient(ConfigBag conf, boolean allowReuse) {
        String username = checkNotNull(conf.get(PaasLocationConfig.ACCESS_IDENTITY), "identity must not be null");
        String password = checkNotNull(conf.get(PaasLocationConfig.ACCESS_CREDENTIAL), "credential must not be null");
        String apiHost = checkNotNull(conf.get(PaasLocationConfig.CLOUD_ENDPOINT), "endpoint must not be null");
        String organization = checkNotNull(conf.get(CloudFoundryPaasLocationConfig.CF_ORG), "organization must not be null");
        String space = checkNotNull(conf.get(CloudFoundryPaasLocationConfig.CF_SPACE), "space must not be null");

        DefaultConnectionContext connectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();
        //TODO: using singleton for the class instance
        PasswordGrantTokenProvider tokenProvider = PasswordGrantTokenProvider.builder()
                .username(username)
                .password(password)
                .build();

        //TODO: using singleton for the class instance
        ReactorCloudFoundryClient client = ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();

        return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(client)
                .organization(organization)
                .space(space)
                .build();
    }

}
