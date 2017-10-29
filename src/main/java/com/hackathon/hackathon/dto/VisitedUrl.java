package com.hackathon.hackathon.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class VisitedUrl {
    @Id
    String url;
    Boolean parsed;


    public Boolean getParsed() {
        return parsed;
    }

    public void setParsed(Boolean parsed) {
        this.parsed = parsed;
    }


    public VisitedUrl() {
    }

    public VisitedUrl(String url, Boolean parsed) {

        this.url = url;
        this.parsed = parsed;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
