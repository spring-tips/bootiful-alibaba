package com.example.client;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.Bucket;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelProtect;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ClientApplication {

	private final DiscoveryClient discoveryClient;
	private final Environment environment;

	ClientApplication(DiscoveryClient discoveryClient, Environment environment) {
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


@Component
@Log4j2
class OssListener {

	private final OSS oss;
	private final String globalBucketName = "kitties-in-a-bucket";
	private final Resource kittens;

	OssListener(OSS oss, @Value("classpath:/kittens.jpg") Resource kittens) {
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
		res.getWriter().append("Oh noes!");
	}
}

