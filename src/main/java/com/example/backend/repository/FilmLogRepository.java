package com.example.backend.repository;

import com.example.backend.entity.FilmLog;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FilmLogRepository extends JpaRepository<FilmLog, Long> {

    List<FilmLog> findByUserOrderByWatchedDateDescCreatedAtDesc(User user);

    List<FilmLog> findByUserAndWatchedDateBetweenOrderByWatchedDateDescCreatedAtDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    long countByUser(User user);

    List<FilmLog> findByUserInOrderByCreatedAtDesc(List<User> users);

    List<FilmLog> findTop30ByOrderByCreatedAtDesc();

    @Query("SELECT fl FROM FilmLog fl WHERE fl.user = :user AND YEAR(fl.watchedDate) = :year ORDER BY fl.watchedDate DESC")
    List<FilmLog> findByUserAndYear(@Param("user") User user, @Param("year") int year);

    @Query("SELECT DISTINCT YEAR(fl.watchedDate) FROM FilmLog fl WHERE fl.user = :user ORDER BY YEAR(fl.watchedDate) DESC")
    List<Integer> findDistinctYearsByUser(@Param("user") User user);
}
