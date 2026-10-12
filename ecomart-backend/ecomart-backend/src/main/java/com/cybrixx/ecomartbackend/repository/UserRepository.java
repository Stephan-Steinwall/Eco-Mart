package com.cybrixx.ecomartbackend.repository;


import com.cybrixx.ecomartbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /**
     * A device token belongs to exactly one account: when someone logs in on a phone that a
     * different account used before, the previous owner must stop receiving that phone's pushes.
     */
    @Transactional
    @Modifying
    @Query("update User u set u.fcmToken = null where u.fcmToken = :token and u.id <> :userId")
    void clearTokenFromOtherUsers(@Param("token") String token, @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("update User u set u.fcmToken = null where u.id = :userId")
    void clearToken(@Param("userId") Long userId);
}
