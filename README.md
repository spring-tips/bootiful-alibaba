# Spring Cloud ALibaba Notes

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


## Nacos 
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

## ACM 
* ACM: goto alibabacloud.com -> Products > Middleware > Application Configutation Management 

