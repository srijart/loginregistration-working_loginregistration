package com.prt.skilltechera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LoginregistrationApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(LoginregistrationApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
		return builder.sources(LoginregistrationApplication.class);
	}

}
