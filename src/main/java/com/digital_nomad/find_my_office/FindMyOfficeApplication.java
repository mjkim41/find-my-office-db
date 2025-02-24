package com.digital_nomad.find_my_office;

import com.digital_nomad.find_my_office.config.DotEnvConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
public class FindMyOfficeApplication {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(FindMyOfficeApplication.class);
		app.addInitializers(new DotEnvConfig());
		app.run(args);
	}

}
