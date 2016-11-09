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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;
import br.com.autonomiccs.apacheCloudStack.exceptions.ApacheCloudStackClientRequestRuntimeException;
import br.com.autonomiccs.apacheCloudStack.exceptions.ApacheCloudStackClientRuntimeException;

@RunWith(MockitoJUnitRunner.class)
public class ApacheCloudStackClientTest {

    private ApacheCloudStackClient apacheCloudStackClient;

    @Mock
    private ApacheCloudStackUser apacheCloudStackUser;

    private String cloudStackDomain = "cloud.domain.com";
    private String cloudStackUrl = "https://" + cloudStackDomain + "/client";

    @Before
    public void setup() {
        apacheCloudStackClient = Mockito.spy(new ApacheCloudStackClient(cloudStackUrl, apacheCloudStackUser));
    }

    @Test
    public void adjustUrlIfNeededTestEndingWithSuffix() {
        executeAndVerifyAdjustUrlIfNeededTest(cloudStackUrl, 0);
    }

    @Test
    public void adjustUrlIfNeededTestEndingWithoutSuffix() {
        executeAndVerifyAdjustUrlIfNeededTest("https://cloud.domain.com", 1);
    }

    private void executeAndVerifyAdjustUrlIfNeededTest(String givenCloudStackUrl, int appendCallTimes) {
        String url = apacheCloudStackClient.adjustUrlIfNeeded(givenCloudStackUrl);

        Assert.assertEquals(cloudStackUrl, url);
        Mockito.verify(apacheCloudStackClient, Mockito.times(appendCallTimes)).appendUrlSuffix(Mockito.anyString());
    }

    @Test
    public void adjustUrlIfNeededTestUrlEndingWithoutSlashClient() {
        String urlWithSuffix = apacheCloudStackClient.adjustUrlIfNeeded("https://cloud.domain.com/");
        Assert.assertEquals(cloudStackUrl, urlWithSuffix);
    }

    @Test
    public void adjustUrlIfNeededTestUrlEndingWithoutSlashAndClient() {
        String urlWithSuffix = apacheCloudStackClient.adjustUrlIfNeeded("https://cloud.domain.com");
        Assert.assertEquals(cloudStackUrl, urlWithSuffix);
    }

    @Test
    public void adjustUrlIfNeededTestUrlEndingWithSlashClientWithoutLastSlash() {
        String urlWithSuffix = apacheCloudStackClient.adjustUrlIfNeeded("https://cloud.domain.com/client");
        Assert.assertEquals(cloudStackUrl, urlWithSuffix);
    }

    @Test
    public void adjustUrlIfNeededTestUrlEndingWithSlashClientWithtLastSlash() {
        String urlWithSuffix = apacheCloudStackClient.adjustUrlIfNeeded("https://cloud.domain.com/client/");
        Assert.assertEquals(cloudStackUrl + "/", urlWithSuffix);
    }

    @Test
    public void appendUrlSuffixTestEndingWithSlash(){
        String urlWithSuffix = apacheCloudStackClient.appendUrlSuffix("https://cloud.domain.com/");
        Assert.assertEquals(cloudStackUrl, urlWithSuffix);
    }

    @Test
    public void appendUrlSuffixTestEndingWithoutSlash() {
        String urlWithSuffix = apacheCloudStackClient.appendUrlSuffix("https://cloud.domain.com");
        Assert.assertEquals(cloudStackUrl, urlWithSuffix);
    }

    @Test
    public void executeRequestTest() throws ClientProtocolException, IOException {
        configureMocksExecuteTestAndVerifyForMethodExecuteRequest(200);
    }

    @Test(expected = ApacheCloudStackClientRequestRuntimeException.class)
    public void executeRequestTestRequestStatusDifferentFromHttpStatusOk() throws ClientProtocolException, IOException {
        configureMocksExecuteTestAndVerifyForMethodExecuteRequest(500);
    }

    private void configureMocksExecuteTestAndVerifyForMethodExecuteRequest(int requestStatusCode) throws IOException, ClientProtocolException {
        Mockito.doReturn(cloudStackUrl).when(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class), Mockito.eq(true));

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        HttpContext httpContextMock = Mockito.mock(HttpContext.class);
        CloseableHttpResponse closeableHttpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);

        Mockito.when(statusLineMock.getStatusCode()).thenReturn(requestStatusCode);
        Mockito.when(closeableHttpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class), Mockito.eq(httpContextMock))).thenReturn(closeableHttpResponseMock);
        Mockito.when(apacheCloudStackClient.createHttpClient()).thenReturn(httpClientMock);
        Mockito.when(apacheCloudStackClient.apacheCloudStackUser.getApiKey()).thenReturn("apiKey");
        Mockito.doReturn(httpContextMock).when(apacheCloudStackClient).createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeeded(Mockito.eq(httpClientMock),
                Mockito.eq(true));
        String responseString = "responseAsString";
        Mockito.doReturn(responseString).when(apacheCloudStackClient).getResponseAsString(Mockito.eq(closeableHttpResponseMock));

        String returnOfExecuteRequest = apacheCloudStackClient.executeRequest(Mockito.mock(ApacheCloudStackRequest.class));
        Assert.assertEquals(responseString, returnOfExecuteRequest);

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient, httpClientMock, closeableHttpResponseMock, statusLineMock);
        inOrder.verify(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class), Mockito.eq(true));
        inOrder.verify(apacheCloudStackClient).createHttpClient();
        inOrder.verify(httpClientMock).execute(Mockito.any(HttpGet.class), Mockito.eq(httpContextMock));
        inOrder.verify(closeableHttpResponseMock).getStatusLine();
        inOrder.verify(statusLineMock).getStatusCode();
        inOrder.verify(httpClientMock).close();
    }

    @Test(expected = ApacheCloudStackClientRuntimeException.class)
    public void executeRequestTestExceptionWhenExecutingRequest() throws IOException, ClientProtocolException {
        Mockito.doReturn(cloudStackUrl).when(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class), Mockito.eq(true));

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class))).thenThrow(new IOException());
        Mockito.when(apacheCloudStackClient.createHttpClient()).thenReturn(httpClientMock);

        apacheCloudStackClient.executeRequest(Mockito.mock(ApacheCloudStackRequest.class));
        Mockito.verify(httpClientMock).close();
    }

    @Test
    public void createHttpClientTestValidateServerHttpsCertificateTrue() {
        configureExecuteAndVerifyTestForCreateHttpClient(true, 0);
    }

    @Test
    public void createHttpClientTestValidateServerHttpsCertificateFalse() {
        configureExecuteAndVerifyTestForCreateHttpClient(false, 1);
    }

    private void configureExecuteAndVerifyTestForCreateHttpClient(boolean shouldVerifyServerCertificates, int numberOfCreateUnsecureSslFactoryCalls) {
        apacheCloudStackClient.validateServerHttpsCertificate = shouldVerifyServerCertificates;
        CloseableHttpClient httpClient = apacheCloudStackClient.createHttpClient();

        Assert.assertNotNull(httpClient);
        Mockito.verify(apacheCloudStackClient, Mockito.times(numberOfCreateUnsecureSslFactoryCalls)).createInsecureSslFactory();
    }

    @Test
    public void getResponseAsStringTest() throws UnsupportedOperationException, IOException {
        String responseTest = "teste response";

        CloseableHttpResponse closeableHttpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);

        InputStream is = new ByteArrayInputStream(responseTest.getBytes());

        Mockito.doReturn(is).when(httpEntityMock).getContent();
        Mockito.doReturn(httpEntityMock).when(closeableHttpResponseMock).getEntity();

        String responseAsString = apacheCloudStackClient.getResponseAsString(closeableHttpResponseMock);

        Assert.assertEquals(responseTest, responseAsString);
    }

    @Test
    public void createApacheCloudStackApiUrlRequestTestWithSignature() {
        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);

        String queryString = "queryString";
        Mockito.doReturn(queryString).when(apacheCloudStackClient).createCommandString(Mockito.eq(apacheCloudStackRequestMock));

        String signatureValue = "signatureValue";
        Mockito.doReturn(signatureValue).when(apacheCloudStackClient).createSignature(Mockito.eq(queryString));

        String urlRequestReturned = apacheCloudStackClient.createApacheCloudStackApiUrlRequest(apacheCloudStackRequestMock, true);
        Assert.assertEquals(cloudStackUrl + "/api?" + queryString + "&signature=" + signatureValue, urlRequestReturned);

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient);
        inOrder.verify(apacheCloudStackClient).createCommandString(Mockito.eq(apacheCloudStackRequestMock));
        inOrder.verify(apacheCloudStackClient).createSignature(Mockito.eq(queryString));
        inOrder.verify(apacheCloudStackClient).getUrlEncodedValue(Mockito.eq(signatureValue));
    }

    @Test
    public void createApacheCloudStackApiUrlRequestTestWithoutSignature() {
        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);

        String queryString = "queryString";
        Mockito.doReturn(queryString).when(apacheCloudStackClient).createCommandString(Mockito.eq(apacheCloudStackRequestMock));

        String urlRequestReturned = apacheCloudStackClient.createApacheCloudStackApiUrlRequest(apacheCloudStackRequestMock, false);
        Assert.assertEquals(cloudStackUrl + "/api?" + queryString, urlRequestReturned);

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient);
        inOrder.verify(apacheCloudStackClient).createCommandString(Mockito.eq(apacheCloudStackRequestMock));
        inOrder.verify(apacheCloudStackClient, Mockito.times(0)).createSignature(Mockito.eq(queryString));
        inOrder.verify(apacheCloudStackClient, Mockito.times(0)).getUrlEncodedValue(Mockito.anyString());
    }

    @Test
    public void createSignatureTest() {
        String expectedSignature = "wqtSA/cANcWnWlh/ukfnaExyM54=";
        Mockito.doReturn("secretKey").when(apacheCloudStackUser).getSecretKey();

        String signature = apacheCloudStackClient.createSignature("queryString");

        Assert.assertEquals(expectedSignature, signature);
    }

    @Test
    public void createCommandStringTest() {
        List<ApacheCloudStackApiCommandParameter> params = new ArrayList<>();
        String valueParam1 = "value1";
        String valueParam2 = "value2 espaço";
        params.add(new ApacheCloudStackApiCommandParameter("param1", valueParam1));
        params.add(new ApacheCloudStackApiCommandParameter("param2", valueParam2));

        String expectedCommandSrting = "param1=value1&param2=value2 espaço";

        ApacheCloudStackRequest ApacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);
        Mockito.doReturn(params).when(apacheCloudStackClient).createSortedCommandQueryList(Mockito.eq(ApacheCloudStackRequestMock));
        Mockito.doReturn(valueParam1).when(apacheCloudStackClient).getUrlEncodedValue(Mockito.eq(valueParam1));
        Mockito.doReturn(valueParam2).when(apacheCloudStackClient).getUrlEncodedValue(Mockito.eq(valueParam2));

        String commandStringReturned = apacheCloudStackClient.createCommandString(ApacheCloudStackRequestMock);
        Assert.assertEquals(expectedCommandSrting, commandStringReturned);

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient);
        inOrder.verify(apacheCloudStackClient).createSortedCommandQueryList(Mockito.eq(ApacheCloudStackRequestMock));
        inOrder.verify(apacheCloudStackClient).getUrlEncodedValue(Mockito.eq(valueParam1));
        inOrder.verify(apacheCloudStackClient).getUrlEncodedValue(Mockito.eq(valueParam2));

    }

    @Test
    public void getUrlEncodedValueTestNullValue() {
        String urlEncodedValueReturned = apacheCloudStackClient.getUrlEncodedValue(null);
        Assert.assertEquals("null", urlEncodedValueReturned);
    }

    @Test
    public void getUrlEncodedValueTestNonSpecialCharactersValue() {
        String urlEncodedValueReturned = apacheCloudStackClient.getUrlEncodedValue("test");
        Assert.assertEquals("test", urlEncodedValueReturned);
    }

    @Test
    public void getUrlEncodedValueTestValueWithSpaces() {
        String urlEncodedValueReturned = apacheCloudStackClient.getUrlEncodedValue("test value");
        Assert.assertEquals("test%20value", urlEncodedValueReturned);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createSortedCommandQueryListTestWithApiKey() {
        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);
        Set<ApacheCloudStackApiCommandParameter> params = new HashSet<>();

        params.add(new ApacheCloudStackApiCommandParameter("param1", "value1"));
        Mockito.doReturn(params).when(apacheCloudStackRequestMock).getParameters();
        Mockito.when(apacheCloudStackClient.apacheCloudStackUser.getApiKey()).thenReturn("apiKey");
        Mockito.doNothing().when(apacheCloudStackClient).configureRequestExpiration(Mockito.anyList());

        List<ApacheCloudStackApiCommandParameter> sortedCommandQueryList = apacheCloudStackClient.createSortedCommandQueryList(apacheCloudStackRequestMock);

        Mockito.verify(apacheCloudStackRequestMock).getParameters();
        Mockito.verify(apacheCloudStackClient).configureRequestExpiration(Mockito.anyList());

        Assert.assertEquals(3, sortedCommandQueryList.size());
        Assert.assertEquals("apiKey", sortedCommandQueryList.get(0).getName());
        Assert.assertEquals("command", sortedCommandQueryList.get(1).getName());
        Assert.assertEquals("param1", sortedCommandQueryList.get(2).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createSortedCommandQueryListTestWithourApiKey() {
        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);
        Set<ApacheCloudStackApiCommandParameter> params = new HashSet<>();

        params.add(new ApacheCloudStackApiCommandParameter("param1", "value1"));
        Mockito.doReturn(params).when(apacheCloudStackRequestMock).getParameters();
        Mockito.when(apacheCloudStackClient.apacheCloudStackUser.getApiKey()).thenReturn(null);
        Mockito.doNothing().when(apacheCloudStackClient).configureRequestExpiration(Mockito.anyList());

        List<ApacheCloudStackApiCommandParameter> sortedCommandQueryList = apacheCloudStackClient.createSortedCommandQueryList(apacheCloudStackRequestMock);

        Mockito.verify(apacheCloudStackRequestMock).getParameters();
        Mockito.verify(apacheCloudStackClient).configureRequestExpiration(Mockito.anyList());

        Assert.assertEquals(2, sortedCommandQueryList.size());
        Assert.assertEquals("command", sortedCommandQueryList.get(0).getName());
        Assert.assertEquals("param1", sortedCommandQueryList.get(1).getName());
    }

    @Test
    public void executeRequestAutoConvertReturnToJavaObject() {
        String jsonTest = "{attr1: 1, attr2: 'test'}";

        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);
        Mockito.doReturn(jsonTest).when(apacheCloudStackClient).executeRequest(Mockito.eq(apacheCloudStackRequestMock));

        TestExecuteReturnObject returnedObject = apacheCloudStackClient.executeRequest(apacheCloudStackRequestMock, TestExecuteReturnObject.class);

        Mockito.verify(apacheCloudStackClient).executeRequest(Mockito.eq(apacheCloudStackRequestMock));

        Assert.assertNotNull(returnedObject);
        Assert.assertTrue(returnedObject instanceof TestExecuteReturnObject);

        Assert.assertEquals(1, returnedObject.attr1);
        Assert.assertEquals("test", returnedObject.attr2);

    }

    private class TestExecuteReturnObject {
        private int attr1;
        private String attr2;
    }

    @Test
    public void createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeededTestAuthenticationWithApiKeyAndSecretKey() {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        HttpContext basicHttpContext = apacheCloudStackClient.createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeeded(closeableHttpClientMock, true);

        Mockito.verify(apacheCloudStackClient, Mockito.times(0)).createHttpContextWithAuthenticatedSessionUsingUserCredentials(Mockito.any(closeableHttpClientMock.getClass()));
        Assert.assertNotNull(basicHttpContext);
    }

    @Test
    public void createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeededTestAuthenticationWithUsernameAndPassword() {
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        HttpContext httpContextMock = Mockito.mock(HttpContext.class);
        Mockito.doReturn(httpContextMock).when(apacheCloudStackClient).createHttpContextWithAuthenticatedSessionUsingUserCredentials(Mockito.any(closeableHttpClientMock.getClass()));
        HttpContext basicHttpContext = apacheCloudStackClient.createHttpContextWithAuthenticatedSessionUsingUserCredentialsIfNeeded(closeableHttpClientMock, false);

        Mockito.verify(apacheCloudStackClient, Mockito.times(1)).createHttpContextWithAuthenticatedSessionUsingUserCredentials(Mockito.eq(closeableHttpClientMock));

        Assert.assertNotNull(basicHttpContext);
        Assert.assertEquals(httpContextMock, basicHttpContext);
    }

    @Test
    public void createHttpContextWithAuthenticatedSessionUsingUserCredentialsTest() throws ClientProtocolException, IOException {
        HttpPost httpPostMock = Mockito.mock(HttpPost.class);
        Mockito.doReturn(httpPostMock).when(apacheCloudStackClient).createHttpPost();

        NameValuePair nameValuePairMock = Mockito.mock(NameValuePair.class);
        ArrayList<NameValuePair> paramsMock = new ArrayList<>(1);
        paramsMock.add(nameValuePairMock);

        Mockito.doReturn(paramsMock).when(apacheCloudStackClient).getParametersForLogin();

        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        CloseableHttpResponse closeableHttpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doReturn(200).when(statusLineMock).getStatusCode();
        Mockito.doReturn(statusLineMock).when(closeableHttpResponseMock).getStatusLine();
        Mockito.doReturn(closeableHttpResponseMock).when(httpClientMock).execute(Mockito.eq(httpPostMock));

        HttpContext httpContextMock = Mockito.mock(HttpContext.class);
        Mockito.doReturn(httpContextMock).when(apacheCloudStackClient).createHttpContextWithCookies(Mockito.eq(closeableHttpResponseMock));
        Mockito.doReturn("responseAsString").when(apacheCloudStackClient).getResponseAsString(Mockito.eq(closeableHttpResponseMock));

        HttpContext httpContextAuthenticatedWithUsernamePassword = apacheCloudStackClient.createHttpContextWithAuthenticatedSessionUsingUserCredentials(httpClientMock);

        Assert.assertEquals(httpContextMock, httpContextAuthenticatedWithUsernamePassword);

        InOrder inOrder = Mockito.inOrder(httpPostMock, apacheCloudStackClient, statusLineMock, closeableHttpResponseMock, httpClientMock);
        inOrder.verify(apacheCloudStackClient).createHttpPost();
        inOrder.verify(apacheCloudStackClient).getParametersForLogin();
        inOrder.verify(httpPostMock).setEntity(Mockito.any(UrlEncodedFormEntity.class));
        inOrder.verify(httpClientMock).execute(Mockito.eq(httpPostMock));
        inOrder.verify(closeableHttpResponseMock).getStatusLine();
        inOrder.verify(statusLineMock).getStatusCode();
        inOrder.verify(apacheCloudStackClient).getResponseAsString(Mockito.eq(closeableHttpResponseMock));
        inOrder.verify(apacheCloudStackClient).createHttpContextWithCookies(Mockito.eq(closeableHttpResponseMock));
    }

    @Test(expected = ApacheCloudStackClientRuntimeException.class)
    public void createHttpContextWithAuthenticatedSessionUsingUserCredentialsTestExceptionExecutingRequest() throws ClientProtocolException, IOException {
        HttpPost httpPostMock = Mockito.mock(HttpPost.class);
        Mockito.doReturn(httpPostMock).when(apacheCloudStackClient).createHttpPost();

        NameValuePair nameValuePairMock = Mockito.mock(NameValuePair.class);
        ArrayList<NameValuePair> paramsMock = new ArrayList<>(1);
        paramsMock.add(nameValuePairMock);

        Mockito.doReturn(paramsMock).when(apacheCloudStackClient).getParametersForLogin();

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doThrow(IOException.class).when(httpClientMock).execute(Mockito.eq(httpPostMock));

        apacheCloudStackClient.createHttpContextWithAuthenticatedSessionUsingUserCredentials(httpClientMock);
    }

    @Test
    public void createHttpContextWithCookiesTest() {
        CloseableHttpResponse closeableHttpResponseMock = Mockito.mock(CloseableHttpResponse.class);

        Header[] headers = new Header[1];
        Mockito.doReturn(headers).when(closeableHttpResponseMock).getAllHeaders();

        Mockito.doNothing().when(apacheCloudStackClient).createAndAddCookiesOnStoreForHeaders(Mockito.any(BasicCookieStore.class), Mockito.eq(headers));
        HttpContext httpContextWithCookies = apacheCloudStackClient.createHttpContextWithCookies(closeableHttpResponseMock);

        Assert.assertNotNull(httpContextWithCookies);
        Assert.assertNotNull(httpContextWithCookies.getAttribute(HttpClientContext.COOKIE_STORE));

        Mockito.verify(apacheCloudStackClient).createAndAddCookiesOnStoreForHeaders(Mockito.any(BasicCookieStore.class), Mockito.eq(headers));

    }

    @Test
    public void createAndAddCookiesOnStoreForHeadersTest() {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("Set-Cookie cookieName", "value1");
        headers[1] = new BasicHeader("anyOtherCookie", "value2");

        CookieStore cookieStoreMock = Mockito.mock(CookieStore.class);

        Mockito.doNothing().when(apacheCloudStackClient).createAndAddCookiesOnStoreForHeader(Mockito.eq(cookieStoreMock), Mockito.any(Header.class));
        apacheCloudStackClient.createAndAddCookiesOnStoreForHeaders(cookieStoreMock, headers);

        Mockito.verify(apacheCloudStackClient).createAndAddCookiesOnStoreForHeader(Mockito.eq(cookieStoreMock), Mockito.eq(headers[0]));
    }

    public void createAndAddCookiesOnStoreForHeaderTest() {
        CookieStore cookieStoreMock = Mockito.mock(CookieStore.class);
        Header headerMock = Mockito.mock(Header.class);

        HeaderElement headerElementMock1 = Mockito.mock(HeaderElement.class);

        List<HeaderElement> headerElements = new ArrayList<>(1);
        headerElements.add(headerElementMock1);

        Mockito.doReturn(headerElements).when(headerMock).getElements();

        Cookie cookieMock = Mockito.mock(Cookie.class);
        Mockito.doReturn(cookieMock).when(apacheCloudStackClient).createCookieForHeaderElement(Mockito.eq(headerElementMock1));

        apacheCloudStackClient.createAndAddCookiesOnStoreForHeader(cookieStoreMock, headerMock);

        Mockito.verify(headerMock).getElements();
        Mockito.verify(apacheCloudStackClient).createCookieForHeaderElement(Mockito.eq(headerElementMock1));
    }

    @Test
    public void createCookieForHeaderElementTest() {
        String cookiePath = "/client/api";

        String paramName = "paramName1";
        String paramValue = "paramVale1";
        NameValuePair[] parameters = new NameValuePair[1];
        parameters[0] = new BasicNameValuePair(paramName, paramValue);

        String headerName = "headerElementName";
        String headerValue = "headerElementValue";
        HeaderElement headerElement = new BasicHeaderElement(headerName, headerValue, parameters);

        Mockito.doNothing().when(apacheCloudStackClient).configureDomainForCookie(Mockito.any(BasicClientCookie.class));

        BasicClientCookie cookieForHeaderElement = apacheCloudStackClient.createCookieForHeaderElement(headerElement);

        Assert.assertNotNull(cookieForHeaderElement);
        Assert.assertEquals(headerName, cookieForHeaderElement.getName());
        Assert.assertEquals(headerValue, cookieForHeaderElement.getValue());
        Assert.assertEquals(paramValue, cookieForHeaderElement.getAttribute(paramName));
        Assert.assertEquals(cookiePath, cookieForHeaderElement.getPath());

        Mockito.verify(apacheCloudStackClient).configureDomainForCookie(Mockito.eq(cookieForHeaderElement));
    }

    @Test
    public void configureDomainForCookieTest() {
        BasicClientCookie basicClientCookie = new BasicClientCookie("name", "value");
        apacheCloudStackClient.configureDomainForCookie(basicClientCookie);

        Assert.assertEquals(cloudStackDomain, basicClientCookie.getDomain());
    }

    @Test
    public void createHttpPostTest() throws MalformedURLException {
        HttpPost httpPost = apacheCloudStackClient.createHttpPost();

        Assert.assertEquals(cloudStackUrl + "/api", httpPost.getURI().toURL().toString());
        Assert.assertEquals("application/x-www-form-urlencoded", httpPost.getFirstHeader("Content-Type").getValue());
    }

    public void getParametersForLoginTest() {
        Mockito.doReturn("userName").when(apacheCloudStackUser).getUsername();
        Mockito.doReturn("password").when(apacheCloudStackUser).getPassword();
        Mockito.doReturn("domain").when(apacheCloudStackUser).getDomain();

        List<NameValuePair> parametersForLogin = apacheCloudStackClient.getParametersForLogin();

        Assert.assertNotNull(parametersForLogin);
        Assert.assertEquals(4, parametersForLogin.size());

        Assert.assertEquals("command", parametersForLogin.get(0).getValue());
        Assert.assertEquals("login", parametersForLogin.get(0).getName());

        Assert.assertEquals("username", parametersForLogin.get(1).getValue());
        Assert.assertEquals("userName", parametersForLogin.get(1).getName());

        Assert.assertEquals("password", parametersForLogin.get(2).getValue());
        Assert.assertEquals("password", parametersForLogin.get(2).getName());

        Assert.assertEquals("domain", parametersForLogin.get(3).getValue());
        Assert.assertEquals("domain", parametersForLogin.get(3).getName());
    }

    @Test
    public void executeUserLogoutTest() {
        String urlRequest = "urlRequest";

        Mockito.doReturn(urlRequest).when(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class), Mockito.eq(false));
        Mockito.doReturn("response").when(apacheCloudStackClient).executeRequestGetResponseAsString(Mockito.eq(urlRequest), Mockito.any(CloseableHttpClient.class), Mockito.any(HttpContext.class));

        apacheCloudStackClient.executeUserLogout(Mockito.mock(CloseableHttpClient.class), Mockito.mock(HttpContext.class));

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient);
        inOrder.verify(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class), Mockito.eq(false));
        inOrder.verify(apacheCloudStackClient).executeRequestGetResponseAsString(Mockito.eq(urlRequest), Mockito.any(CloseableHttpClient.class), Mockito.any(HttpContext.class));
    }

    @Test
    public void configureRequestExpirationTestRequestsShouldNotExpire() {
        apacheCloudStackClient.setShouldRequestsExpire(false);

        ArrayList<ApacheCloudStackApiCommandParameter> arrayList = new ArrayList<>();
        apacheCloudStackClient.configureRequestExpiration(arrayList);

        Assert.assertEquals(0, arrayList.size());
    }

    @Test
    public void configureRequestExpirationTestRequestsShouldNotExpireUsingOverride() {
        apacheCloudStackClient.setShouldRequestsExpire(false);

        ArrayList<ApacheCloudStackApiCommandParameter> arrayList = new ArrayList<>();
        arrayList.add(new ApacheCloudStackApiCommandParameter("expires", "2011-10-10T12:00:00+0530"));

        apacheCloudStackClient.configureRequestExpiration(arrayList);

        Assert.assertEquals(2, arrayList.size());
        Mockito.verify(apacheCloudStackClient, Mockito.never()).createExpirationDate();
    }

    @Test
    public void configureRequestExpirationTestRequestsShouldExpireUsingOverride() {
        apacheCloudStackClient.setShouldRequestsExpire(true);

        ArrayList<ApacheCloudStackApiCommandParameter> arrayList = new ArrayList<>();
        ApacheCloudStackApiCommandParameter expirationParameter = new ApacheCloudStackApiCommandParameter("expires", "2011-10-10T12:00:00+0530");
        arrayList.add(expirationParameter);

        apacheCloudStackClient.configureRequestExpiration(arrayList);

        Assert.assertEquals(2, arrayList.size());
        Assert.assertEquals(expirationParameter, arrayList.get(0));
        Mockito.verify(apacheCloudStackClient, Mockito.never()).createExpirationDate();
    }

    @Test
    public void configureRequestExpirationTestRequestsShouldExpireWithoutOverride() {
        apacheCloudStackClient.setShouldRequestsExpire(true);

        ArrayList<ApacheCloudStackApiCommandParameter> arrayList = new ArrayList<>();

        String expirationDate = "2011-10-10T12:00:00+0530";
        Mockito.doReturn(expirationDate).when(apacheCloudStackClient).createExpirationDate();

        apacheCloudStackClient.configureRequestExpiration(arrayList);

        Assert.assertEquals(2, arrayList.size());
        Assert.assertEquals("signatureVersion", arrayList.get(0).getName());
        Assert.assertEquals(3, arrayList.get(0).getValue());
        Assert.assertEquals("expires", arrayList.get(1).getName());
        Assert.assertEquals(expirationDate, arrayList.get(1).getValue());

        Mockito.verify(apacheCloudStackClient).createExpirationDate();
    }

    @Test
    public void createExpirationDateTest() {
        Calendar someMomentInTimeSpace = Calendar.getInstance();
        someMomentInTimeSpace.set(1999, 12, 31, 23, 59, 59);
        someMomentInTimeSpace.set(Calendar.MILLISECOND, 0);
        Mockito.doReturn(someMomentInTimeSpace.getTime()).when(apacheCloudStackClient).getExpirationDate();

        String expirationDate = apacheCloudStackClient.createExpirationDate();

        String expectedExpirationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(someMomentInTimeSpace.getTime());
        Assert.assertEquals(expectedExpirationDate, expirationDate);

        Mockito.verify(apacheCloudStackClient).getExpirationDate();
    }
}
