package br.com.autonomiccs.apacheCloudStack.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.apacheCloudStack.ApacheCloudStackClientRequestRuntimeException;
import br.com.autonomiccs.apacheCloudStack.ApacheCloudStackClientRuntimeException;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;

@RunWith(MockitoJUnitRunner.class)
public class ApacheCloudStackClientTest {

    private ApacheCloudStackClient apacheCloudStackClient;

    @Mock
    private ApacheCloudStackUser apacheCloudStackUser;

    private String cloudStackUrl = "https://cloud.domain.com/client";

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
        Mockito.doReturn(cloudStackUrl).when(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class));

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse closeableHttpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);

        Mockito.when(statusLineMock.getStatusCode()).thenReturn(requestStatusCode);
        Mockito.when(closeableHttpResponseMock.getStatusLine()).thenReturn(statusLineMock);
        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class))).thenReturn(closeableHttpResponseMock);
        Mockito.when(apacheCloudStackClient.createHttpClient()).thenReturn(httpClientMock);

        String responseString = "responseAsString";
        Mockito.doReturn(responseString).when(apacheCloudStackClient).getResponseAsString(Mockito.eq(closeableHttpResponseMock));

        String returnOfExecuteRequest = apacheCloudStackClient.executeRequest(Mockito.mock(ApacheCloudStackRequest.class));
        Assert.assertEquals(responseString, returnOfExecuteRequest);

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient, httpClientMock, closeableHttpResponseMock, statusLineMock);
        inOrder.verify(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class));
        inOrder.verify(apacheCloudStackClient).createHttpClient();
        inOrder.verify(httpClientMock).execute(Mockito.any(HttpGet.class));
        inOrder.verify(closeableHttpResponseMock).getStatusLine();
        inOrder.verify(statusLineMock).getStatusCode();
    }

    @Test(expected = ApacheCloudStackClientRuntimeException.class)
    public void executeRequestTestExceptionWhenExecutingRequest() throws IOException, ClientProtocolException {
        Mockito.doReturn(cloudStackUrl).when(apacheCloudStackClient).createApacheCloudStackApiUrlRequest(Mockito.any(ApacheCloudStackRequest.class));

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class))).thenThrow(new IOException());
        Mockito.when(apacheCloudStackClient.createHttpClient()).thenReturn(httpClientMock);

        apacheCloudStackClient.executeRequest(Mockito.mock(ApacheCloudStackRequest.class));
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
        Mockito.verify(apacheCloudStackClient, Mockito.times(numberOfCreateUnsecureSslFactoryCalls)).createUnsecureSslFactory();
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
    public void createApacheCloudStackApiUrlRequestTest() {
        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);

        String queryString = "queryString";
        Mockito.doReturn(queryString).when(apacheCloudStackClient).createCommandString(Mockito.eq(apacheCloudStackRequestMock));

        String signatureValue = "signatureValue";
        Mockito.doReturn(signatureValue).when(apacheCloudStackClient).createSignature(Mockito.eq(queryString));

        String urlRequestReturned = apacheCloudStackClient.createApacheCloudStackApiUrlRequest(apacheCloudStackRequestMock);
        Assert.assertEquals(cloudStackUrl + "/api?" + queryString + "&signature=" + signatureValue, urlRequestReturned);

        InOrder inOrder = Mockito.inOrder(apacheCloudStackClient);
        inOrder.verify(apacheCloudStackClient).createCommandString(Mockito.eq(apacheCloudStackRequestMock));
        inOrder.verify(apacheCloudStackClient).createSignature(Mockito.eq(queryString));
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
    public void createSortedCommandQueryListTest() {
        ApacheCloudStackRequest apacheCloudStackRequestMock = Mockito.mock(ApacheCloudStackRequest.class);
        Set<ApacheCloudStackApiCommandParameter> params = new HashSet<>();

        params.add(new ApacheCloudStackApiCommandParameter("param1", "value1"));
        Mockito.doReturn(params).when(apacheCloudStackRequestMock).getParameters();

        List<ApacheCloudStackApiCommandParameter> sortedCommandQueryList = apacheCloudStackClient.createSortedCommandQueryList(apacheCloudStackRequestMock);

        Mockito.verify(apacheCloudStackRequestMock).getParameters();

        Assert.assertEquals(3, sortedCommandQueryList.size());
        Assert.assertEquals("apiKey", sortedCommandQueryList.get(0).getName());
        Assert.assertEquals("command", sortedCommandQueryList.get(1).getName());
        Assert.assertEquals("param1", sortedCommandQueryList.get(2).getName());
    }

    @Test
    public void executeRequestAutoConverReturnToJavaObject() {
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
}
