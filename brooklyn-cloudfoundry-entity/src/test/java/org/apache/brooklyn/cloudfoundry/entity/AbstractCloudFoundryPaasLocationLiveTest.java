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

import java.util.UUID;

import org.apache.brooklyn.api.location.LocationSpec;
import org.apache.brooklyn.cloudfoundry.location.CloudFoundryPaasLocation;
import org.apache.brooklyn.core.entity.Entities;
import org.apache.brooklyn.core.internal.BrooklynProperties;
import org.apache.brooklyn.core.mgmt.internal.LocalManagementContext;
import org.apache.brooklyn.core.test.BrooklynAppLiveTestSupport;
import org.apache.brooklyn.core.test.entity.LocalManagementContextForTests;
import org.apache.brooklyn.core.test.entity.TestApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


public abstract class AbstractCloudFoundryPaasLocationLiveTest extends BrooklynAppLiveTestSupport {

    private static final Logger log = LoggerFactory.getLogger(AbstractCloudFoundryPaasLocationLiveTest.class);

    protected BrooklynProperties brooklynProperties;
    protected LocalManagementContext managementContext;
    protected CloudFoundryPaasLocation cloudFoundryPaasLocation;
    protected final String LOCATION_SPEC_NAME = "cloudfoundry-instance";
    protected final String APPLICATION_NAME = "test-brooklyn-application-" + UUID.randomUUID()
            .toString().substring(0, 8);


    @BeforeMethod
    public void setUp() throws Exception {
        managementContext = newLocalManagementContext();
        brooklynProperties = new LocalManagementContext().getBrooklynProperties();
        cloudFoundryPaasLocation = newSampleCloudFoundryLocationForTesting(LOCATION_SPEC_NAME);
        app = TestApplication.Factory.newManagedInstanceForTests();
    }

    protected LocalManagementContext newLocalManagementContext() {
        return new LocalManagementContextForTests(BrooklynProperties.Factory.newDefault());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (app != null) {
            Entities.destroyAllCatching(app.getManagementContext());
        }
        if (managementContext != null) {
            Entities.destroyAll(managementContext);
        }
    }

    protected CloudFoundryPaasLocation newSampleCloudFoundryLocationForTesting(String spec) {
        return (CloudFoundryPaasLocation) managementContext.getLocationRegistry().getLocationManaged(spec);
    }

    public String getClasspathUrlForResource(String resourceName) {
        return "classpath://" + resourceName;
    }


}
