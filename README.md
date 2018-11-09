# Bootiful Alibaba 

## Dependencies 

* 	`org.springframework.cloud`:`spring-cloud-alibaba-dependencies`:`0.2.0.RELEASE`:`pom`:`import`
* 	`org.springframework.cloud`:`spring-cloud-starter-alibaba-nacos-config`
* 	`org.springframework.cloud`:`spring-cloud-starter-alibaba-nacos-discovery`
* 	`org.springframework.cloud`:`spring-cloud-starter-alibaba-sentinel`
* 	`org.springframework.cloud`:`spring-cloud-starter-alicloud-oss`

Are u having trouble downloading those dependencies? They're all in Maven central, but sometimes - depending on where you are in the world - Maven central can be a little slow. You _might_ want to try this Alibaba Maven repository in your `~/.m2/settings.xml`:

```
<settings 
	xmlns="http://maven.apache.org/SETTINGS/1.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
	<mirrors><mirror>
	<id>alimaven</id>
	<name>aliyun maven</name>
	<url>http://maven.aliyun.com/nexus/content/groups/public/</url>
	<mirrorOf>central</mirrorOf>
	</mirror></mirrors>
</settings>
```

## Sentinel 

* 	Alibaba faced scale problems few others could hope to appreciate w/ 11/11
* 	Sentinel is the tool that helps them address that issue 
* 	it defines flow control rules 
* 	you can define these rules in code...

	```
	@Configuration
	class SentinelCodeConfiguration {

		@EventListener(ApplicationReadyEvent.class)
		public void configureSentinel() {
			FlowRule flowRule = new FlowRule();
			flowRule.setResource("/code");
			flowRule.setCount(2);
			flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
			flowRule.setLimitApp("default");
			FlowRuleManager.loadRules(Collections.singletonList(flowRule));
		}
	}

	```
* 	or in sentinel's dashboard, which you can download in English! (https://github.com/alibaba/Sentinel/releases). Run it and visit it.

*	once u have the sentinel application up and running u can respond to 'blocks' in a number of ways. One is to use a property, e.g.: `spring.cloud.sentinel.servlet.block-page=/eek`. This will route the `/eek` endpoint. 
* 	another way to handle this is to use a `UrlBlockHandler`:
	```
	@Log4j2
	@Component
	class CustomUrlBlockHandler implements UrlBlockHandler {

		@Override
		public void blocked(
			HttpServletRequest request,
			HttpServletResponse response,
			BlockException be) throws IOException {

			response
				.getWriter()
				.append(">_<".trim());

		}
	}
	```

* 	a THIRD alternative is to store the configuration elsewhere. One great place to store them is in a configuration server using a custom `SentinelDataSource`. let's look at Nacos as a thing that could support this. Were not going to use it in this way, but we WILL look at Nacos to store other configuration.

## Nacos 

* 	Nacos means 'NAming and COnfiguration Server'
* 	it supports service registration and discovery. 
*	You'll need to point your app to where Nacos lives for both config and service-and-registration purpose: 
	
	```
	nacos=localhost:8848
	spring.cloud.nacos.config.server-addr=${nacos}
	spring.cloud.nacos.discovery.server-addr=${nacos}
	```

* 	Nacos also has actuator endpoints. Enable them: `management.endpoints.web.exposure.include=*` and `management.endpoint.health.show-details=always`. then you can visit the Nacos actuator endpoint at `http://localhost:8080/actuator/nacos-config`.
*	Nacos runs on port 8848 because, and this made me laugh, Mt. Everest's height is 8848M tall! 
* 	you need to tell the client app what its name is `spring.application.name` and what file extension to use when talking to Nacos: ``` spring.cloud.nacos.config.file-extension=yaml  ```
*	Also, make sure to enable `@EnableDiscoveryClient` 
* 	Once you've done this, start the same a couple of times on the same host and you'll see multiple instances in the Nacos registry.
* 	You can use the `DiscoveryClient`.
*	Draw some config from Nacos 
* 	You can change the configuration live and use the `@EventListener(RefreshScopeRefreshedEvent.class)` or `@RefreshScope` to reconfigure on refreshes. 
*	Nacos is a great place to store sensitive information that shouldnt be in the application code itself. Sensitive information like, say, the auth token and secret for Aliyun, or Alibaba Cloud. Lets do that and then use it to connect our application to services runnning on Aliyun


## Aliyun 

* 	Add `spring.cloud.alicloud.access-key=...` and `spring.cloud.alicloud.secret-key=...` to Nacos. Now we can use Spring Cloud Alibaba Alicloud from our Spring Boot appplication. 
* 	You need to also specify which endpoint you'd like to connect your application: `spring.cloud.alicloud.oss.endpoint=http://oss-us-west-1.aliyuncs.com`
* 	you can see what services are available in your ALiyun region by visiting Alibaba Cloud. We're going to take advantage of Aliyun OSS:
	```
	@Component
	@Log4j2
	class OssProcessor {

		private final OSS oss;
		private final String globalBucketName = "kitties-in-a-bucket";
		private final Resource kittens;

		OssProcessor(OSS oss, @Value("classpath:/kittens.jpg") Resource kittens) {
			this.oss = oss;
			this.kittens = kittens;
			Assert.isTrue(kittens.exists(), "the file must exist");
		}

		@EventListener(ApplicationReadyEvent.class)
		public void useOss() throws Exception {
			Bucket photos = this.oss.createBucket(globalBucketName);
			this.oss.putObject(photos.getName(), "kittens.jpg", this.kittens.getFile());
		}
	}

	```