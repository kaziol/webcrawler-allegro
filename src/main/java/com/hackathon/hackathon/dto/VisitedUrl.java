package com.hackathon.hackathon.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class VisitedUrls {
    @Id
    String url;
    Boolean suspicious;
}
