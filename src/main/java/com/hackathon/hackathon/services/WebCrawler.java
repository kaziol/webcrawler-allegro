package com.hackathon.hackathon.services;


import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@Service
public class WebCrawler  {
    @Autowired
    VisitedUrlRepository visitedUrlRepository;
    public final String initialSite= "https://allegro.pl/dzial/elektronika";


    String [] keyPhrases ={"fotograf",  "macbook", "galaxy", "intel",  "apple", "akcesori",   "fotograf",     "aparat",     "analogow",
            "cyfrow",     "kart", "pamieci",     "lamp", "blyskow",    "obiektyw",   "wyposazeni", "studi",      "zasilani",
            "literatur",  "instrukcj",  "sprzet",      "optyczn",    "uslug", "pozostal",   "komputer",   "laptop",         "czesc",
            "laptop",    "drukark",     "skaner",     "dysk",  "pamiec",      "przenosn",   "dzwiek",      "internet",    "stacjonarn",
            "komunikac", "lacznosc",    "laptop",     "mikrokomputer",    "naped",      "optyczn",    "nosnik",
            "obraz", "grafik",     "obudow",     "zasilani",   "oprogramowani",    "podzespol",       "serwer",     "scsi",  "tablet",
            "urzadzeni",  "wskazujac",  "konsol",     "automat",         "advanc",     "microsoft",   "xbox",
            "nintendo",           "gamecub",    "switch",         "snes",       "dreamcast",   "playstation",
            "gierki",   "elektroniczn",     "pegasus",     "automat",    "gier",
            "uslugi",                    "czytnik",     "ebook",     "elektroni",    "akcesori",   "kamer",
            "pilot",      "audio",  "przenosn",      "satelitarn", "sluchawk",        "video",       "player",
            "gramofon",   "kabl",      "przewod",    "wtyk",  "mikrofon",  "mikser",     "naglosnieni",      "scen",      "estrad",
            "podest",    "video", "walizk",      "torb", "swiatlo",     "efekt",      "rejestrujac",      "telefon",    "abonament",
            "pamiec",      "powerbank",     "radiokomunikacj",  "smartwatch",  "komorkow",   "zlot", "numer",      "satelitarn",
            "sluchawk",        "video", "zasilani",   "pozostal",   "uslug", "kart", "sprzet",      "estradow",   "studyjn",
            "gramofon",    "kabl",      "przewod",    "wtyk",  "mikrofon",   "mikser",     "audio", "naglosnieni",      "scen",
            "estrad",     "podest",    "sluchawk",    "video", "studi",      "walizk",      "torb", "swiatlo",     "efekt",      "urzadzeni",
            "rejestrujac",      "literatur",  "instrukcj",  "pozostal",   "uslug", "telefon",    "akcesori",   "abonament",  "akcesori",
            "kart", "pamiec",      "powerbank",     "radiokomunikacj",  "smartwatch",  "telefon",    "komorkow",   "urzadzeni",
            "stacjonarn", "zlot", "numer",      "pozostal",   "uslug"};
    List<String> phrasesList = Arrays.asList(keyPhrases);
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
        Comparator<String> comparator = Comparator.<String, Boolean>
                comparing(s -> {
                    for(String key: keyPhrases){
                        if(s.contains(key)) return true;
                    }
                    return false;
        })
              .reversed().thenComparing(Comparator.naturalOrder());


        xml.getAllElements().stream().map(element -> element.attr("href")).filter(
                e->!e.isEmpty() &&
                        e.startsWith("http") && e.contains("allegro.pl/") && !e.contains(".php") && !e.endsWith(".css")).
                distinct().sorted(comparator).forEach(address->{
                    if(visitedUrlRepository.findByUrl(address)==null){
                        crawl(address);
                    }
        });
        return url;
    }
}
