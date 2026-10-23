package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    long countByFollower(User follower);

    long countByFollowing(User following);

    List<UserFollow> findByFollowerOrderByCreatedAtDesc(User follower);

    List<UserFollow> findByFollowingOrderByCreatedAtDesc(User following);

    @Query("SELECT uf.following FROM UserFollow uf WHERE uf.follower = :follower")
    List<User> findFollowingUsersByFollower(@Param("follower") User follower);

    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.following = :following")
    List<User> findFollowerUsersByFollowing(@Param("following") User following);

    void deleteByFollowerAndFollowing(User follower, User following);
}
