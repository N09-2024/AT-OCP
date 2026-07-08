package com.ocp.at.repository;

import com.ocp.at.entity.RefreshToken;
import com.ocp.at.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUtilisateur(Utilisateur utilisateur);

    @Modifying
    void deleteByToken(String token);
}
