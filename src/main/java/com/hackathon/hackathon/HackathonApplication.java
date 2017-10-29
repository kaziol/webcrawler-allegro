package com.hackathon.hackathon;

import com.hackathon.hackathon.services.WebCrawler;
import com.hackathon.hackathon.services.WebParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class HackathonApplication {

	@Autowired
	WebCrawler webCrawler;


	public static void main(String[] args) {
		SpringApplication.run(HackathonApplication.class, args);
	}


}
