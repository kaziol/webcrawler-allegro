package com.hackathon.hackathon.repositories;

import com.hackathon.hackathon.dto.VisitedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitedUrlRepository extends JpaRepository<VisitedUrl, String> {
    List<VisitedUrl> findAllByParsed(boolean parsed);
    VisitedUrl findByParsed(boolean parsed);
    VisitedUrl findByUrl(String url);

}
