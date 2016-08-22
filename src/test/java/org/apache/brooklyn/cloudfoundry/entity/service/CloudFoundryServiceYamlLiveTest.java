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
package org.apache.brooklyn.cloudfoundry.entity.service;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.apache.brooklyn.api.entity.Application;
import org.apache.brooklyn.cloudfoundry.AbstractCloudFoundryYamlLiveTest;
import org.apache.brooklyn.util.text.Strings;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class CloudFoundryServiceYamlLiveTest extends AbstractCloudFoundryYamlLiveTest {

    @AfterMethod
    public void tearDown() {
        launcher.destroyAll();
    }

    @Test(groups = {"Live"})
    public void deploySimpleClearDbService() {
        Application app = launcher
                .launchAppYaml("cf-service-standalone.yml")
                .getApplication();

        CloudFoundryService service = (CloudFoundryService)
                findChildEntitySpecByPlanId(app, DEFAULT_SERVICE_ID);
        checkRunningSensors(service);
        assertTrue(Strings
                .isNonBlank(service.getAttribute(CloudFoundryService.SERVICE_INSTANCE_NAME)));
    }

    @Test(groups = {"Live"})
    public void deploySimpleClearDbServiceWithInstanceName() {
        Application app = launcher
                .launchAppYaml("cf-service-with-name.yml")
                .getApplication();

        CloudFoundryService service = (CloudFoundryService)
                findChildEntitySpecByPlanId(app, DEFAULT_SERVICE_ID);
        checkRunningSensors(service);
        assertEquals(service.getAttribute(CloudFoundryService.SERVICE_INSTANCE_NAME),
                MY_CLEARDB_INSTANCE);
    }

}
