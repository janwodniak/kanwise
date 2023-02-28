package com.kanwise.user_service.repository.authentication;

import com.kanwise.user_service.model.otp.OneTimePassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Long> {
}

