package com.hackathon.hackathon.crawler;


import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WebCrawler implements Runnable  {

    @Autowired
    VisitedUrlRepository visitedUrlRepository;
    public final String [] initialSites= {"https://allegro.pl/", "http://www.ceneo.pl"};
    private String urlPattern = "(www|http)[a-zA-Z._~:/?#@!$&'()*+,;=\\d\\[\\]]+";
    Pattern pattern = Pattern.compile(urlPattern);
    public WebCrawler(){
    }

    public String crawl(String url){
        Document xml = null;
        try {
            xml = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        VisitedUrl visitedUrl  = new VisitedUrl();
        visitedUrl.setParsed(false);
        visitedUrl.setUrl(url);
        visitedUrlRepository.save(visitedUrl);
        for(Element element : xml.getAllElements().stream().filter(el->
                 el.ownText().contains("allegro") || el.getElementsByAttribute("href").size()>0).collect(Collectors.toList())){
             Matcher matcher = pattern.matcher(element.ownText());
             while (matcher.find())
             {
                 String anotherUrl=matcher.group();
                 if(visitedUrlRepository.getOne(anotherUrl) != null) crawl(anotherUrl);
             }
             for(Element att: element.getElementsByAttribute("href")){
                 Matcher attMatcher = pattern.matcher(att.attr("href"));
                 while (attMatcher.find())
                 {
                     String anotherUrl=attMatcher.group();
                     if(visitedUrlRepository.getOne(anotherUrl) != null) crawl(anotherUrl);
                 }
             }
         }
         return url;
    }


    @Override
    public void run() {
        for(String init: initialSites){
            crawl(init);
        }
    }
}
