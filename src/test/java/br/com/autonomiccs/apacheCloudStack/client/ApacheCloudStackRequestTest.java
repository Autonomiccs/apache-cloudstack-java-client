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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheCloudStackRequestTest {

    private ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest("command");

    @Test
    public void addParameterTest() {
        String paramName = "teste";
        String value = "value";

        ApacheCloudStackRequest addParameterReturn = apacheCloudStackRequest.addParameter(paramName, value);

        Assert.assertNotNull(addParameterReturn);
        Assert.assertEquals(ApacheCloudStackRequest.class, addParameterReturn.getClass());

        Set<ApacheCloudStackApiCommandParameter> parameters = apacheCloudStackRequest.getParameters();
        Assert.assertEquals(1, parameters.size());

        ApacheCloudStackApiCommandParameter parameter = parameters.iterator().next();
        Assert.assertEquals(paramName, parameter.getName());
        Assert.assertEquals(value, parameter.getValue());

    }
}
