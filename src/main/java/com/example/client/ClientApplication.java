package com.example.client;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelProtect;
import org.springframework.cloud.alibaba.sentinel.datasource.annotation.SentinelDataSource;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

@Log4j2
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ClientApplication {

	@SentinelDataSource("spring.cloud.sentinel.datasource")
	private ReadableDataSource readableDataSource;

	private final DiscoveryClient discoveryClient;
	private final Environment environment;

	ClientApplication(DiscoveryClient discoveryClient, Environment environment) throws UnknownHostException {
		this.discoveryClient = discoveryClient;
		this.environment = environment;
	}

	@SentinelProtect
	@GetMapping("/nihao")
	String hello() {
		return "Hello, world!";
	}

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@EventListener(RefreshScopeRefreshedEvent.class)
	public void refreshScopeRefreshedEvent() {
		log.info(this.environment.getProperty("message"));
	}

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {
		log.info(ApplicationReadyEvent.class.getName() + " (annotation)");
		this.discoveryClient
			.getServices()
			.stream()
			.flatMap(svcId -> this.discoveryClient.getInstances(svcId).stream())
			.forEach(si -> log.info("service instance ID: " + si.toString()));
	}
}

/*@Data
@AllArgsConstructor
@NoArgsConstructor
class Foo {
	private String name;
}

@Configuration
class FooConfig {

	@ConfigurationProperties(prefix = "a")
	Foo a() {
		return new Foo();
	}

	@ConfigurationProperties(prefix = "b")
	Foo b() {
		return new Foo();
	}

}

@Component
class Lister {

	private final Foo[] foos;

	Lister(Foo[] foos) {
		this.foos = foos;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() {
		for (Foo f : foos) {
			LogFactory.getLog(getClass()).info(f.toString());
		}
	}

}
*
*/

@Component("flowConverter")
class FlowConverter implements Converter<String, List<FlowRule>> {

	@Override
	public List<FlowRule> convert(String source) {
		return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
		});
	}
}


/*
flow control rules from sentinel if the TPS limit is exceeded this gets invoked
**/
@Component
@Log4j2
class MyBlockHandler implements UrlBlockHandler {

	@Override
	public void blocked(HttpServletRequest req,
																					HttpServletResponse res,
																					BlockException e) throws IOException {
		String app = e.getRuleLimitApp();
		log.info("app: " + app);
		log.info("block URI: " + req.getRequestURI());
		res.getWriter().append("HOLY SHIT IT'S THE END!!!!!!");
	}
}

