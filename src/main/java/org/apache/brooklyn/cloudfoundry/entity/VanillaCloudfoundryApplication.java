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


import org.apache.brooklyn.api.entity.Entity;
import org.apache.brooklyn.api.entity.ImplementedBy;
import org.apache.brooklyn.api.sensor.AttributeSensor;
import org.apache.brooklyn.cloudfoundry.location.CloudFoundryPaasLocation;
import org.apache.brooklyn.config.ConfigKey;
import org.apache.brooklyn.core.config.ConfigKeys;
import org.apache.brooklyn.core.config.MapConfigKey;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.entity.BrooklynConfigKeys;
import org.apache.brooklyn.core.entity.lifecycle.Lifecycle;
import org.apache.brooklyn.core.entity.trait.Startable;
import org.apache.brooklyn.core.sensor.Sensors;
import org.apache.brooklyn.util.collections.MutableMap;
import org.apache.brooklyn.util.core.flags.SetFromFlag;
import org.apache.brooklyn.util.time.Duration;

@ImplementedBy(VanillaCloudfoundryApplicationImpl.class)
public interface VanillaCloudfoundryApplication extends Entity, Startable {

    @SetFromFlag("name")
    ConfigKey<String> APPLICATION_NAME = ConfigKeys.newStringConfigKey(
            "cloudFoundry.application.name", "Name of the application");

    @SetFromFlag("path")
    ConfigKey<String> ARTIFACT_PATH = ConfigKeys.newStringConfigKey(
            "cloudFoundry.application.artifact", "URI of the application");


    @SetFromFlag("buildpack")
    ConfigKey<String> BUILDPACK = ConfigKeys.newStringConfigKey(
            "cloudFoundry.application.buildpack", "Buildpack to deploy an application");

    @SetFromFlag("env")
    MapConfigKey<String> ENVS = new MapConfigKey<String>(String.class, "cloudfoundry.application.env",
            "Enviroment variables for the application", MutableMap.<String, String>of());

    @SetFromFlag("domain")
    ConfigKey<String> APPLICATION_DOMAIN = ConfigKeys.newStringConfigKey(
            "cloudFoundry.application.domain", "Domain for the application");

    @SetFromFlag("cloudfoundry.instances")
    ConfigKey<Integer> REQUIRED_INSTANCES = ConfigKeys.newIntegerConfigKey(
            "cloudfoundry.profile.instances", "Number of instances of the application", 1);

    @SetFromFlag("cloudfoundry.instances")
    ConfigKey<Integer> REQUIRED_MEMORY = ConfigKeys.newIntegerConfigKey(
            "cloudfoundry.profile.memory", "Memory allocated for the application (MB)", 512);

    @SetFromFlag("cloudfoundry.instances")
    ConfigKey<Integer> REQUIRED_DISK = ConfigKeys.newIntegerConfigKey(
            "cloudfoundry.profile.disk", "Disk size allocated for the application (MB)", 1024);

    @SetFromFlag("startTimeout")
    ConfigKey<Duration> START_TIMEOUT = BrooklynConfigKeys.START_TIMEOUT;

    AttributeSensor<String> ROOT_URL =
            Sensors.newStringSensor("webapp.url", "URL of the application");

    AttributeSensor<CloudFoundryPaasLocation> CLOUDFOUNDRY_LOCATION = Sensors.newSensor(
            CloudFoundryPaasLocation.class, "cloudFoundryWebApp.paasLocation",
            "CloudFoundry location used to deploy the application");

    AttributeSensor<Boolean> SERVICE_PROCESS_IS_RUNNING = Sensors.newBooleanSensor(
            "service.process.isRunning",
            "Whether the process for the service is confirmed as running");

    AttributeSensor<Lifecycle> SERVICE_STATE_ACTUAL = Attributes.SERVICE_STATE_ACTUAL;


}