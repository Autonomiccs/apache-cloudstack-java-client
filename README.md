# apache-cloudstack-java-client
## Project status: [![Build Status](http://jenkinsbadge.autonomiccs.com.br/buildStatus/icon?job=Apache-cloudstack-java-client)](http://jenkins.autonomiccs.com.br/job/Apache-cloudstack-java-client/)<a style="padding-right: 2px; padding-left: 1px;" href="https://sonar.autonomiccs.com.br/"><img alt="Sonar quality gate" src="http://sonarbadges.autonomiccs.com.br/api/badges/gate?key=apache-cloudstack-java-client"/></a><a href="https://sonar.autonomiccs.com.br/" style="padding-right: 2px;" ><img alt="Code coverage" src="http://sonarbadges.autonomiccs.com.br/api/badges/measure?key=apache-cloudstack-java-client&metric=overall_coverage"/></a><a href="https://sonar.autonomiccs.com.br/" style="padding-right: 2px;" ><img alt="duplicated lines density" src="http://sonarbadges.autonomiccs.com.br/api/badges/measure?key=apache-cloudstack-java-client&metric=duplicated_lines_density"/></a><a href="https://sonar.autonomiccs.com.br/" style="padding-right: 2px;" ><img alt="bugs" src="http://sonarbadges.autonomiccs.com.br/api/badges/measure?key=apache-cloudstack-java-client&metric=bugs"/></a> 
This project facilitates the integration of Java applications with Apache CloudStack through its API. It is a Java framework that enables the execution of requests to Apache CloudStack API. If you want to write only a simple script I recommend you using <a href="https://github.com/apache/cloudstack-cloudmonkey">CloudMonkey</a>, instead of this framework. However, if you have the need to write a Java application that has to consume Apache CloudStack API, you are welcome to use this framework.
 
You can find examples on how to use the framework at <a href="https://github.com/Autonomiccs/apache-cloudstack-java-client/tree/master/samples/apache-cloudstack-client-examples">examples</a>. The usage is as simple as that:

```java

        String secretKey = "<secretKey>";
        String apiKey = "<apiKey>";
        String cloudStackUrl = "https://cloud.domain.com/client/api";

        ApacheCloudStackUser apacheCloudStackUser = new ApacheCloudStackUser(secretKey, apiKey);
        ApacheCloudStackClient apacheCloudStackClient = new ApacheCloudStackClient(cloudStackUrl, apacheCloudStackUser);

        ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest("listClusters");
        apacheCloudStackRequest.addParameter("response", "json");
        apacheCloudStackRequest.addParameter("name", "clusterName");

        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        System.out.println(response);
```

The component is on Maven central repo; to use it, you only need to add the following piece of code to your pom.xml:

```
<dependency>
    <groupId>br.com.autonomiccs</groupId>
    <artifactId>apache-cloudstack-java-client</artifactId>
    <version>1.0.4</version>
</dependency>
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
