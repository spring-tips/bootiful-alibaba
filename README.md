# Spring Cloud ALibaba Notes

https://nacos.io/en-us/docs/quick-start.html

<!-- goals -->
<!-- - rocketmq (next version)  -->
- sentinel (not english)
- nacos 
- ACM (application configuration management - only on Alibaba Cloud)
- OSS (object store service - only on Alibaba Cloud)


<!-- notes -->
* download Sentinel (how?) (regular users clone the alibaba/Sentinel and `mvn clean package -f sentinel-dashboard/pom.xml `)
* download Nacos.io (where?) ( use Java 8 and then $NACOS/bin/startup.sh -m standalone)
* git clone Spring Cloud Alibaba 
* git clone alibaba/Sentinel. Do `mvn clean install` for the `sentinel-dashboard` module. if ur behind the GFW u will need the following `settings.xml`:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                  https://maven.apache.org/xsd/settings-1.0.0.xsd">
<mirrors><mirror>
<id>alimaven</id>
<name>aliyun maven</name>
<url>http://maven.aliyun.com/nexus/content/groups/public/</url>
<mirrorOf>central</mirrorOf>
</mirror></mirrors>
</settings>
```

## Nacos 
* NaCos = Naming & Config Service
* port 8848 because Mt. Everest's height is 8848M tall (LOL)
* start nacos: `$NACOS_HOME/bin/startup.sh -m standalone`
* visit nacos at http://localhost:8848/nacos/#/configurationManagement?dataId=&group=&appName=&namespace= 
* make sure to choose 'En'!
* goto spring-cloud-alibaba/s-c-a-examples/nacos/discovery
* mvn spring-boot:run 
* nacos can do both configuration and discovery (  <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery
            </artifactId>
        </dependency>)

* u can change the configuration in the nacos config management and itll be instantly refreshed in the client apps and u can listen tot he RefreshedScopedRefreshEvent just as u would with spring cloud config server. 
* http://localhost:62475/actuator/nacos-config

* u can add Sentinel FlowRules to Nacos itself: https://github.com/alibaba/sentinel/blob/master/sentinel-demo/sentinel-demo-dynamic-file-rule/src/main/resources/FlowRule.json (customized to reflect the resource in this client app, `/nihao`)
  see the following image: adding-flow-rules-from-sentinel-to-nacos-so-that-they-apply-to-other-microservices.png
* add the following to the client that wants this flow rules to be honored: 
    <dependency>
       <groupId>com.alibaba.csp</groupId>
       <artifactId>sentinel-datasource-nacos</artifactId>
       <version>1.3.0-GA</version>
    </dependency>
* add the following properties to tell the client to laod any flow rules from Naco: 
    spring.cloud.sentinel.datasource.type=nacos
    spring.cloud.sentinel.datasource.serverAddr=localhost:8848
    spring.cloud.sentinel.datasource.groupId=DEFAULT_GROUP
    spring.cloud.sentinel.datasource.dataId=sentinel-flow-rules.json
    spring.cloud.sentinel.datasource.converter=flowConverter
* the `flowConverter` is: 
    
    ```  
    @Component("flowConverter")
    class FlowConverter implements Converter<String, List<FlowRule>> {
    
        @Override
        public List<FlowRule> convert(String source) {
            return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
            });
        }
    }
    ``` 
    
* ```	@SentinelDataSource("spring.cloud.sentinel.datasource") private ReadableDataSource readableDataSource; ```

## OSS (object storeage service)
* OSS: goto alibabacloud.com -> Products > Middleware > Storage & CDN > Object Storage Service
git@github.com:spring-tips/bootiful-alibaba.git

* add ```<dependency> <groupId>org.springframework.cloud</groupId> <artifactId>spring-cloud-starter-alicloud-oss</artifactId> </dependency>``` 
* add 
    
    ```
    spring.cloud.alicloud.access-key=AK
    spring.cloud.alicloud.secret-key=SK
    spring.cloud.alicloud.oss.endpoint=***.aliyuncs.com
    ```

* goto alibabacloud.com, goto profile in top right (with ur avatar), choose 'Access Key', then download it. 
 
*  Regionsin Alibaba Cloud https://www.alibabacloud.com/help/doc-detail/31837.htm?spm=a2c63.l28256.a3.26.11515139ShvIYU 
