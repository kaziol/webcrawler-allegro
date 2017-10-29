package com.hackathon.hackathon.services;


import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WebCrawler  {
    @Autowired
    VisitedUrlRepository visitedUrlRepository;
    public final String initialSite= "https://allegro.pl/";
    private String urlPattern = "(www|http)[a-zA-Z._~:/?#@!$&'()*+,;=\\d\\[\\]]+";
    Pattern pattern = Pattern.compile(urlPattern);
    public WebCrawler(){
    }

    public String crawl(){
        return crawl(initialSite);
    }

    public String crawl(String url){
        VisitedUrl visitedUrl  = new VisitedUrl();
        visitedUrl.setUrl(url);
        visitedUrlRepository.saveAndFlush(visitedUrl);
        Document xml = null;
        try {
            xml = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if(!url.endsWith(".css")){
            WebParser wp = new WebParser(url, visitedUrlRepository);
            Thread t1 = new Thread(wp);
            t1.start();
        }
        {
        for(String href : xml.getAllElements().stream().map(element -> element.attr("href")).filter(
                e->!e.isEmpty() &&
                       e.startsWith("http") && e.contains("allegro.pl/") && !e.endsWith(".css")).collect(Collectors.toList()))
        if(visitedUrlRepository.findByUrl(href) ==null)
            {
                     crawl(href);
             }
         }
         return url;
    }
}
