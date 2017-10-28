package com.hackathon.hackathon.webparser;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebParser {

    private String [] excludedWords= {" i ", " a ", " o ", " w "};

    String specialChars = "[!@#$%^&*()_=+{[}]|;:'<>?}\"]";

    org.jsoup.nodes.Document xml;
    LocalDateTime date;

    public WebParser() {
    }



    private List<String> getTagContents(String url) {
        List<String> phrases=null;
        try {
            phrases = new ArrayList<>();
            xml = Jsoup.connect(url).get();
            return xml.getAllElements().stream().filter(p -> p.hasText() ).map(p -> {
                String output = p.toString().toLowerCase();
                for(String excluded: excludedWords){
                    output=output.replace(excluded,"");
                }
                return output.replaceAll(specialChars,"");
            }
            ).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return phrases;
    }
    public void saveTextFileFromWebsite(String url){
        date =  LocalDateTime.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
        String timestamp=date.format(formatter);
        try {
            File file = new File(timestamp + ".txt");
            PrintWriter out = new PrintWriter(file);
            List<String> phrases = getTagContents(url);
            for(String phrase: phrases){
                out.write(phrase);
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

}
