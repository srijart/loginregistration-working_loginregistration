package com.prt.skilltechera.repository;

import com.prt.skilltechera.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepo  extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(int token);
}
