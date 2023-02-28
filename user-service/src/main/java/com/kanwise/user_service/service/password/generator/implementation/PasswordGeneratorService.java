package com.kanwise.user_service.service.password.generator.implementation;

import com.kanwise.user_service.service.password.encoder.IPasswordEncoderService;
import com.kanwise.user_service.service.password.generator.IPasswordGeneratorService;
import com.netflix.discovery.shared.Pair;
import lombok.RequiredArgsConstructor;
import org.passay.CharacterRule;
import org.passay.PasswordGenerator;
import org.springframework.stereotype.Service;

import static java.util.Arrays.asList;
import static org.passay.EnglishCharacterData.Digit;
import static org.passay.EnglishCharacterData.LowerCase;
import static org.passay.EnglishCharacterData.Special;
import static org.passay.EnglishCharacterData.UpperCase;

@RequiredArgsConstructor
@Service
public class PasswordGeneratorService implements IPasswordGeneratorService {

    private final PasswordGenerator passwordGenerator;
    private final IPasswordEncoderService passwordEncoderService;

    @Override
    public String generatePassword() {
        return passwordGenerator.generatePassword(8,
                asList(
                        new CharacterRule(UpperCase, 1),
                        new CharacterRule(LowerCase, 1),
                        new CharacterRule(Digit, 1),
                        new CharacterRule(Special, 1))
        );
    }

    @Override
    public String generateEncryptedPassword(String password) {
        return passwordEncoderService.encodePassword(generatePassword());
    }

    @Override
    public Pair<String, String> generatePasswordAndEncryptedPassword() {
        String password = generatePassword();
        return new Pair<>(password, passwordEncoderService.encodePassword(password));
    }
}
