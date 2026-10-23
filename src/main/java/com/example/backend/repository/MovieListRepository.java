package com.example.backend.repository;

import com.example.backend.entity.MovieList;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieListRepository extends JpaRepository<MovieList, Long> {

    List<MovieList> findByUserOrderByCreatedAtDesc(User user);

    List<MovieList> findByIsPublicTrueOrderByCreatedAtDesc();

    List<MovieList> findByUserInAndIsPublicTrueOrderByCreatedAtDesc(List<User> users);

    List<MovieList> findTop30ByIsPublicTrueOrderByCreatedAtDesc();

    Optional<MovieList> findByIdAndUser(Long id, User user);
}
