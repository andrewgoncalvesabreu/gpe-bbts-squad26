package br.com.bbts.gpe.gpe_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GpeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GpeBackendApplication.class, args);
	}

}
