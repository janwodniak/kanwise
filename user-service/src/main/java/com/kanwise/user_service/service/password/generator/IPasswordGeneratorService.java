package com.kanwise.user_service.service.password.generator;

import com.netflix.discovery.shared.Pair;

public interface IPasswordGeneratorService {

    String generatePassword();

    String generateEncryptedPassword(String password);

    Pair<String, String> generatePasswordAndEncryptedPassword();
}
