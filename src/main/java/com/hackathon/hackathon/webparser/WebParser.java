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

    private final String [] excludedWords= {" ach "," aj "," albo "," bardzo "," bez "," bo "," być "," ci "," cię "," ciebie "," co "," czy "," daleko "," dla "," dlaczego "," dlatego "," do "," dobrze "," dokąd "," dość "," dużo "," dwa "," dwaj "," dwie "," dwoje "," dziś "," dzisiaj "," gdyby "," gdzie "," go "," ich "," ile "," im "," inny "," ja "," ją "," jak "," jakby "," jaki "," je "," jeden "," jedna "," jedno "," jego "," jej "," jemu "," jeśli "," jest "," jestem "," jeżeli ",	" już "," każdy "," kiedy "," kierunku "," kto "," ku "," lub "," ma "," mają "," mam "," mi "," mną "," mnie "," moi "," mój "," moja "," moje "," może "," mu "," my "," na "," nam "," nami "," nas "," nasi "," nasz "," nasza "," nasze "," natychmiast "," nią "," nic "," nich "," nie "," niego "," niej "," niemu "," nigdy "," nim "," nimi "," niż "," obok "," od "," około "," on "," ona "," one "," oni "," ono "," owszem "," po ",	" pod "," ponieważ "," przed "," przedtem "," są "," sam "," sama "," się "," skąd "," tak "," taki "," tam "," ten "," to "," tobą "," tobie "," tu "," tutaj "," twoi "," twój "," twoja "," twoje "," ty "," wam "," wami "," was "," wasi "," wasz "," wasza "," wasze "," we "," więc "," wszystko "," wtedy "," wy "," żaden "," zawsze "," że "};

    final String specialChars = "[!@#$%^&*()_=+{[}]|;:'<>?}\"”„]";

    org.jsoup.nodes.Document xml;
    LocalDateTime date;

    public WebParser() {
    }



    private List<String> getTagContents(String url) {
        List<String> phrases=null;
        try {
            phrases = new ArrayList<>();
            xml = Jsoup.connect(url).get();
            return xml.getAllElements().stream().filter(p -> !p.ownText().isEmpty()).map(p -> {
                //add space as excluded wors are starting with spaces
                String output =" " + p.ownText().toLowerCase();
                output=output.replaceAll(specialChars,"");
                for(String excluded: excludedWords){
                    output=output.replace(excluded," ");
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
