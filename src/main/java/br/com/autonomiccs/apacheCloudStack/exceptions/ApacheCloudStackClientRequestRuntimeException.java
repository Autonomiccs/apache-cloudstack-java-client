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
package br.com.autonomiccs.apacheCloudStack.exceptions;

import org.apache.http.HttpStatus;

/**
 *   This exception is thrown when a request return with a status code different from {@link HttpStatus#SC_OK}.
 *   It contains the returned status code and the server response.
 */
@SuppressWarnings("serial")
public class ApacheCloudStackClientRequestRuntimeException extends RuntimeException {

    /**
     * Status of the HTTP request that generated this exception
     */
    private final int statusCode;
    /**
     * Response of the HTTP request that generated this exception
     */
    private final String response;

    /**
     * Command request that generated the error
     */
    private final String commandRequest;

    public ApacheCloudStackClientRequestRuntimeException(int statusCode, String responseAsString, String commandRequest) {
        this.statusCode = statusCode;
        this.response = responseAsString;
        this.commandRequest = commandRequest;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return String.format("Status code [%d], with response [%s] for request [%s]", statusCode, response, commandRequest);
    }

}
