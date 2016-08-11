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
package br.com.autonomiccs.apacheCloudStack.examples;

import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackClient;
import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackRequest;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;

public class UsingChainedParametersAddition {

    public static void main(String[] args) {
        String secretKey = "<secretKey>";
        String apiKey = "<apiKey>";
        String cloudStackUrl = "https://cloud.domain.com/client";

        ApacheCloudStackUser apacheCloudStackUser = new ApacheCloudStackUser(secretKey, apiKey);
        ApacheCloudStackClient apacheCloudStackClient = new ApacheCloudStackClient(cloudStackUrl, apacheCloudStackUser);

        ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest("listClusters");
        apacheCloudStackRequest
        .addParameter("response", "json")
        .addParameter("name", "clusterName");

        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        System.out.println(response);
    }
}
