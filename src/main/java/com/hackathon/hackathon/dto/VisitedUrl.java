package com.hackathon.hackathon.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class VisitedUrl {
    @Id
    String url;
    Boolean parsed;
    String auctionId;

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public Boolean getParsed() {
        return parsed;
    }

    public void setParsed(Boolean parsed) {
        this.parsed = parsed;
    }


    public VisitedUrl() {
        this.parsed=false;
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
