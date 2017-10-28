package com.hackathon.hackathon.webparser;

import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WebParser {
    @Autowired
    VisitedUrlRepository visitedUrlRepository;
    private final int maxBatchSize=100;

    final String specialChars = "[!@#$%^&*()_=+{\\[}\\]|;:'<>?}\"”„\t]";

    org.jsoup.nodes.Document xml;
    LocalDateTime date;

    public WebParser() {
        while(true){
            batchFiles();
        }
    }

    private LinkedHashMap<String, String> getParameters(String url){
        LinkedHashMap<String, String> phrases=null;
        try {
            phrases = new LinkedHashMap<>();
            xml = Jsoup.connect(url).get();
            String title = xml.getElementsByTag("title").first().ownText();
            String nameCleanerRgx="\\(\\d+\\) - Allegro.pl - Więcej niż aukcje.";
            title=title.replaceAll(nameCleanerRgx,"");
            phrases.put("nazwa", removeSpecChars(title));
            String price = xml.getElementsByAttribute("data-price").attr("data-price");
            phrases.put("cena", removeSpecChars(price));

            Element attContainer= xml.getElementsByClass("attributes-container").first();
            for(Element record: attContainer.getElementsByTag("li")){
                String key=record.getElementsByClass("attribute-name").first().ownText();
                String value=record.getElementsByClass("attribute-value").first().ownText();
                phrases.put(removeSpecChars(key),removeSpecChars(value));
            }
            if(phrases.size()==0 || !phrases.get("stan").equals("nowy")) return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return phrases;
    }

    private String removeSpecChars(String input){
        input= input.replaceAll(specialChars,"");
        input = input.replace("ś","s");
        input = input.replace("ą","a");
        input = input.replace("ź","z");
        input = input.replace("ż","z");
        input = input.replace("ę","e");
        input = input.replace("ą","a");
        input = input.replace("ó","o");
        input = input.replace("ń","n");
        input = input.replace("ć","c");
        input = input.replace("ł","l");
        return input;
    }




    public void saveTextFileFromWebsite(LinkedHashMap<String, String> phrases){
        date =  LocalDateTime.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
        String timestamp=date.format(formatter);
        try {
            File file = new File(timestamp + ".csv");
            PrintWriter out = new PrintWriter(file);
            for(String key:phrases.keySet()){
                out.write(key+"\t");
            }
            out.write("\r\n");
            for(String value:phrases.values()){
                out.write(value+"\t");
            }
            out.write("\r\n");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void batchFiles(){
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        List<LinkedHashMap<String,String>> allMaps = new ArrayList<>();
        int i=0;
        while(i++< maxBatchSize && visitedUrlRepository.findAllByParsed(false).size()>0){
            VisitedUrl visitedUrl=visitedUrlRepository.findByParsed(false);
            visitedUrl.getUrl();
            LinkedHashMap<String,String> phrases= getParameters(visitedUrl.getUrl());
            if(phrases !=null){
                headers.addAll(phrases.keySet());
                allMaps.add(phrases);
            }
            visitedUrl.setParsed(true);
            visitedUrlRepository.save(visitedUrl);
        }
        if(headers.size()>0){
                for(LinkedHashMap<String,String> batchedMap: allMaps){
                    for(String header: headers) {
                        if(!batchedMap.keySet().contains(header)) batchedMap.put(header,"");
                    }
                }
                saveBatch(allMaps);
        }
        if(visitedUrlRepository.findAllByParsed(false).size()==0){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveBatch(List<LinkedHashMap<String,String>> allMaps){
        date =  LocalDateTime.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
        String timestamp=date.format(formatter);
        File file = new File(timestamp + ".csv");
        PrintWriter out = null;
        LinkedHashSet<String> headers = (LinkedHashSet) allMaps.get(0).keySet();
        try {
            out = new PrintWriter(file);
            for(String header : headers){
                out.write(header+"\t");
            }
            out.write("\r\n");
            for(LinkedHashMap<String, String> auction : allMaps){
                for(String header: headers){
                    out.write(auction.get(header) +"\t");
                }
                out.write("\r\n");
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
 }

}
