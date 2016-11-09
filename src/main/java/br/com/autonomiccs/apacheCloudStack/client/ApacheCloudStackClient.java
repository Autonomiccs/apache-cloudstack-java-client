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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;
import br.com.autonomiccs.apacheCloudStack.exceptions.ApacheCloudStackClientRequestRuntimeException;
import br.com.autonomiccs.apacheCloudStack.exceptions.ApacheCloudStackClientRuntimeException;

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
     * This flag indicates if we are going to validate the server certificate in case of HTTPS connections.
     * The default value is 'true', meaning that we always validate the server HTTPS certificate.
     */
    protected boolean validateServerHttpsCertificate = true;

    /**
     * The validity time of the ACS request.
     * The default value is {@value #requestValidity} .
     */
    private int requestValidity = 30;

    /**
     * This parameter controls if the expiration of requests is activated or not.
     * It is activated by default. The validity of requests if defined by {@value #requestValidity} property.
     */
    private boolean shouldRequestsExpire = true;

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
    protected ApacheCloudStackUser apacheCloudStackUser;

    public ApacheCloudStackClient(String url, ApacheCloudStackUser apacheCloudStackUser) {
        this.url = adjustUrlIfNeeded(url);
        this.apacheCloudStackUser = apacheCloudStackUser;

    }

    /**
     * adds the suffix '{@value #CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX}' if it does have it.
     * It uses the method {@link #appendUrlSuffix(String)} to execute the appending.
     */
    protected String adjustUrlIfNeeded(String url) {
        if (StringUtils.endsWith(url, "/client") || StringUtils.endsWith(url, "/client/")) {
            return url;
        }
        return appendUrlSuffix(url);
    }

    /**
     * Appends the suffix '{@value #CLOUDSTACK_BASE_ENDPOINT_URL_SUFFIX}' at the end of the given URL.
     * If it is needed, it will also add, a '/' before the suffix is appended to the URL.
     */
    protected String appendUrlSuffix(String url) {
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
        boolean isSecretKeyApiKeyAuthenticationMechanism = StringUtils.isNotBlank(this.apacheCloudStackUser.getApiKey());
        String urlRequest = createApacheCloudStackApiUrlRequest(request, isSecretKeyApiKeyAuthenticationMechanism);
        logger.debug("Executing request[%s].", urlRequest);
        CloseableHttpClient httpClient = createHttpClient();
        HttpContext httpContext = createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeeded(httpClient, isSecretKeyApiKeyAuthenticationMechanism);
        try {
            return executeRequestGetResponseAsString(urlRequest, httpClient, httpContext);
        } finally {
            if (!isSecretKeyApiKeyAuthenticationMechanism) {
                executeUserLogout(httpClient, httpContext);
            }
            HttpClientUtils.closeQuietly(httpClient);
        }
    }

    /**
     * Executes the request with the given {@link HttpContext}.
     */
    protected String executeRequestGetResponseAsString(String urlRequest, CloseableHttpClient httpClient, HttpContext httpContext) {
        try {
            HttpRequestBase httpGetRequest = new HttpGet(urlRequest);
            CloseableHttpResponse response = httpClient.execute(httpGetRequest, httpContext);
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
     *  This method executes the user logout when using username/password/domain authentication.
     *  The logout is executed calling the 'logout' command of the Apache CloudStack API.
     */
    protected void executeUserLogout(CloseableHttpClient httpClient, HttpContext httpContext) {
        String urlRequest = createApacheCloudStackApiUrlRequest(new ApacheCloudStackRequest("logout").addParameter("response", "json"), false);
        String returnOfLogout = executeRequestGetResponseAsString(urlRequest, httpClient, httpContext);
        logger.debug("Logout result[%s]", returnOfLogout);
    }

    /**
     * According to the 'isSecretKeyApiKey AuthenticationMechanism' parameter this method creates an HttpContext that is used when executing requests.
     * If the user has provided his/her API/secret keys, we return a {@link BasicHttpContext} object. Otherwise, we authenticate the user with his/her username/password/domain and return an {@link HttpContext} object that contains the authenticated session Id configured as a cookie.
     */
    protected HttpContext createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeeded(CloseableHttpClient httpClient, boolean isSecretKeyApiKeyAuthenticationMechanism) {
        if (isSecretKeyApiKeyAuthenticationMechanism) {
            return new BasicHttpContext();
        }
        return createHttpContextWithAuthenticatedSessionUsingUserCredentials(httpClient);
    }

    /**
     *  It creates an {@link CloseableHttpClient} object.
     *  If {@link #validateServerHttpsCertificate} indicates that we should not validate HTTPS server certificate, we use an insecure SSL factory; the insecure factory is created using {@link #createInsecureSslFactory()}.
     */
    protected CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (!validateServerHttpsCertificate) {
            SSLConnectionSocketFactory sslsf = createInsecureSslFactory();
            httpClientBuilder.setSSLSocketFactory(sslsf);
        }
        return httpClientBuilder.build();
    }

    /**
     * This method creates an {@link HttpContext} with an authenticated JSESSIONID.
     * The authentication is performed using username, password and domain that are provided by the user.
     */
    protected HttpContext createHttpContextWithAuthenticatedSessionUsingUserCredentials(CloseableHttpClient httpClient) {
        HttpPost httpPost = createHttpPost();
        List<NameValuePair> params = getParametersForLogin();

        try {
            UrlEncodedFormEntity postParams = new UrlEncodedFormEntity(params, "UTF-8");
            httpPost.setEntity(postParams);

            CloseableHttpResponse loginResponse = httpClient.execute(httpPost);
            int statusCode = loginResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new ApacheCloudStackClientRequestRuntimeException(statusCode, getResponseAsString(loginResponse), "login");
            }
            logger.debug("Authentication response:[%s]", getResponseAsString(loginResponse));

            return createHttpContextWithCookies(loginResponse);
        } catch (IOException e) {
            throw new ApacheCloudStackClientRuntimeException(e);
        }
    }

    /**
     *  It creates an {@link HttpContext} object with a cookie store that will contain the cookies returned by the user in the {@link CloseableHttpResponse} that is received as parameter.
     */
    protected HttpContext createHttpContextWithCookies(CloseableHttpResponse loginResponse) {
        CookieStore cookieStore = new BasicCookieStore();
        createAndAddCookiesOnStoreForHeaders(cookieStore, loginResponse.getAllHeaders());
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return httpContext;
    }

    /**
     *  For every header that contains the command 'Set-Cookie' it will call the method {@link #createAndAddCookiesOnStoreForHeader(CookieStore, Header)}
     */
    protected void createAndAddCookiesOnStoreForHeaders(CookieStore cookieStore, Header[] allHeaders) {
        for (Header header : allHeaders) {
            if (StringUtils.startsWithIgnoreCase(header.getName(), "Set-Cookie")) {
                createAndAddCookiesOnStoreForHeader(cookieStore, header);
            }
        }
    }

    /**
     * This method creates a cookie for every {@link HeaderElement} of the {@link Header} given as parameter.
     * Then, it adds this newly created cookie into the {@link CookieStore} provided as parameter.
     */
    protected void createAndAddCookiesOnStoreForHeader(CookieStore cookieStore, Header header) {
        for (HeaderElement element : header.getElements()) {
            BasicClientCookie cookie = createCookieForHeaderElement(element);
            cookieStore.addCookie(cookie);
        }
    }

    /**
     *  This method will create a {@link BasicClientCookie} with the given {@link HeaderElement}.
     *  It sill set the cookie's name and value according to the {@link HeaderElement#getName()} and {@link HeaderElement#getValue()} methods.
     *  Moreover, it will transport every {@link HeaderElement} parameter to the cookie using the {@link BasicClientCookie#setAttribute(String, String)}.
     *  Additionally, it configures the cookie path ({@link BasicClientCookie#setPath(String)}) to value '/client/api' and the cookie domain using {@link #configureDomainForCookie(BasicClientCookie)} method.
     */
    protected BasicClientCookie createCookieForHeaderElement(HeaderElement element) {
        BasicClientCookie cookie = new BasicClientCookie(element.getName(), element.getValue());
        for (NameValuePair parameter : element.getParameters()) {
            cookie.setAttribute(parameter.getName(), parameter.getValue());
        }
        cookie.setPath("/client/api");
        configureDomainForCookie(cookie);
        return cookie;
    }

    /**
     *  It configures the cookie domain with the domain of the Apache CloudStack that is being accessed.
     *  The domain is extracted from {@link #url} variable.
     */
    protected void configureDomainForCookie(BasicClientCookie cookie) {
        try {
            HttpHost httpHost = URIUtils.extractHost(new URI(url));
            String domain = httpHost.getHostName();
            cookie.setDomain(domain);
        } catch (URISyntaxException e) {
            throw new ApacheCloudStackClientRuntimeException(e);
        }
    }

    /**
     *  Creates an {@link HttpPost} object to be sent to Apache CloudStack API.
     *  The content type configured for this request is 'application/x-www-form-urlencoded'.
     */
    protected HttpPost createHttpPost() {
        HttpPost httpPost = new HttpPost(url + APACHE_CLOUDSTACK_API_ENDPOINT);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        return httpPost;
    }

    /**
     *  This method creates a list of {@link NameValuePair} and returns the data for login using username and password.
     */
    protected List<NameValuePair> getParametersForLogin() {
        List<NameValuePair> params = new ArrayList<>(4);
        params.add(new BasicNameValuePair("command", "login"));
        params.add(new BasicNameValuePair("username", this.apacheCloudStackUser.getUsername()));
        params.add(new BasicNameValuePair("password", this.apacheCloudStackUser.getPassword()));
        params.add(new BasicNameValuePair("domain", this.apacheCloudStackUser.getDomain()));
        return params;
    }

    /**
     * This method creates an insecure SSL factory that will trust on self signed certificates.
     * For that we use {@link TrustSelfSignedStrategy}.
     */
    protected SSLConnectionSocketFactory createInsecureSslFactory() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(new TrustSelfSignedStrategy());
            return new SSLConnectionSocketFactory(builder.build());
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new ApacheCloudStackClientRuntimeException(e);
        }
    }

    /**
     * It retrieves the response status as a {@link String}
     */
    protected String getResponseAsString(CloseableHttpResponse response) throws IOException {
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
     * and then, if it needs, it creates the signature using the method {@link #createSignature(String)} and append it to the URL.
     */
    protected String createApacheCloudStackApiUrlRequest(ApacheCloudStackRequest request, boolean shouldSignAppendSignature) {
        StringBuilder urlRequest = new StringBuilder(url + APACHE_CLOUDSTACK_API_ENDPOINT);
        urlRequest.append("?");

        String queryString = createCommandString(request);
        urlRequest.append(queryString);

        if (shouldSignAppendSignature) {
            String signature = createSignature(queryString);
            urlRequest.append("&signature=" + getUrlEncodedValue(signature));
        }
        return urlRequest.toString();
    }

    /**
     * Creates a signature (HMAC-sha1) with the {@link #ApacheCloudStackUser#getSecretKey()} and the given queryString
     * The returner signature is encoded in Base64.
     */
    protected String createSignature(String queryString) {
        byte[] signatureBytes = HmacUtils.hmacSha1(apacheCloudStackUser.getSecretKey(), queryString.toLowerCase());
        return Base64.encodeBase64String(signatureBytes);
    }

    /**
     *  It creates the command query string, placing the parameters in alphabetical order.
     *  To execute the sorting, it uses the {@link #createSortedCommandQueryList(ApacheCloudStackRequest)} method.
     */
    protected String createCommandString(ApacheCloudStackRequest request) {
        List<ApacheCloudStackApiCommandParameter> queryCommand = createSortedCommandQueryList(request);

        StringBuilder commandString = new StringBuilder();
        for (ApacheCloudStackApiCommandParameter param : queryCommand) {
            String value = getUrlEncodedValue(param.getValue());
            commandString.append(String.format("%s=%s&", param.getName(), value));
        }
        return commandString.toString().substring(0, commandString.length() - 1);
    }

    /**
     *  This method encodes the parameter value as specified by Apache CloudStack
     */
    protected String getUrlEncodedValue(Object paramValue) {
        String value = Objects.toString(paramValue);
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ApacheCloudStackClientRuntimeException(e);
        }
        return value.replaceAll("\\+", "%20");
    }

    /**
     *  This methods adds the final data needed to the command query.
     *  It will add a parameter called 'command' with the value of {@link ApacheCloudStackRequest#getCommand()} as value.
     *  It also adds a parameter called 'apiKey', with the value of {@link #ApacheCloudStackUser#getApiKey()} as value.
     *  Then, it will sort the parameters that are in a list in alphabetical order.
     */
    protected List<ApacheCloudStackApiCommandParameter> createSortedCommandQueryList(ApacheCloudStackRequest request) {
        List<ApacheCloudStackApiCommandParameter> queryCommand = new ArrayList<>();
        queryCommand.addAll(request.getParameters());
        queryCommand.add(new ApacheCloudStackApiCommandParameter("command", request.getCommand()));
        if (StringUtils.isNotBlank(this.apacheCloudStackUser.getApiKey())) {
            queryCommand.add(new ApacheCloudStackApiCommandParameter("apiKey", this.apacheCloudStackUser.getApiKey()));
        }
        configureRequestExpiration(queryCommand);
        Collections.sort(queryCommand);
        return queryCommand;
    }

    /**
     * This method configures the request expiration if needed.
     * It uses the value defined at {@link #requestValidity} to determine until when the request is valid.
     * It also uses the parameter {@link #shouldRequestsExpire} to decide if it has to configure or not the validity of the request.
     * Moreover, if the 'apacheCloudStackRequestList' contains the 'expires' it will only add a parameter called 'signatureVersion=3', in order to enable that override.
     */
    protected void configureRequestExpiration(List<ApacheCloudStackApiCommandParameter> apacheCloudStackRequestList) {
        boolean isOverridingExpirationConfigs = apacheCloudStackRequestList.contains(new ApacheCloudStackApiCommandParameter("expires", StringUtils.EMPTY));
        if (!isOverridingExpirationConfigs && !shouldRequestsExpire) {
            return;
        }
        apacheCloudStackRequestList.add(new ApacheCloudStackApiCommandParameter("signatureVersion", 3));
        if (isOverridingExpirationConfigs) {
            return;
        }
        String expirationDataAsSring = createExpirationDate();
        apacheCloudStackRequestList.add(new ApacheCloudStackApiCommandParameter("expires", expirationDataAsSring));
    }

    /**
     *  This method creates the expiration date as a string according to the ISO 8601.
     */
    protected String createExpirationDate() {
        DateFormat acsIso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return acsIso8601DateFormat.format(getExpirationDate());
    }

    /**
     * Creates the expiration date, by adding the {@link #requestValidity} to the current time.
     */
    protected Date getExpirationDate() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, requestValidity);
        return now.getTime();
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

    public void setRequestValidity(int requestValidity) {
        this.requestValidity = requestValidity;
    }

    public void setShouldRequestsExpire(boolean shouldRequestsExpire) {
        this.shouldRequestsExpire = shouldRequestsExpire;
    }
}
