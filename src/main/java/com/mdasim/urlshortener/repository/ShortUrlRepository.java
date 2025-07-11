package com.mdasim.urlshortener.repository;

import com.mdasim.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByOriginalUrl(String originalUrl);

    @Query("SELECT s FROM ShortUrl s ORDER BY s.hitCount DESC")
    List<ShortUrl> findAllOrderByHitCountDesc();

    @Query("SELECT s FROM ShortUrl s WHERE s.hitCount > :minHits")
    List<ShortUrl> findByHitCountGreaterThan(@Param("minHits") int minHits);

    @Query("SELECT COUNT(s) FROM ShortUrl s WHERE s.originalUrl = :url")
    long countByOriginalUrl(@Param("url") String url);
}