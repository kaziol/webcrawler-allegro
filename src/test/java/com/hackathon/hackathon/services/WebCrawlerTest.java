package com.hackathon.hackathon.crawler;

import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import com.hackathon.hackathon.webparser.WebParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class WebCrawlerTest {
    @Autowired
    WebCrawler webCrawler;

    @Autowired
    WebParser webParser;

    @Autowired
    VisitedUrlRepository visitedUrlRepository;

    @Test
    public void crawl() throws Exception {
        Thread t1 = new Thread(webCrawler);
        t1.start();
        sleep(1000);
    }

}