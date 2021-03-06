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
package org.apache.brooklyn.cloudfoundry.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameResolver {

    public static String findArchiveNameFromUrl(String url) {
        String name = url.substring(url.lastIndexOf('/') + 1);
        if (name.indexOf("?") > 0) {
            Pattern p = Pattern.compile("[A-Za-z0-9_\\-]+\\..(ar|AR)($|(?=[^A-Za-z0-9_\\-]))");
            Matcher wars = p.matcher(name);
            if (wars.find()) {
                name = wars.group();
            }
        }
        return name;
    }
}
