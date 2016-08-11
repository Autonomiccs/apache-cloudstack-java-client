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
