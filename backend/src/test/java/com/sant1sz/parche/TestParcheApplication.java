package com.sant1sz.parche;

import org.springframework.boot.SpringApplication;

public class TestParcheApplication {

	public static void main(String[] args) {
		SpringApplication.from(ParcheApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
