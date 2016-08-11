/*
 * Apache CloudStack Java Client
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.apacheCloudStack.client;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheCloudStackApiCommandParameterTest {


    @Test
    public void equalsTest() {
        ApacheCloudStackApiCommandParameter apacheCloudStackApiCommandParameter1 = new ApacheCloudStackApiCommandParameter("param1", null);
        ApacheCloudStackApiCommandParameter apacheCloudStackApiCommandParameter2 = new ApacheCloudStackApiCommandParameter("param1", "value");

        Assert.assertEquals(apacheCloudStackApiCommandParameter1, apacheCloudStackApiCommandParameter2);
    }

    @Test
    public void hashCodeTest() {
        ApacheCloudStackApiCommandParameter apacheCloudStackApiCommandParameter = new ApacheCloudStackApiCommandParameter("param1", "value");
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append("param1");

        Assert.assertEquals(hashCodeBuilder.toHashCode(), apacheCloudStackApiCommandParameter.hashCode());

    }

    @Test
    public void compareToTest() {
        ApacheCloudStackApiCommandParameter apacheCloudStackApiCommandParameter = new ApacheCloudStackApiCommandParameter("param1", "value");
        ApacheCloudStackApiCommandParameter apacheCloudStackApiCommandParameterOther = new ApacheCloudStackApiCommandParameter("param2", "value");

        Assert.assertEquals(-1, apacheCloudStackApiCommandParameter.compareTo(apacheCloudStackApiCommandParameterOther));
    }
}
