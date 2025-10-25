package com.example.livestream_apd;

import com.example.livestream_apd.utils.TimeUtil;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
@OpenAPIDefinition(
		info = @Info(
				title = "LiveStream API",
				version = "1.0.0",
				description = "API documentation for livestream",
				contact = @Contact(
						name = "POW",
						email = "hbday2k3@gmail.com"
				)
		),
		servers = {
				@Server(url = "http://localhost:8080/api/v1", description = "dev server"),
				@Server(url ="", description = "production server")
		}
)
public class LivestreamApdApplication {
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		TimeUtil.verifyTimezoneSetup();
	}

	public static void main(String[] args) {
		SpringApplication.run(LivestreamApdApplication.class, args);
	}

}
