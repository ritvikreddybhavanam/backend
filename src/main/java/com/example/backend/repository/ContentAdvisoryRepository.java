package com.example.backend.repository;

import com.example.backend.entity.ContentAdvisory;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentAdvisoryRepository extends JpaRepository<ContentAdvisory, Long> {

    List<ContentAdvisory> findByMovieId(Long movieId);

    Optional<ContentAdvisory> findByMovieIdAndUserAndCategory(Long movieId, User user, String category);

    long countByMovieId(Long movieId);

    long countByMovieIdAndCategory(Long movieId, String category);
}
