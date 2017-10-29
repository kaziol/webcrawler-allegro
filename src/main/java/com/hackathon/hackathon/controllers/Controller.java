package com.hackathon.hackathon.controllers;

import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import com.hackathon.hackathon.services.WebCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {
    @Autowired
    WebCrawler webCrawler;

    @Autowired
    VisitedUrlRepository repository;
/*
    @RequestMapping("/run")
    public List<VisitedUrl> runCrawler(){
        webCrawler.crawl();
        return repository.findAll();
    }*/

    @RequestMapping("/list")
    public List<VisitedUrl> getListOfAuctions(){
        return repository.findAll();
    }

    @RequestMapping("/count")
    public Map<String, Integer> getCountOfParsed(){
        Map<String, Integer> map = new HashMap<>();
        map.put("all_visited",repository.findAll().size());
        map.put("parsed",repository.findAllByParsed(true).size());
        return map;
    }

    @RequestMapping("/list/{parsed}")
    public List<VisitedUrl> getListOfFiles(@PathVariable("parsed") boolean parsed){
        return repository.findAllByParsed(parsed);
    }


    @Scheduled(fixedDelay = 100)
    private void scheduleRun(){
        webCrawler.crawl();
    }

}
