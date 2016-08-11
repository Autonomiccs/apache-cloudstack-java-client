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

import java.util.HashSet;
import java.util.Set;

/**
 * This class represent a request to be executed upon the Apache CloudStack API.
 * There is a command to be called, and parameters to be sent.
 */
public class ApacheCloudStackRequest {

    /**
     * The command to be executed
     */
    private String command;

    /**
     * The parameters that are going to be sent.
     */
    private Set<ApacheCloudStackApiCommandParameter> parameters = new HashSet<ApacheCloudStackApiCommandParameter>();

    public ApacheCloudStackRequest(String command) {
        this.command = command;
    }

    /**
     * Adds a parameter in the internal parameters map.
     *
     * @return this own object, in order to allow chained calls.
     */
    public ApacheCloudStackRequest addParameter(String paramName, Object value) {
        parameters.add(new ApacheCloudStackApiCommandParameter(paramName, value));
        return this;
    }

    public String getCommand() {
        return command;
    }

    public Set<ApacheCloudStackApiCommandParameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return String.format("Request[command=%s; parameters(%s)]", command, parameters);
    }
}
