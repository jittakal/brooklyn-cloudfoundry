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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.brooklyn.api.entity.EntitySpec;
import org.apache.brooklyn.cloudfoundry.AbstractCloudFoundryLiveTest;
import org.apache.brooklyn.cloudfoundry.location.CloudFoundryPaasLocation;
import org.apache.brooklyn.core.entity.lifecycle.Lifecycle;
import org.apache.brooklyn.test.Asserts;
import org.apache.brooklyn.util.exceptions.PropagatedRuntimeException;
import org.apache.brooklyn.util.text.Strings;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class CloudFoundryServiceLiveTest extends AbstractCloudFoundryLiveTest {

    @Test(groups = {"Live"})
    public void testCreateService() throws IOException {
        CloudFoundryService entity = addClearDbServiceToApp();
        startServiceInLocationAndCheckSensors(entity, cloudFoundryPaasLocation);
        assertTrue(Strings
                .isNonBlank(entity.getAttribute(CloudFoundryService.SERVICE_INSTANCE_NAME)));
    }

    @Test(groups = {"Live"})
    public void testCreateServiceWithName() throws IOException {
        CloudFoundryService entity = addClearDbServiceToApp(SERVICE_INSTANCE_NAME);
        startServiceInLocationAndCheckSensors(entity, cloudFoundryPaasLocation);

        assertEquals(entity.getAttribute(CloudFoundryService.SERVICE_INSTANCE_NAME),
                SERVICE_INSTANCE_NAME);
    }

    @Test(groups = {"Live"})
    public void testCreateRepeatedService() throws IOException {
        CloudFoundryService entity = addClearDbServiceToApp(SERVICE_INSTANCE_NAME);
        startServiceInLocationAndCheckSensors(entity, cloudFoundryPaasLocation);
        boolean errorCreating = false;
        try {
            CloudFoundryService sameService = addClearDbServiceToApp(SERVICE_INSTANCE_NAME);
            startServiceInLocationAndCheckSensors(sameService, cloudFoundryPaasLocation);
        } catch (PropagatedRuntimeException e) {
            errorCreating = true;
        }
        assertTrue(errorCreating);
    }

    @Test(groups = {"Live"}, expectedExceptions = PropagatedRuntimeException.class)
    public void testCreateInvalidService() throws IOException {
        CloudFoundryService invalidService = addInvalidServiceToApp();
        startServiceInLocationAndCheckSensors(invalidService, cloudFoundryPaasLocation);
    }

    @Test(groups = {"Live"}, expectedExceptions = PropagatedRuntimeException.class)
    public void testCreateUsingInvalidPlanService() throws IOException {
        CloudFoundryService invalidService = addServiceToAppWithInvalidPlan();
        startServiceInLocationAndCheckSensors(invalidService, cloudFoundryPaasLocation);
    }

    @Test(groups = {"Live"})
    public void testStopService() throws IOException {
        CloudFoundryService entity = addClearDbServiceToApp(SERVICE_INSTANCE_NAME);
        startServiceInLocationAndCheckSensors(entity, cloudFoundryPaasLocation);
        stopServiceAndCheckSensors(entity);
        assertFalse(cloudFoundryPaasLocation.serviceInstanceExist(SERVICE_INSTANCE_NAME));
    }

    private void stopServiceAndCheckSensors(CloudFoundryService service) {
        service.stop();
        assertNull(service.getAttribute(CloudFoundryService.SERVICE_UP));
        assertNull(service.getAttribute(CloudFoundryService.SERVICE_PROCESS_IS_RUNNING));
    }

    private void startServiceInLocationAndCheckSensors(CloudFoundryService entity,
                                                       CloudFoundryPaasLocation location) {
        entity.start(ImmutableList.of(location));
        Asserts.succeedsEventually(new Runnable() {
            public void run() {
                assertTrue(entity.getAttribute(CloudFoundryService.SERVICE_UP));
                assertTrue(entity.getAttribute(CloudFoundryService.SERVICE_PROCESS_IS_RUNNING));
                assertEquals(entity.getAttribute(CloudFoundryService.SERVICE_STATE_ACTUAL),
                        Lifecycle.RUNNING);
            }
        });
    }

    private CloudFoundryService addClearDbServiceToApp() {
        return addClearDbServiceToApp(Strings.EMPTY);
    }

    private CloudFoundryService addClearDbServiceToApp(String serviceInstanceName) {
        EntitySpec<CloudFoundryService> spec =
                getServiceEntitySpec(CLEARDB_SERVICE, CLEARDB_SPARK_PLAN);
        if (Strings.isNonBlank(serviceInstanceName)) {
            spec.configure(CloudFoundryService.SERVICE_INSTANCE_NAME,
                    serviceInstanceName);
        }
        return app.createAndManageChild(spec);
    }

    private CloudFoundryService addInvalidServiceToApp() {
        EntitySpec<CloudFoundryService> spec =
                getServiceEntitySpec(NON_EXISTENT_SERVICE, CLEARDB_SPARK_PLAN);
        return app.createAndManageChild(spec);
    }

    private CloudFoundryService addServiceToAppWithInvalidPlan() {
        EntitySpec<CloudFoundryService> spec =
                getServiceEntitySpec(NON_EXISTENT_SERVICE, CLEARDB_SPARK_PLAN);
        return app.createAndManageChild(spec);
    }

    private EntitySpec<CloudFoundryService> getServiceEntitySpec(String name, String plan) {
        return EntitySpec
                .create(CloudFoundryService.class)
                .configure(CloudFoundryService.SERVICE_NAME, name)
                .configure(CloudFoundryService.PLAN, plan);
    }

}