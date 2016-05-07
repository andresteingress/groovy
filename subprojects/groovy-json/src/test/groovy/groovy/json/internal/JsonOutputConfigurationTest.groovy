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
package groovy.json.internal

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static groovy.json.internal.JsonOutputConfiguration.newConfiguration

/**
 * @author Andre Steingress
 */
@RunWith(JUnit4)
class JsonOutputConfigurationTest extends GroovyTestCase {

    @Test
    void newJsonOutputConfigurationFromStringWithMultipleExcludes() {
        def config = newConfiguration([excludes: 'value1, value2'])

        assert config.getExcludes().hasPropertyName(null, 'value1')
        assert config.getExcludes().hasPropertyName(null, 'value2')
    }

    @Test
    void newJsonOutputConfigurationFromStringWithMultipleIncludes() {
        def config = newConfiguration([includes: 'value1, value2'])

        assert config.getIncludes().hasPropertyName(null, 'value1')
        assert config.getIncludes().hasPropertyName(null, 'value2')
    }

    @Test
    void newJsonOutputConfigurationFromStringWithSingleValue() {
        def config = newConfiguration([excludes: ' value1 '])

        assert config.getExcludes().hasPropertyName(null, 'value1')
    }

    @Test
    void newJsonOutputConfigurationFromSimpleNameAndStringExcludes() {
        def config = newConfiguration([excludes: [Foo: 'value1']])

        assert config.getExcludes().hasPropertyName(new Foo(), 'value1')
    }

    @Test
    void newJsonOutputConfigurationFromSimpleNameAndStringIncludes() {
        def config = newConfiguration([includes: [Foo: 'value1']])

        assert config.getIncludes().hasPropertyName(new Foo(), 'value1')
    }

    @Test
    void newJsonOutputConfigurationFromSimpleNameAndStringValuesExcludes() {
        def config = newConfiguration([excludes: [Foo: ' value1, value2, value3 ']])

        assert config.getExcludes().hasPropertyName(new Foo(), 'value1')
        assert config.getExcludes().hasPropertyName(new Foo(), 'value2')
        assert config.getExcludes().hasPropertyName(new Foo(), 'value3')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value4')
    }

    @Test
    void newJsonOutputConfigurationFromSimpleNameAndStringValuesIncludes() {
        def config = newConfiguration([includes: [Foo: ' value1, value2, value3 ']])

        assert config.getIncludes().hasPropertyName(new Foo(), 'value1')
        assert config.getIncludes().hasPropertyName(new Foo(), 'value2')
        assert config.getIncludes().hasPropertyName(new Foo(), 'value3')
        assert !config.getIncludes().hasPropertyName(new Foo(), 'value4')
    }

    @Test
    void newJsonOutputConfigurationFromSimpleNameWithoutStringValues() {
        def config = newConfiguration([excludes: [Foo: '']])

        assert !config.getExcludes().hasPropertyName(new Foo(), 'value1')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value2')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value3')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value4')
    }

    @Test
    void newJsonOutputConfigurationFromSimpleNameWithPropertyList() {
        def config = new JsonOutputConfiguration([excludes: [Foo: ['value1', 'value2', 'value3']]])

        assert config.getExcludes().hasPropertyName(new Foo(), 'value1')
        assert config.getExcludes().hasPropertyName(new Foo(), 'value2')
        assert config.getExcludes().hasPropertyName(new Foo(), 'value3')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value4')
    }

    @Test
    void newJsonOutputConfigurationFromEmptyConfiguration() {
        def config = newConfiguration([:])

        assert !config.getExcludes().hasPropertyName(new Foo(), 'value1')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value2')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value3')
        assert !config.getExcludes().hasPropertyName(new Foo(), 'value4')

        assert !config.getIncludes().hasPropertyName(new Foo(), 'value1')
        assert !config.getIncludes().hasPropertyName(new Foo(), 'value2')
        assert !config.getIncludes().hasPropertyName(new Foo(), 'value3')
        assert !config.getIncludes().hasPropertyName(new Foo(), 'value4')
    }

    @Test
    void newJsonOutputConfigurationWithDefaultExcludes() {
        def config = newConfiguration([:])

        assert config.getExcludes().hasPropertyName(new Foo(), 'class')
        assert config.getExcludes().hasPropertyName(new Foo(), 'metaClass')
        assert config.getExcludes().hasPropertyName(new Foo(), 'declaringClass')
    }

    static class Foo {
        String value1 = 'value1'
        String value2 = 'value2'
        String value3 = 'value3'
        String value4 = 'value4'
    }
}

