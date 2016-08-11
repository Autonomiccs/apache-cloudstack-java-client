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

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class represents a single Apache CloudStack API command parameter
 */
public class ApacheCloudStackApiCommandParameter implements Comparable<ApacheCloudStackApiCommandParameter> {

    /**
     * parameter name
     */
    private String name;
    /**
     * parameter value
     */
    private Object value;

    public ApacheCloudStackApiCommandParameter(String paramName, Object value) {
        this.name = paramName;
        this.value = value;
    }

    /**
     * The hashcode is following the same idea as the {@link #compareTo(ApacheCloudStackApiCommandParameter)} method.
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.name);
        return hashCodeBuilder.toHashCode();
    }

    /**
     * We only check the {@link #name} property because, we can only have on parameter with a given name.
     */
    @Override
    public boolean equals(Object obj) {
        final boolean isIstanceOfThisClass = obj instanceof ApacheCloudStackApiCommandParameter;
        if(!isIstanceOfThisClass){
            return false;
        }
        ApacheCloudStackApiCommandParameter that = (ApacheCloudStackApiCommandParameter)obj;
        return StringUtils.equals(this.name, that.name);
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(ApacheCloudStackApiCommandParameter o) {
        return ComparatorUtils.NATURAL_COMPARATOR.compare(this.name, o.name);
    }

    @Override
    public String toString() {
        return String.format("%s=%s", name, value);
    }
}
