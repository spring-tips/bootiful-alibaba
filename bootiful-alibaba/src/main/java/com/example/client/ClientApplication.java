package com.example.client;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.Bucket;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;


@EnableDiscoveryClient
@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}

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

@Component
@Log4j2
class OssProcessor {

	private final OSS oss;
	private final String globalBucketName = "kitties-in-a-bucket";
	private final Resource kittens;

	OssProcessor(OSS oss, @Value("file:${user.home}/Desktop/kitty.jpg") Resource kittens) {
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

@RestController
class FlowControlledRestController {

	@GetMapping("/eek")
	String eek() {
		return "OH NOES!";
	}

	@GetMapping("/code")
	String code() {
		return "nong hao";
	}

	//	@SentinelResource
	@GetMapping("/dashboard")
	String dashboard() {
		return "ni hao";
	}
}