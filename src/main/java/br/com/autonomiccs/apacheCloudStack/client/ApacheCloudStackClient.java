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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.autonomiccs.apacheCloudStack.ApacheCloudStackClientRequestRuntimeException;
import br.com.autonomiccs.apacheCloudStack.ApacheCloudStackClientRuntimeException;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;

/**
 * Apache CloudStack API client.
 * The client is a pair of URL and user credentials ({@link #apacheCloudStackUser}).
 */
public class ApacheCloudStackClient {
    /**
     * The suffix 'client' that is the endpoint to access Apache CloudStack.
     */
    private final static String CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX = "client";
    /**
     * The Apache CloudStack API endpoint
     */
    private static final String APACHE_CLOUDSTACK_API_ENDPOINT = "/api";

    /**
     * This flag indicates if we are going to validade the server certificate in case of HTTPS connections.
     * The default value is 'true', meaning that we always validate the server HTTPS certificate.
     */
    private boolean validateServerHttpsCertificate = true;

    private Gson gson = new GsonBuilder().create();
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The URL used to access Apache CloudStack API.
     * Ex: https://cloud.domain.com/client
     */
    private String url;

    /**
     * User credentials that can be used to access ApacheCloudStack.
     * They can be either a pair of secret key and API key or a triple of username, password and domain
     */
    private ApacheCloudStackUser apacheCloudStackUser;

    public ApacheCloudStackClient(String url, ApacheCloudStackUser apacheCloudStackUser) {
        this.url = adjustUrlIfNeeded(url);
        this.apacheCloudStackUser = apacheCloudStackUser;

    }

    /**
     * adds the suffix '{@value #CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX}' if it does have it.
     * It uses the method {@link #appendUrlSuffix(String)} to execute the appending.
     */
    private String adjustUrlIfNeeded(String url) {
        if (!StringUtils.endsWith(url, "/client")) {
            url = appendUrlSuffix(url);
        }
        return url;
    }

    /**
     * Appends the suffix '{@value #CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX}' at the end of the given URL.
     * If it is needed, it will also add, a '/' before the suffix is appended to the URL.
     */
    private String appendUrlSuffix(String url) {
        if (StringUtils.endsWith(url, "/")) {
            return url + CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX;
        }
        return url + "/" + CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX;
    }

    /**
     * This method executes the given {@link ApacheCloudStackRequest}.
     * It will return the response as a plain {@link String}.
     * You should have in mind that if the parameter 'response' is not set, the default is 'XML'.
     */
    public String executeRequest(ApacheCloudStackRequest request) {
        String urlRequest = createApacheCloudStackApiUrlRequest(request);

        CloseableHttpClient httpClient = createHttpClient();
        HttpGet httpGetRequest = new HttpGet(urlRequest);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGetRequest);
            StatusLine requestStatus = response.getStatusLine();
            if (requestStatus.getStatusCode() == HttpStatus.SC_OK) {
                return getResponseAsString(response);
            }
            throw new ApacheCloudStackClientRequestRuntimeException(requestStatus.getStatusCode(), getResponseAsString(response), urlRequest.toString());
        } catch (IOException e) {
            logger.error(String.format("Error while executing request [%s]", urlRequest));
            throw new ApacheCloudStackClientRuntimeException(e);
        }
    }

    /**
     *  It creates an {@link CloseableHttpClient} object.
     *  If {@link #validateServerHttpsCertificate} indicates that we should not validate HTTPS server certificate, we use an unsecure SSL factory; the insecure factory is created using {@link #createUnsecureSslFactory()}.
     */
    private CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (!validateServerHttpsCertificate) {
            SSLConnectionSocketFactory sslsf = createUnsecureSslFactory();
            httpClientBuilder.setSSLSocketFactory(sslsf);
        }
        return httpClientBuilder.build();
    }

    /**
     * This method creates an insecure SSL factory that will trust on self signed certificates.
     * For that we use {@link TrustSelfSignedStrategy}.
     */
    private SSLConnectionSocketFactory createUnsecureSslFactory() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            return new SSLConnectionSocketFactory(builder.build());
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new ApacheCloudStackClientRuntimeException(e);
        }
    }

    /**
     * It retrieves the response status as a {@link String}
     */
    private String getResponseAsString(CloseableHttpResponse response) throws IOException {
        InputStream responseContent = response.getEntity().getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(responseContent, writer, Charset.defaultCharset());

        responseContent.close();
        response.close();
        return writer.toString();
    }

    /**
     * This method creates transforms the given {@link ApacheCloudStackRequest} into a URL requeest for the Apache CloudStack API.
     * Therefore, it will create a command query string following the CloudStack specifications using method {@link #createCommandString(ApacheCloudStackRequest)};
     * and then, create the signature using the method {@link #createSignature(String)} and append it to the URL.
     */
    private String createApacheCloudStackApiUrlRequest(ApacheCloudStackRequest request) {
        StringBuilder urlRequest = new StringBuilder(url + APACHE_CLOUDSTACK_API_ENDPOINT);
        urlRequest.append("?");

        String queryString = createCommandString(request);
        urlRequest.append(queryString);

        String signature = createSignature(queryString);
        urlRequest.append("&signature=" + signature);
        return urlRequest.toString();
    }

    /**
     * Creates a signature (HMAC-sha1) with the {@link #ApacheCloudStackUser#getSecretKey()} and the given queryString
     * The returner signature is encoded in Base64.
     */
    private String createSignature(String queryString) {
        byte[] signatureBytes = HmacUtils.hmacSha1(apacheCloudStackUser.getSecretKey(), queryString.toLowerCase());
        return Base64.encodeBase64String(signatureBytes);
    }

    /**
     *  It creates the command query string, placing the parameters in alphabetical order.
     *  To execute the sorting, it uses the {@link #createSortedCommandQueryList(ApacheCloudStackRequest)} method.
     */
    private String createCommandString(ApacheCloudStackRequest request) {
        List<ApacheCloudStackApiCommandParameter> queryCommand = createSortedCommandQueryList(request);

        StringBuilder commandString = new StringBuilder();
        for (ApacheCloudStackApiCommandParameter param : queryCommand) {
            commandString.append(String.format("%s=%s&", param.getName(), Objects.toString(param.getValue()).replaceAll("\\+", "%20")));
        }
        return commandString.toString().substring(0, commandString.length() - 1);
    }

    /**
     *  This methods adds the final data needed to the command query.
     *  It will add a parameter called 'command' with the value of {@link ApacheCloudStackRequest#getCommand()} as value.
     *  It also adds a parameter called 'apiKey', with the value of {@link #ApacheCloudStackUser#getApiKey()} as value.
     *  Then, it will sort the parameters that are in a list in alphabetical order.
     */
    private List<ApacheCloudStackApiCommandParameter> createSortedCommandQueryList(ApacheCloudStackRequest request) {
        List<ApacheCloudStackApiCommandParameter> queryCommand = new ArrayList<>();
        queryCommand.addAll(request.getParameters());
        queryCommand.add(new ApacheCloudStackApiCommandParameter("command", request.getCommand()));
        queryCommand.add(new ApacheCloudStackApiCommandParameter("apiKey", this.apacheCloudStackUser.getApiKey()));
        Collections.sort(queryCommand);
        return queryCommand;
    }

    /**
     *  It executes the given request and converts the result into an object of the given type.
     *  This method will change the response type to 'JSON'. To execute the request, it uses the method {@link #executeRequest(ApacheCloudStackRequest)}.
     *  To convert the result into an object, it will use {@link Gson#fromJson(String, Class)}
     */
    public <T> T executeRequest(ApacheCloudStackRequest request, Class<T> clazz) {
        request.addParameter("response", "json");
        String response = executeRequest(request);
        return gson.fromJson(response, clazz);
    }

    public void setValidateServerHttpsCertificate(boolean validateServerHttpsCertificate) {
        this.validateServerHttpsCertificate = validateServerHttpsCertificate;
    }
}
