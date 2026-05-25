package com.block.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



public interface RefreshTokenRepository extends JpaRepository<com.block.auth.entity.RefreshToken, Long>{
	
	 @Query("SELECT COUNT(rt) = 0 FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revoked = false")
	    boolean areAllTokensRevokedForUser(Long userId);

}
