package com.example.backend.repository;

import com.example.backend.entity.MovieList;
import com.example.backend.entity.MovieListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieListItemRepository extends JpaRepository<MovieListItem, Long> {

    Optional<MovieListItem> findByMovieListAndMovieId(MovieList movieList, Long movieId);

    void deleteByMovieListAndMovieId(MovieList movieList, Long movieId);
}
