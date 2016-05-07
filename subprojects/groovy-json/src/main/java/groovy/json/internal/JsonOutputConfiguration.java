/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.json.internal;

import java.util.*;

/**
 * Internal configuration representation in use by {@link groovy.json.JsonOutput}. Takes a configuration
 * {@link java.util.Map} and parses its content.
 *
 * @see groovy.json.JsonOutput
 *
 * @author Andre Steingress
 */
public class JsonOutputConfiguration {

    /**
     * Internal class for holding "properties" that might either be used for inclusion or exclusion.
     */
    public static class Properties {

        // Set of global property names
        private Set<String> globalPropertyNames = new HashSet<String>();

        // Mapping between class names (simple names) and property names
        private Map<String, List<String>> propertyNamesWithSimpleNames = new HashMap<String, List<String>>();

        public Properties(Object configuration, String... defaultGlobalPropertyNames) {
            init(configuration, defaultGlobalPropertyNames);
        }

        /**
         * @return whether there are property names defined or not.
         */
        public boolean isEmpty() {
            return globalPropertyNames.isEmpty() && propertyNamesWithSimpleNames.isEmpty();
        }

        /**
         * @return whether the given <tt>propertyName</tt> is part of this properties object.
         */
        public boolean hasPropertyName(final Object obj, final String propertyName) {

            if (propertyName == null) return false;
            if (globalPropertyNames.contains(propertyName)) return true;

            if (obj == null) return false;

            return propertyNamesWithSimpleNames.containsKey(mapClassToStringKey(obj.getClass())) && propertyNamesWithSimpleNames.get(mapClassToStringKey(obj.getClass())).contains(propertyName);

        }

        /**
         * In charge of parsing the configuration object that was found for the "excludes" or "includes" configuration map entry.
         *
         * The configuration may be a {@link String} or a {@link Map}.
         *
         * In case it is a {@link String}, the string is interpreted as list of property names, separated by "," (comma).
         * The properties will then be globally excluded as the {@link String} does not contain any class references,
         * just property names.
         *
         * In case the configuration object is a {@link Map}, the map may consist of {@code String/String},
         * {@code Class/String}, {@code String/List<String>} or {@code Class/List<String>} content. The map key always
         * represents a simple class name (from {@link Class#getSimpleName()}). Equally named classes can not be configured
         * differently in the current implementation as even class map keys are mapped to their String representations.
         */
        @SuppressWarnings("unchecked")
        private void init(final Object configuration, final String[] defaultGlobalPropertyNames) {

            if (defaultGlobalPropertyNames != null && defaultGlobalPropertyNames.length > 0) {
                globalPropertyNames.addAll(Arrays.asList(defaultGlobalPropertyNames));
            }

            // configuration object is a plain String, e.g. [excludes: "prop1, prop2"]
            if (configuration instanceof String) {
                String configurationString = (String) configuration;
                List<String> configurationValues = toUnmodifiableList(configurationString);
                if (!configurationValues.isEmpty()) {
                    globalPropertyNames.addAll(configurationValues);
                }
            }

            // configuration object is a Map, e.g. [excludes: [MyClass: 'default']] or [excludes: [MyClass: ['default', 'other']]]
            if (configuration instanceof Map) {
                Map<Object, Object> configurationMap = (Map<Object, Object>) configuration;

                for (Map.Entry<Object, Object> entry : configurationMap.entrySet()) {
                    Object key = entry.getKey();

                    // the map value is a String of property names
                    if (entry.getValue() instanceof String) {

                        List<String> configurationValues = toUnmodifiableList((String) entry.getValue());
                        if (!configurationValues.isEmpty()) {
                            if (key instanceof String) {
                                propertyNamesWithSimpleNames.put((String) key, configurationValues);
                            } else if (key instanceof Class) {
                                propertyNamesWithSimpleNames.put(mapClassToStringKey((Class) key), configurationValues);
                            }
                        }

                        // the map value is an explicitly defined collection of property names
                    } else if (entry.getValue() instanceof Collection) {
                        if (key instanceof String) {
                            propertyNamesWithSimpleNames.put((String) key, Collections.unmodifiableList(new ArrayList<String>((Collection<String>) entry.getValue())));
                        } else if (key instanceof Class) {
                            propertyNamesWithSimpleNames.put(mapClassToStringKey((Class) key), Collections.unmodifiableList(new ArrayList<String>((Collection<String>) entry.getValue())));
                        }
                    }
                }
            }
        }

        /**
         * Takes a {@link String} and converts it to a List of strings.
         */
        private List<String> toUnmodifiableList(String value) {
            if (value == null || value.length() == 0) return Collections.emptyList();

            String[] propertyNames = value.split(CONFIGURATION_VALUE_SEPARATOR_REGEXP);
            if (propertyNames.length == 0) return Collections.emptyList();

            List<String> trimmedPropertyNames = new ArrayList<String>();
            for (String propertyName : propertyNames) {
                String trimmedPropertyName = propertyName.trim();
                if (trimmedPropertyName.length() > 0) {
                    trimmedPropertyNames.add(trimmedPropertyName);
                }
            }

            return Collections.unmodifiableList(trimmedPropertyNames);
        }

        /**
         * Maps the given <tt>clazz</tt> to its internal {@link String} representation.
         */
        private String mapClassToStringKey(Class clazz) {
            return clazz.getSimpleName();
        }
    }

    private static final String CONFIGURATION_KEY_EXCLUDES = "excludes";
    private static final String CONFIGURATION_KEY_INCLUDES = "includes";

    private static final String CONFIGURATION_VALUE_SEPARATOR_REGEXP = "\\s*,\\s*";

    private final Properties excludes;
    private final Properties includes;

    /**
     * @return a new instance with the default values for JsonOutputConfiguration.
     */
    public static JsonOutputConfiguration newConfiguration() {
        return new JsonOutputConfiguration(Collections.<String, Object>emptyMap());
    }

    /**
     * @return a new JsonOutputConfiguration instance based on the given <tt>configuration</tt> map.
     */
    public static JsonOutputConfiguration newConfiguration(final Map<String, Object> configuration) {
        if (configuration == null) throw new IllegalArgumentException("Argument 'configuration' must not be null!");

        return new JsonOutputConfiguration(configuration);
    }

    /**
     * Takes a <tt>configuration</tt> map and parses its content. The map is guaranteed to be left unmodified by this
     * class. The <tt>configuration</tt> argument may reference an empty map.
     */
    protected JsonOutputConfiguration(final Map<String, Object> configuration) {
        if (configuration == null) throw new IllegalArgumentException("Argument 'configuration' must not be null!");

        // excludes has a default set of global properties that will always be excluded during rendering
        excludes = new Properties(configuration.get(CONFIGURATION_KEY_EXCLUDES), "class", "metaClass", "declaringClass");
        includes = new Properties(configuration.get(CONFIGURATION_KEY_INCLUDES));
    }

    /**
     * @return the properties for exclusion.
     */
    public Properties getExcludes() {
        return excludes;
    }

    /**
     * @return the properties for inclusion.
     */
    public Properties getIncludes() {
        return includes;
    }
}
