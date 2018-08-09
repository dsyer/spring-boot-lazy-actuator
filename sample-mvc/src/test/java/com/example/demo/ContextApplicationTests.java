package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "management.endpoints.web.base-path=/manage")
public class ContextApplicationTests {

	@Autowired
	private TestRestTemplate rest;

	@Test
	public void contextLoads() {
		assertThat(rest.getForObject("/manage/health", String.class)).contains("UP");
	}

}
