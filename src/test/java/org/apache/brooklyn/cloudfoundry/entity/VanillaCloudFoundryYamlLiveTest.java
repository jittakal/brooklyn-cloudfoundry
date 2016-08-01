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
package org.apache.brooklyn.cloudfoundry.entity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.apache.brooklyn.api.entity.Application;
import org.apache.brooklyn.api.entity.Entity;
import org.apache.brooklyn.camp.brooklyn.BrooklynCampConstants;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.entity.trait.Startable;
import org.apache.brooklyn.launcher.camp.SimpleYamlLauncher;
import org.apache.brooklyn.test.Asserts;
import org.apache.brooklyn.util.collections.MutableMap;
import org.testng.annotations.Test;

public class VanillaCloudFoundryYamlLiveTest {

    @Test(groups = {"Live"})
    public void deployWebappFromYaml() {
        SimpleYamlLauncher launcher = new SimpleYamlLauncher();
        launcher.setShutdownAppsOnExit(true);
        Application app = launcher.launchAppYaml("vanilla-cf-stadalone.yml").getApplication();

        final VanillaCloudFoundryApplication entity = (VanillaCloudFoundryApplication)
                findChildEntitySpecByPlanId(app, "vanilla-app");

        Asserts.succeedsEventually(new Runnable() {
            public void run() {
                assertTrue(entity.getAttribute(Startable.SERVICE_UP));
                assertTrue(entity.getAttribute(VanillaCloudFoundryApplication
                        .SERVICE_PROCESS_IS_RUNNING));

                assertTrue(entity.getAttribute(Startable.SERVICE_UP));
                assertNotNull(entity.getAttribute(Attributes.MAIN_URI).toString());
                assertNotNull(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL));
            }
        });
    }

    @Test(groups = {"Live"})
    @SuppressWarnings("unchecked")
    public void deployWebappWithEnvFromYaml() {
        SimpleYamlLauncher launcher = new SimpleYamlLauncher();
        launcher.setShutdownAppsOnExit(true);
        Application app = launcher.launchAppYaml("vanilla-cf-env.yml").getApplication();

        final VanillaCloudFoundryApplication entity = (VanillaCloudFoundryApplication)
                findChildEntitySpecByPlanId(app, "vanilla-app");

        Asserts.succeedsEventually(new Runnable() {
            public void run() {
                assertTrue(entity.getAttribute(Startable.SERVICE_UP));
                assertTrue(entity.getAttribute(VanillaCloudFoundryApplication
                        .SERVICE_PROCESS_IS_RUNNING));

                assertTrue(entity.getAttribute(Startable.SERVICE_UP));
                assertNotNull(entity.getAttribute(Attributes.MAIN_URI).toString());
                assertNotNull(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL));
            }
        });
        Map<String, String> env = (Map<String, String>)
                entity.getAttribute(VanillaCloudFoundryApplication.ENV);
        assertEquals(env, MutableMap.of("env1", "value1", "env2", "2", "env3", "value3"));
    }

    private Entity findChildEntitySpecByPlanId(Application app, String planId) {
        for (Entity child : app.getChildren()) {
            String childPlanId = child.getConfig(BrooklynCampConstants.PLAN_ID);
            if ((childPlanId != null) && (childPlanId.equals(planId))) {
                return child;
            }
        }
        return null;
    }

}
