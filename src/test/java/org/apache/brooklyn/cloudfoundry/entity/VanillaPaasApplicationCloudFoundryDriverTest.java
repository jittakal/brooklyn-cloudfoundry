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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.brooklyn.cloudfoundry.AbstractCloudFoundryUnitTest;
import org.apache.brooklyn.cloudfoundry.location.CloudFoundryPaasLocation;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.util.collections.MutableMap;
import org.cloudfoundry.client.lib.StartingInfo;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

public class VanillaPaasApplicationCloudFoundryDriverTest extends AbstractCloudFoundryUnitTest {
    CloudFoundryPaasLocation location;

    private MockWebServer mockWebServer;
    private HttpUrl serverUrl;
    private String applicationUrl;

    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        location = mock(CloudFoundryPaasLocation.class);
        mockWebServer = new MockWebServer();
        serverUrl = mockWebServer.url("/");
        applicationUrl = serverUrl.url().toString();
        mockWebServer.setDispatcher(getGenericDispatcher());
    }

    @Test
    public void testStartApplication() throws MalformedURLException {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(EMPTY_ENV);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);
        mockLocationProfileUsingEntityConfig(location, entity);

        assertNull(entity.getAttribute(Attributes.MAIN_URI));
        assertNull(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL));

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertEquals(entity.getAttribute(Attributes.MAIN_URI), serverUrl.uri());
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL), applicationUrl);
        assertTrue(driver.isRunning());
        checkDefaultResourceProfile(entity);
    }

    @Test
    public void testStartApplicationWithEnv() {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(SIMPLE_ENV);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setConfigEvenIfOwned(VanillaCloudFoundryApplication.ENV, SIMPLE_ENV);
        entity.setManagementContext(mgmt);

        assertNull(entity.getAttribute(Attributes.MAIN_URI));
        assertNull(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL));

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertEquals(entity.getAttribute(Attributes.MAIN_URI), serverUrl.uri());
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL), applicationUrl);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), SIMPLE_ENV);
        assertTrue(driver.isRunning());
        verify(location, times(1)).setEnv(entity.getApplicationName(), SIMPLE_ENV);
    }

    @Test
    public void testStartApplicationWithoutEnv() {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(EMPTY_ENV);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);

        assertNull(entity.getAttribute(Attributes.MAIN_URI));
        assertNull(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL));

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertEquals(entity.getAttribute(Attributes.MAIN_URI), serverUrl.uri());
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ROOT_URL), applicationUrl);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), EMPTY_ENV);
        assertTrue(driver.isRunning());
        assertTrue(EMPTY_ENV.isEmpty());
        verify(location, never()).setEnv(entity.getApplicationName(), EMPTY_ENV);
    }

    @Test
    public void testSetEnvToApplication() {
        Map<String, String> env = SIMPLE_ENV;
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(env);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setConfigEvenIfOwned(VanillaCloudFoundryApplication.ENV, env);
        entity.setManagementContext(mgmt);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), env);
        assertTrue(driver.isRunning());
        verify(location, times(1)).setEnv(entity.getApplicationName(), env);

        Map<String, String> newEnv = MutableMap.of("k2", "v2");
        driver.setEnv(newEnv);
        env.putAll(newEnv);
        entity.getAttribute(VanillaCloudFoundryApplication.ENV);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ENV), env);
        verify(location, times(1)).setEnv(entity.getApplicationName(), newEnv);
    }

    @Test
    @SuppressWarnings("all")
    public void testSetNullEnvToApplication() {
        Map<String, String> newEnv = null;
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(SIMPLE_ENV);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setConfigEvenIfOwned(VanillaCloudFoundryApplication.ENV, SIMPLE_ENV);
        entity.setManagementContext(mgmt);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        driver.setEnv(newEnv);
        assertEquals(entity
                .getAttribute(VanillaCloudFoundryApplication.ENV), SIMPLE_ENV);
        assertTrue(driver.isRunning());
        verify(location, never()).setEnv(entity.getApplicationName(), newEnv);
    }

    @Test
    public void testSetMemory() {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(EMPTY_ENV);
        when(location.getMemory(anyString())).thenReturn(CUSTOM_MEMORY);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);
        mockLocationProfileUsingEntityConfig(location, entity);
        when(location.getMemory(anyString())).thenReturn(
                entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_MEMORY),
                CUSTOM_MEMORY);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertTrue(driver.isRunning());
        checkDefaultResourceProfile(entity);

        driver.setMemory(CUSTOM_MEMORY);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ALLOCATED_MEMORY).intValue(),
                CUSTOM_MEMORY);
        verify(location, times(1)).setMemory(entity.getApplicationName(), CUSTOM_MEMORY);
    }

    @Test
    public void testSetDisk() {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(EMPTY_ENV);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);
        mockLocationProfileUsingEntityConfig(location, entity);
        when(location.getDiskQuota(anyString())).thenReturn(
                entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_DISK),
                CUSTOM_DISK);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertTrue(driver.isRunning());
        checkDefaultResourceProfile(entity);

        driver.setDiskQuota(CUSTOM_DISK);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.ALLOCATED_DISK).intValue(),
                CUSTOM_DISK);
        verify(location, times(1)).setDiskQuota(entity.getApplicationName(), CUSTOM_DISK);
    }

    @Test
    public void testSetInstances() {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        when(location.getEnv(anyString())).thenReturn(EMPTY_ENV);
        when(location.getInstancesNumber(anyString())).thenReturn(CUSTOM_INSTANCES);

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);
        mockLocationProfileUsingEntityConfig(location, entity);
        when(location.getInstancesNumber(anyString())).thenReturn(
                entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_INSTANCES),
                CUSTOM_INSTANCES);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertTrue(driver.isRunning());
        checkDefaultResourceProfile(entity);

        driver.setInstancesNumber(CUSTOM_INSTANCES);
        assertEquals(entity.getAttribute(VanillaCloudFoundryApplication.INSTANCES).intValue(),
                CUSTOM_INSTANCES);
        verify(location, times(1)).setInstancesNumber(entity.getApplicationName(), CUSTOM_INSTANCES);
    }

    @Test
    public void testStopApplication() throws IOException {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doNothing().when(location).stopApplication(anyString());

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertTrue(driver.isRunning());

        driver.stop();
        mockWebServer.shutdown();
        assertFalse(driver.isRunning());
        verify(location, times(1)).stopApplication(anyString());
    }

    @Test
    public void testRestartApplication() {
        when(location.deploy(anyMap())).thenReturn(applicationUrl);
        doReturn(new StartingInfo(null)).when(location).startApplication(anyString());
        doNothing().when(location).restartApplication(anyString());

        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();
        entity.setManagementContext(mgmt);

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.start();
        assertTrue(driver.isRunning());

        driver.restart();
        verify(location, times(1)).restartApplication(entity.getApplicationName());
    }

    @Test
    public void testDeleteApplication() throws IOException {
        VanillaCloudFoundryApplicationImpl entity = new VanillaCloudFoundryApplicationImpl();

        VanillaPaasApplicationDriver driver =
                new VanillaPaasApplicationCloudFoundryDriver(entity, location);
        driver.delete();
        assertFalse(driver.isRunning());
        verify(location, times(1)).deleteApplication(anyString());
    }

    private void mockLocationProfileUsingEntityConfig(CloudFoundryPaasLocation location,
                                                      VanillaCloudFoundryApplication entity) {
        if (new MockUtil().isMock(location)) {
            doNothing().when(location).setMemory(anyString(), anyInt());
            doNothing().when(location).setDiskQuota(anyString(), anyInt());
            doNothing().when(location).setInstancesNumber(anyString(), anyInt());

            when(location.getMemory(anyString()))
                    .thenReturn(entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_MEMORY));
            when(location.getDiskQuota(anyString()))
                    .thenReturn(entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_DISK));
            when(location.getInstancesNumber(anyString()))
                    .thenReturn(entity.getConfig(VanillaCloudFoundryApplication.REQUIRED_INSTANCES));
        }
    }

    private Dispatcher getGenericDispatcher() {
        return new Dispatcher() {
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/")) {
                    return new MockResponse().setResponseCode(200);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
    }

}
