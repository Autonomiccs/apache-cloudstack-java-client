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
package br.com.autonomiccs.apacheCloudStack.client.beans;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class represent an Apache CloudStack users. It holds the user data that is needed to execute the authentication process.
 * We can use either the username/password  or the secret key and API key authentication mechanism.
 */
public class ApacheCloudStackUser {

    /**
     * User name used to authenticate a user with an Apache CloudStack application
     */
    private String username;
    /**
     * The password e used to authenticate a user with an Apache CloudStack application
     */
    private String password;
    /**
     * Domain of in which the user is enrolled. Be aware that if no domain is specified, Apache CloudStack uses the default 'ROOT'.
     */
    private String domain;

    /**
     * The secret key of the user that can be used to execute API commands
     */
    private String secretKey;
    /**
     * The API key of the user tha can be used to execute API commands
     */
    private String apiKey;

    public ApacheCloudStackUser(String secretKey, String apiKey) {
        this.secretKey = secretKey;
        this.apiKey = apiKey;
    }

    public ApacheCloudStackUser(String username, String password, String domain) {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.username);
        hashCodeBuilder.append(this.password);
        hashCodeBuilder.append(this.domain);

        hashCodeBuilder.append(this.secretKey);
        hashCodeBuilder.append(this.apiKey);

        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isObjInstanceOfThisClass = obj instanceof ApacheCloudStackUser;
        if (!isObjInstanceOfThisClass) {
            return false;
        }
        ApacheCloudStackUser that = (ApacheCloudStackUser)obj;
        EqualsBuilder equalsBuilder = new EqualsBuilder();

        equalsBuilder.append(this.username, that.username);
        equalsBuilder.append(this.password, that.password);
        equalsBuilder.append(this.domain, that.domain);

        equalsBuilder.append(this.secretKey, that.secretKey);
        equalsBuilder.append(this.apiKey, that.apiKey);

        return equalsBuilder.isEquals();
    }
}
