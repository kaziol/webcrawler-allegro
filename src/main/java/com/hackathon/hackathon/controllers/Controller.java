package com.hackathon.hackathon.controllers;

import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import com.hackathon.hackathon.services.WebCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {
    @Autowired
    WebCrawler webCrawler;

    @Autowired
    VisitedUrlRepository repository;

    @RequestMapping("/run")
    public List<VisitedUrl> runCrawler(){
        webCrawler.crawl();
        return repository.findAll();
    }

    @RequestMapping("/list")
    public List<VisitedUrl> getListOfFiles(){
        return repository.findAll();
    }


}
