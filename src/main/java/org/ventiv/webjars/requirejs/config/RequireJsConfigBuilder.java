/**
 * Copyright (c) 2015 Ventiv Technology
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.env.Environment;
import org.webjars.RequireJS;
import org.webjars.WebJarAssetLocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author John Crygier
 */
public class RequireJsConfigBuilder {

    private static final Log log = LogFactory.getLog(RequireJsConfigBuilder.class);

    private final WebJarAssetLocator locator = new WebJarAssetLocator();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String rootPath;
    private final Environment env;

    public RequireJsConfigBuilder() {
        this("/webjars/", null);
    }

    public RequireJsConfigBuilder(String rootPath, Environment env) {
        this.rootPath = rootPath;
        this.env = env;
    }

    public Map<String, Object> buildConfig() {
        Map<String, String> webJars = locator.getWebJars();
        Map<String, String> paths = new TreeMap<String, String>();

        Map<String, Object> requireJsConfig = new LinkedHashMap<>();
        requireJsConfig.put("paths", paths);

        for (Map.Entry<String, String> webJar : webJars.entrySet()) {
            Map<String, Object> webJarRequireJsConfig = getWebJarRequireJsConfig(webJar);

            if (webJarRequireJsConfig != null) {
                buildPathForJar(webJar.getKey(), webJar.getValue(), webJarRequireJsConfig, paths);
                buildOthersForJar(webJar.getKey(), webJar.getValue(), webJarRequireJsConfig, requireJsConfig);
            }
        }

        // Add to any dependencies
        if (env != null) {
            Map<String, Object> shim = (Map<String, Object>) requireJsConfig.get("shim");
            for (Map.Entry<String, Object> shimEntry : shim.entrySet()) {
                List newDependencies = env.getProperty("webjars.requirejs.dependencies." + shimEntry.getKey(), List.class);
                if (newDependencies != null && !newDependencies.isEmpty()) {
                    if (shimEntry.getValue() == null)
                        shimEntry.setValue(new ArrayList<String>());

                    if (shimEntry.getValue() instanceof Collection)
                        ((Collection) shimEntry.getValue()).addAll(newDependencies);
                }
            }
        }

        // Add any paths / dependencies that are in the environment that are not in our config already
        if (env != null) {
            List<String> newModules = env.getProperty("webjars.requirejs.newModules", List.class);
            if (newModules != null && !newModules.isEmpty()) {
                for (String module : newModules) {
                    paths.put(module, env.getProperty("webjars.requirejs.paths." + module));

                    List<String> dependencies = env.getProperty("webjars.requirejs.dependencies." + module, List.class);
                    if (dependencies != null && !dependencies.isEmpty())
                        ((Map<String, Object>) requireJsConfig.get("shim")).put(module, dependencies);
                }
            }
        }

        return requireJsConfig;
    }

    private void buildOthersForJar(String name, String version, Map<String, Object> jarConfig, Map<String, Object> requireJsConfig) {
        for (Map.Entry<String, Object> jarConfigElement : jarConfig.entrySet()) {
            if (!jarConfigElement.getKey().equals("paths") && jarConfigElement.getValue() instanceof Map) {
                Map nestedMap = (Map) requireJsConfig.get(jarConfigElement.getKey());

                if (nestedMap == null) {
                    nestedMap = new TreeMap<>();
                    requireJsConfig.put(jarConfigElement.getKey(), nestedMap);
                }

                nestedMap.putAll((Map) jarConfigElement.getValue());
            }
        }
    }

    private void buildPathForJar(String name, String version, Map<String, Object> requireJsConfig, Map<String, String> paths) {
        if (env != null && env.containsProperty("webjars.requirejs.paths." + name)) {
            log.debug("Found property webjars.requirejs.paths." + name + ".  Overriding URL.");

            paths.put(name, rootPath + name + "/" + version + "/" + env.getProperty("webjars.requirejs.paths." + name));
            return;
        }

        if (requireJsConfig.containsKey("paths") && requireJsConfig.get("paths") instanceof Map) {
            Map<String, String> pathMap = (Map) requireJsConfig.get("paths");

            for (Map.Entry<String, String> modulePath : pathMap.entrySet()) {
                paths.put(modulePath.getKey(), rootPath + name + "/" + version + "/" + modulePath.getValue());
            }
        } else {
            log.debug("No 'paths' node in Jar's pom.xml require property.  WebJar " + name + " will not be exposed");
        }
    }

    protected Map<String, Object> getWebJarRequireJsConfig(Map.Entry<String, String> webJar) {
        String webJarConfig = RequireJS.getRawWebJarRequireJsConfig(webJar);
        try {
            return mapper.readValue(webJarConfig, Map.class);
        } catch (IOException e) {
            log.error("Unable to retrieve RequireJs Configuration from " + webJar.getKey() + ".  Skipping exposure");
            return null;
        }
    }


}
