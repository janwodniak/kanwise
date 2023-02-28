package com.kanwise.user_service.service.user.implementation;

import com.kanwise.user_service.error.custom.user.validation.NotUniqueEmailException;
import com.kanwise.user_service.error.custom.user.validation.NotUniquePhoneNumberException;
import com.kanwise.user_service.error.custom.user.validation.NotUniqueUsernameException;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.command.EditUserCommand;
import com.kanwise.user_service.model.user.command.EditUserPartiallyCommand;
import com.kanwise.user_service.repository.user.UserRepository;
import com.kanwise.user_service.service.user.IUserEditValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class UserEditValidationService implements IUserEditValidationService {

    private final UserRepository userRepository;

    @Override
    public void validateEditUserCommand(EditUserCommand command, User user) {
        validateUsername(command.username(), user);
        validateEmail(command.email(), user);
        validatePhoneNumber(command.phoneNumber(), user);
    }

    @Override
    public void validateEditUserPartiallyCommand(EditUserPartiallyCommand command, User user) {
        ofNullable(command.username()).ifPresent(username -> validateUsername(username, user));
        ofNullable(command.email()).ifPresent(email -> validateEmail(email, user));
        ofNullable(command.phoneNumber()).ifPresent(phoneNumber -> validatePhoneNumber(phoneNumber, user));
    }

    private void validateUsername(String username, User user) {
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new NotUniqueUsernameException();
        }
    }

    private void validateEmail(String email, User user) {
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new NotUniqueEmailException();
        }
    }

    private void validatePhoneNumber(String phoneNumber, User user) {
        if (!user.getPhoneNumber().equals(phoneNumber) && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new NotUniquePhoneNumberException();
        }
    }
}
