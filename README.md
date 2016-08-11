# apache-cloudstack-java-client
This project facilitates the integration of Java applications with Apache CloudStack through its API. It is a Java framework that enables the execution of requests to Apache CloudStack API. If you want to write only a simple script I recommend you using <a href="https://github.com/apache/cloudstack-cloudmonkey">CloudMonkey</a>, instead of this framework. However, if you have the need to write a Java application that has to consume Apache CloudStack API, you are welcome to use this framework.

You can find examples on how to use the framework at <a href="https://github.com/Autonomiccs/apache-cloudstack-java-client/tree/master/samples/apache-cloudstack-client-examples">examples</a>. The usage is as simple as that:

```java

        String secretKey = "<secretKey>";
        String apiKey = "<apiKey>";
        String cloudStackUrl = "https://cloud.domain.com/client";

        ApacheCloudStackUser apacheCloudStackUser = new ApacheCloudStackUser(secretKey, apiKey);
        ApacheCloudStackClient apacheCloudStackClient = new ApacheCloudStackClient(cloudStackUrl, apacheCloudStackUser);

        ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest("listClusters");
        apacheCloudStackRequest.addParameter("response", "json");
        apacheCloudStackRequest.addParameter("name", "clusterName");

        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        System.out.println(response);
```

# License
 Apache CloudStack Java Client
 
 Copyright (C) 2016 Autonomiccs, Inc.

 Licensed to the Autonomiccs, Inc. under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
