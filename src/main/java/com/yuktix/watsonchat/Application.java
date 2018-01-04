package com.yuktix.watsonchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.WebSocketTraceChannelInterceptorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Import(WebSocketTraceChannelInterceptorAutoConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}
