package com.carmineabk.parcella;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ParcellaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParcellaApplication.class, args);
	}

}
