package com.hackathon.hackathon.services;
import com.hackathon.hackathon.dto.VisitedUrl;
import com.hackathon.hackathon.repositories.VisitedUrlRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebParser implements Runnable {

    VisitedUrlRepository visitedUrlRepository;

    final String specialChars = "[!@#$%^&*()_=+{\\[}\\]|;:'<>?}\"”„\t]";

    final String [] ecludedWords= {"ach","aj","albo","bardzo","bez","bo","być","ci","cie",
            "ciebie","co","czy","daleko","dla","dlaczego","dlatego","do","dobrze","dokąd","dosc",
            "duzo","dwa","dwaj","dwie","dwoje","dziś","dzisiaj","gdyby","gdzie","go","ich","ile","im","inny","ja",
            "ja","jak","jakby","jaki","je","jeden","jedna","jedno","jego","jej","jemu","jesli","jest",
            "jestem","jezeli",	"już","kazdy","kiedy","kierunku","kto","ku","lub","ma","mają","mam","mi","mna","mnie",
            "moi","mój","moja","moje","moze","mu","my","na","nam","nami","nas","nasi","nasz","nasza","nasze","natychmiast",
            "nia","nic","nich","nie","niego","niej","niemu","nigdy","nim","nimi","niz","obok","od","okolo",
            "on","ona","one","oni","ono","owszem","po",	"pod","poniewaz","przed","przedtem","sa","sam","sama",
            "sie","skad","tak","taki","tam","ten","to","toba","tobie","tu","tutaj","twoi","twoj","twoja",
            "twoje","ty","wam","wami","was","wasi","wasz","wasza","wasze","we","wiec","wszystko","wtedy","wy",
            "zaden","zawsze","ze"};

    private String url;

    org.jsoup.nodes.Document xml;
    LocalDateTime date;

    public WebParser() {
    }

    public WebParser(String url, VisitedUrlRepository visitedUrlRepository){
        this.url=url;
        this.visitedUrlRepository=visitedUrlRepository;
    }


    private LinkedHashMap<String, String> getParameters(){
        LinkedHashMap<String, String> phrases=null;
        try {
            phrases = new LinkedHashMap<>();
            xml = Jsoup.connect(url).get();
            try {
                List<Element> categories = xml.getElementsByAttribute("href").stream().filter(e -> e.attr("href").
                        contains("kategoria")).collect(Collectors.toList());
                String mainCategory = categories.get(0).getElementsByTag("span").first().ownText();
                String user = xml.getElementsByClass("btn-user").first().getElementsByTag("span").first().ownText();
                user = user.replaceAll(" {2,}", "");
                phrases.put("uzytkownik", user);
                String price = xml.getElementsByAttribute("data-price").attr("data-price");
                if (price == null || price.isEmpty()) return null;
                String category = categories.get(1).getElementsByTag("span").first().ownText();
                phrases.put("kategoria", category);
                phrases.put("cena", removeSpecChars(price));
                String mail = xml.getElementsByAttribute("href").stream().filter(element -> element.attr("href").
                        contains("mailto:")).findFirst().get().ownText();
                phrases.put("email", mail);
                String count = xml.getElementsByAttribute("max").first().attr("max");
                phrases.put("ilosc", count);
            }catch (NullPointerException | IndexOutOfBoundsException ex){
                ex.getMessage();
            }
            String description = getDescritpion();
            phrases.put("description", description);
            String auctionTitle = xml.getElementsByTag("title").first().ownText();
            String nameCleanerRgx="\\(\\d+\\) - Allegro.pl - Więcej niż aukcje.";
            String title=auctionTitle.replaceAll(nameCleanerRgx,"");
            String patternId  = "(.+\\()(\\d+)(\\) - Allegro.pl - Więcej niż aukcje.)";
            Pattern regex = Pattern.compile(patternId);
            Matcher matcher = regex.matcher(auctionTitle);
            matcher.matches();
            String auctionId=matcher.group(2);
          //  String auctionId =auctionTitle.replaceAll(patternId,"\\3");
            VisitedUrl dbUrl = visitedUrlRepository.findByAuctionIdAndAndParsed(auctionId,true);
            if(dbUrl!= null && dbUrl.getParsed()) {
                VisitedUrl currentUrl = new VisitedUrl();
                currentUrl.setAuctionId(auctionId);
                currentUrl.setUrl(url);
                visitedUrlRepository.saveAndFlush(currentUrl);
                return null;
            };
            phrases.put("auctionId", auctionId);

            phrases.put("nazwa", removeSpecChars(title));
            Element attContainer= xml.getElementsByClass("attributes-container").first();
            for(Element record: attContainer.getElementsByTag("li")){
                String key=record.getElementsByClass("attribute-name").first().ownText();
                String value=record.getElementsByClass("attribute-value").first().ownText();
                phrases.put(removeSpecChars(key),removeSpecChars(value));
            }
            if(phrases.size()==0) return null;
        } catch (IndexOutOfBoundsException | IOException e) {
            e.printStackTrace();
            System.out.println(url);
            return null;
        }
        return phrases;
    }

    private String getDescritpion(){
        List<String> descriptionStrings=xml.getAllElements().stream().filter(element -> !element.ownText().isEmpty()).map(e->removeSpecChars(e.ownText()).replaceAll("[\\d]+", ""))
                .distinct().collect(Collectors.toList());
        String desc= descriptionStrings.toString();
        return removeExcludedWords(removeSpecChars(desc.toLowerCase()));
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

    private String removeExcludedWords(String input){
        input = removeSpecChars(input);
        for(String removedWord : ecludedWords){
            input=input.replaceAll("\\b" + removedWord + "\\b","");
        }
        return input;
    }


    public void saveTextFileFromWebsite(LinkedHashMap<String, String> phrases){
        if(phrases==null || phrases.size()==0) return;
        date =  LocalDateTime.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
        String timestamp=date.format(formatter);
        try {
            File file = new File(timestamp + ".csv");
            PrintWriter out = new PrintWriter(file);
            String description = phrases.remove("description");
            String auctionId = phrases.remove("auctionId");
            for(String key:phrases.keySet()){
                out.write(removeSpecChars(key.toLowerCase())+"\t");
            }
            out.write("\r\n");
            for(String value:phrases.values()){
                out.write(removeSpecChars(value.toLowerCase())+"\t");
            }
            out.write("\r\n");
            out.write(description);
            out.write("\r\n");
            out.close();
            VisitedUrl visitedUrl = new VisitedUrl();
            visitedUrl.setUrl(url);
            visitedUrl.setParsed(true);
            visitedUrl.setAuctionId(auctionId);
            visitedUrlRepository.saveAndFlush(visitedUrl);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    @Transactional
    public void run() {
        if(!visitedUrlRepository.findByUrl(url).getParsed())
        saveTextFileFromWebsite(getParameters());
    }
}
