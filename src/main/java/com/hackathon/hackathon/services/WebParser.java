package com.hackathon.hackathon.services;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebParser implements Runnable {

    final String specialChars = "[!@#$%^&*()_=+{\\[}\\]|;:'<>?}\"”„\t]";

    final String [] ecludedWords= {"ach","aj","albo","bardzo","bez","bo","być","ci","cię",
            "ciebie","co","czy","daleko","dla","dlaczego","dlatego","do","dobrze","dokąd","dość",
            "dużo","dwa","dwaj","dwie","dwoje","dziś","dzisiaj","gdyby","gdzie","go","ich","ile","im","inny","ja",
            "ją","jak","jakby","jaki","je","jeden","jedna","jedno","jego","jej","jemu","jeśli","jest",
            "jestem","jeżeli",	"już","każdy","kiedy","kierunku","kto","ku","lub","ma","mają","mam","mi","mną","mnie",
            "moi","mój","moja","moje","może","mu","my","na","nam","nami","nas","nasi","nasz","nasza","nasze","natychmiast",
            "nią","nic","nich","nie","niego","niej","niemu","nigdy","nim","nimi","niż","obok","od","około",
            "on","ona","one","oni","ono","owszem","po",	"pod","ponieważ","przed","przedtem","są","sam","sama","się","skąd","tak","taki","tam","ten","to","tobą","tobie","tu","tutaj","twoi","twój","twoja",
            "twoje","ty","wam","wami","was","wasi","wasz","wasza","wasze","we","więc","wszystko","wtedy","wy","żaden","zawsze","że"};

    final String [] categoriesArray={"Fotografia","Akcesoria fotograficzne","Aparaty analogowe","Aparaty cyfrowe",
            "Karty pamięci","Lampy błyskowe","Obiektywy","Wyposażenie studia","Zasilanie aparatów","Literatura i instrukcje",
            "Sprzęt optyczny","Usługi","Pozostałe","Komputery","Akcesoria (Laptop, PC)","Części do laptopów","Drukarki i skanery",
            "Dyski i pamięci przenośne","Dźwięk","Internet","Komputery stacjonarne","Komunikacja i łączność","Laptopy","Mikrokomputery",
            "Napędy optyczne i nośniki","Obraz i grafika","Obudowy i zasilanie","Oprogramowanie","Podzespoły bazowe",
            "Serwery i SCSI","Tablety","Urządzenia wskazujące","Usługi","Pozostałe","Konsole i automaty","Game Boy",
            "Game Boy Advance","Microsoft Xbox","Microsoft Xbox 360","Microsoft Xbox One","Nintendo 3DS","Nintendo 64","" +
            "Nintendo DS","Nintendo GameCube","Nintendo Switch","Nintendo Wii U","Nintendo (SNES i NES)","Nintendo Wii",
            "Sega Dreamcast","Sega (inne)","Sony PlayStation (PSX)","Sony PlayStation 2 (PS2)","Sony PlayStation 3 (PS3)",
            "Sony PlayStation 4 (PS4)","Sony PS Vita","Sony PSP","'Gierki' elektroniczne","Pegasus","Automaty do gier","Usługi",
            "Pozostałe","RTV i AGD","AGD do zabudowy","AGD drobne","AGD wolnostojące","Czytniki ebooków","Elektronika",
            "GPS i akcesoria","Kamery","Piloty","Sprzęt audio dla domu","Sprzęt audio przenośny","Sprzęt car audio","Sprzęt satelitarny",
            "Słuchawki","TV i Video","Zasilanie","Pozostałe","Usługi","Karty Allegro","Sprzęt estradowy, studyjny i DJ-ski",
            "CD-playery dla DJ-ów","Gramofony dla DJ-ów","Kable, przewody i wtyki","Mikrofony","Miksery audio","Nagłośnienie",
            "Sceny, estrady, podesty","Słuchawki","Video dla studia","Walizki i torby","Światło i efekty","Urządzenia rejestrujące",
            "Literatura i instrukcje","Pozostałe","Usługi","Telefony i Akcesoria","Abonamenty","Akcesoria GSM","Karty pamięci",
            "Powerbanki","Pre-paid","Radiokomunikacja","Smartwatch","Telefony komórkowe","Urządzenia stacjonarne","Złote numery",
            "Pozostałe","Usługi"};
    final List<String> categoriesElectrions =Arrays.asList(categoriesArray);

    private String url;

    org.jsoup.nodes.Document xml;
    LocalDateTime date;

    public WebParser() {
    }

    public WebParser(String url){
        this.url=url;
    }


    private LinkedHashMap<String, String> getParameters(){
        LinkedHashMap<String, String> phrases=null;
        try {
            phrases = new LinkedHashMap<>();
            xml = Jsoup.connect(url).get();
            List<Element> categories = xml.getElementsByAttribute("href").stream().filter(e -> e.attr("href").
                    contains("kategoria")).collect(Collectors.toList());
            String mainCategory=categories.get(0).getElementsByTag("span").first().ownText();

            if(!categoriesElectrions.contains(mainCategory)) return null;
            String user = xml.getElementsByClass("btn-user").first().getElementsByTag("span").first().ownText();
            user = user.replaceAll(" {2,}", "");
            phrases.put("uzytkownik", user);

            //TODO: EMAIL, USER
            String price = xml.getElementsByAttribute("data-price").attr("data-price");
            if(price==null || price.isEmpty()) return null;

            String category=categories.get(1).getElementsByTag("span").first().ownText();

            phrases.put("kategoria", category);

            phrases.put("cena", removeSpecChars(price));
            String mail = xml.getElementsByAttribute("href").stream().filter(element -> element.attr("href").
                    contains("mailto:")).findFirst().get().ownText();
            phrases.put("email", mail);

            String count = xml.getElementsByAttribute("max").first().attr("max");
            phrases.put("ilosc", count);




            String description = getDescritpion();
            phrases.put("description", description);
            String title = xml.getElementsByTag("title").first().ownText();
            String nameCleanerRgx="\\(\\d+\\) - Allegro.pl - Więcej niż aukcje.";
            title=title.replaceAll(nameCleanerRgx,"");
            phrases.put("nazwa", removeSpecChars(title));
            Element attContainer= xml.getElementsByClass("attributes-container").first();
            for(Element record: attContainer.getElementsByTag("li")){
                String key=record.getElementsByClass("attribute-name").first().ownText();
                String value=record.getElementsByClass("attribute-value").first().ownText();
                phrases.put(removeSpecChars(key),removeSpecChars(value));
            }
            if(phrases.size()==0 || !phrases.get("stan").equals("nowy")) return null;
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
        return removeExcludedWords(desc);
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
            for(String key:phrases.keySet()){
                out.write(removeSpecChars(key)+"\t");
            }
            out.write("\r\n");
            for(String value:phrases.values()){
                out.write(removeSpecChars(value)+"\t");
            }
            out.write("\r\n");
            out.write(removeSpecChars(description));
            out.write("\r\n");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        saveTextFileFromWebsite(getParameters());
    }
}
