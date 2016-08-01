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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.apache.brooklyn.api.entity.EntitySpec;
import org.apache.brooklyn.api.location.Location;
import org.apache.brooklyn.cloudfoundry.AbstractCloudFoundryUnitTest;
import org.apache.brooklyn.cloudfoundry.location.CloudFoundryPaasLocation;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.entity.trait.Startable;
import org.apache.brooklyn.test.Asserts;
import org.apache.brooklyn.util.collections.MutableMap;
import org.apache.brooklyn.util.exceptions.PropagatedRuntimeException;
import org.cloudfoundry.client.lib.StartingInfo;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class VanillaCloudFoundryApplicationTest extends AbstractCloudFoundryUnitTest {

    private static final String MOCKED_APP_PATH = "vanilla-cf-app-test";

    private MockWebServer mockWebServer;
    private HttpUrl serverUrl;
    private CloudFoundryPaasLocation location;
    private String serverAddress;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        location = spy(cloudFoundryPaasLocation);

        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(getGenericDispatcher());
        serverUrl = mockWebServer.url(MOCKED_APP_PATH);
        serverAddress = serverUrl.url().toString();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        super.tearDown();
        mockWebServer.shutdown();
    }

    @Test
    public void testDeployApplication() throws IOException {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(EMPTY_ENV).when(location).getEnv(anyString());
        doNothing().when(location).setEnv(anyString(), anyMapOf(String.class, String.class));

        VanillaCloudFoundryApplication entity =
                addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);
        assertTrue(entity.getAttribute(VanillaCloudFoundryApplication.ENV).isEmpty());
        verify(location, never()).setEnv(APPLICATION_NAME, EMPTY_ENV);
        checkDefaultResourceProfile(entity);
    }

    @Test
    public void testDeployApplicationWithEnv() throws IOException {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(SIMPLE_ENV).when(location).getEnv(anyString());
        doNothing().when(location).setEnv(anyString(), anyMapOf(String.class, String.class));

        VanillaCloudFoundryApplication entity =
                addDefaultVanillaToAppAndMockProfileMethods(location, MutableMap.copyOf(SIMPLE_ENV));
        startEntityInLocationAndCheckSensors(entity, location);
        assertEquals(location.getEnv(APPLICATION_NAME), SIMPLE_ENV);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), SIMPLE_ENV);
        verify(location, times(1)).setEnv(APPLICATION_NAME, SIMPLE_ENV);
    }

    @Test(expectedExceptions = PropagatedRuntimeException.class)
    public void testDeployApplicationWithoutLocation() throws IOException {
        final VanillaCloudFoundryApplication entity = addDefaultVanillaEntityChildToApp();
        entity.start(ImmutableList.<Location>of());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetEnvEffector() throws IOException {
        Map<String, String> env = MutableMap.copyOf(EMPTY_ENV);
        CloudFoundryPaasLocation location = spy(cloudFoundryPaasLocation);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(env).when(location).getEnv(anyString());
        doNothing().when(location).setEnv(anyString(), anyMapOf(String.class, String.class));

        VanillaCloudFoundryApplication entity = addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);
        assertTrue(entity.getAttribute(VanillaCloudFoundryApplication.ENV).isEmpty());
        entity.setEnv("k1", "v1");
        env.put("k1", "v1");
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), env);
        entity.setEnv("k2", "v2");
        env.put("k2", "v2");
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), env);
    }

    @Test
    public void testSetMemory() {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(EMPTY_ENV).when(location).getEnv(anyString());

        VanillaCloudFoundryApplication entity =
                addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);
        checkDefaultResourceProfile(entity);
        doReturn(CUSTOM_MEMORY).when(location).getMemory(anyString());

        entity.setMemory(CUSTOM_MEMORY);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ALLOCATED_MEMORY).intValue(), CUSTOM_MEMORY);
        verify(location, times(1)).setMemory(APPLICATION_NAME, CUSTOM_MEMORY);
    }

    @Test
    public void testSetDisk() {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(EMPTY_ENV).when(location).getEnv(anyString());

        VanillaCloudFoundryApplication entity =
                addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);
        checkDefaultResourceProfile(entity);
        doReturn(CUSTOM_DISK).when(location).getDiskQuota(anyString());

        entity.setDiskQuota(CUSTOM_DISK);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ALLOCATED_DISK).intValue(),
                CUSTOM_DISK);
        verify(location, times(1)).setDiskQuota(APPLICATION_NAME, CUSTOM_DISK);
    }

    @Test
    public void testSetInstances() {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(EMPTY_ENV).when(location).getEnv(anyString());

        doNothing().when(location).setInstancesNumber(anyString(), anyInt());

        VanillaCloudFoundryApplication entity = addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);
        checkDefaultResourceProfile(entity);
        doReturn(CUSTOM_INSTANCES).when(location).getInstancesNumber(anyString());

        entity.setInstancesNumber(CUSTOM_INSTANCES);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.INSTANCES).intValue(),
                CUSTOM_INSTANCES);
        verify(location, times(1)).setInstancesNumber(APPLICATION_NAME, CUSTOM_INSTANCES);
    }

    @Test
    public void testStopApplication() {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doNothing().when(location).stopApplication(anyString());
        doNothing().when(location).deleteApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(EMPTY_ENV).when(location).getEnv(anyString());

        final VanillaCloudFoundryApplication entity =
                addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);

        entity.stop();
        Asserts.succeedsEventually(new Runnable() {
            public void run() {
                assertNull(entity.getAttribute(Startable.SERVICE_UP));
                assertNull(entity.getAttribute(VanillaCloudFoundryApplication
                        .SERVICE_PROCESS_IS_RUNNING));
            }
        });
    }

    @Test
    public void testRestartApplication() {
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doNothing().when(location).restartApplication(anyString());
        doReturn(serverAddress).when(location).deploy(anyMap());
        doReturn(EMPTY_ENV).when(location).getEnv(anyString());

        final VanillaCloudFoundryApplication entity =
                addDefaultVanillaToAppAndMockProfileMethods(location);
        startEntityInLocationAndCheckSensors(entity, location);

        entity.restart();
        verify(location, times(1)).restartApplication(APPLICATION_NAME);
    }

    private VanillaCloudFoundryApplication addDefaultVanillaToAppAndMockProfileMethods(
            CloudFoundryPaasLocation location) {
        return addDefaultVanillaToAppAndMockProfileMethods(location, null);
    }

    private VanillaCloudFoundryApplication addDefaultVanillaToAppAndMockProfileMethods(
            CloudFoundryPaasLocation location, Map<String, String> env) {
        VanillaCloudFoundryApplication entity = addDefaultVanillaEntityChildToApp(env);
        mockLocationProfileUsingEntityConfig(location, entity);
        return entity;
    }

    private VanillaCloudFoundryApplication addDefaultVanillaEntityChildToApp() {
        return addDefaultVanillaEntityChildToApp(null);
    }

    private VanillaCloudFoundryApplication addDefaultVanillaEntityChildToApp(Map<String, String> env) {
        EntitySpec<VanillaCloudFoundryApplication> vanilla = EntitySpec
                .create(VanillaCloudFoundryApplication.class)
                .configure(VanillaCloudFoundryApplication.APPLICATION_NAME, APPLICATION_NAME)
                .configure(VanillaCloudFoundryApplication.ARTIFACT_PATH, APPLICATION_ARTIFACT_URL)
                .configure(VanillaCloudFoundryApplication.APPLICATION_DOMAIN, BROOKLYN_DOMAIN)
                .configure(VanillaCloudFoundryApplication.BUILDPACK, MOCK_BUILDPACK);
        if (env != null) {
            vanilla.configure(VanillaCloudFoundryApplication.ENV, env);
        }
        return app.createAndManageChild(vanilla);
    }

    private void mockLocationProfileUsingEntityConfig(CloudFoundryPaasLocation location,
                                                      VanillaCloudFoundryApplication entity) {
        if (new MockUtil().isMock(location)) {
            doNothing().when(location).setMemory(anyString(), anyInt());
            doNothing().when(location).setDiskQuota(anyString(), anyInt());
            doNothing().when(location).setInstancesNumber(anyString(), anyInt());

            doReturn(entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_MEMORY))
                    .when(location).getMemory(anyString());
            doReturn(entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_DISK))
                    .when(location).getDiskQuota(anyString());
            doReturn(entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_INSTANCES))
                    .when(location).getInstancesNumber(anyString());
        }
    }

    private void startEntityInLocationAndCheckSensors(final VanillaCloudFoundryApplication entity,
                                                      CloudFoundryPaasLocation location) {
        entity.start(ImmutableList.of(location));
        Asserts.succeedsEventually(new Runnable() {
            public void run() {
                assertTrue(entity.getAttribute(Startable.SERVICE_UP));
                assertTrue(entity.getAttribute(VanillaCloudFoundryApplication
                        .SERVICE_PROCESS_IS_RUNNING));
            }
        });
        assertEquals(entity.getAttribute(Attributes.MAIN_URI).toString(), serverAddress);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL), serverAddress);
    }

    private Dispatcher getGenericDispatcher() {
        return new Dispatcher() {
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/" + MOCKED_APP_PATH)) {
                    return new MockResponse().setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
    }

}
