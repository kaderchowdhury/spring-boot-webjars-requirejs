/**
 * Copyright (c) 2014 Ventiv Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ventiv.webjars.requirejs.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequireJsConfigBuilderTest {

    @Test
    public void buildConfiguration() throws Exception{
        RequireJsConfigBuilder builder = new RequireJsConfigBuilder();
        Map<String, Object> requireJsConfig = builder.buildConfig();

        // Verify the paths
        assertNotNull(requireJsConfig.get("paths"));
        Map<String, String> paths = (Map<String, String>) requireJsConfig.get("paths");
        assertEquals(paths.size(), 167);
        assertEquals(paths.get("angular"), "/webjars/angularjs/1.3.0-rc.0/angular");
        assertEquals(paths.get("angular-locale_am"), "/webjars/angularjs/1.3.0-rc.0/i18n/angular-locale_am");
        assertEquals(paths.get("ng-grid"), "/webjars/ng-grid/2.0.12/ng-grid");

        // Verify the shims
        assertNotNull(requireJsConfig.get("shim"));
        Map<String, Object> shim = (Map<String, Object>) requireJsConfig.get("shim");
        assertEquals(shim.size(), 167);
        assertEquals(shim.get("angular-animate"), Arrays.asList("angular"));
        assertEquals(((Map)shim.get("angular")).get("exports"), "angular");
        assertEquals(((Map)shim.get("jquery")).get("exports"), "$");
        assertEquals(shim.get("ng-grid"), Arrays.asList("jquery", "angular"));
    }

}