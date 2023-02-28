package com.kanwise.user_service.service.password.encoder;

public interface IPasswordEncoderService {

    String encodePassword(String password);

    boolean encodedPasswordMatches(String password, String encodedPassword);
}
