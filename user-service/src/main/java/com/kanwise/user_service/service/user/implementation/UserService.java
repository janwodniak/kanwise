package com.kanwise.user_service.service.user.implementation;

import com.kanwise.clients.kanwise_service.member.MemberClient;
import com.kanwise.clients.kanwise_service.member.model.CreateMemberRequest;
import com.kanwise.clients.kanwise_service.member.model.EditMemberPartiallyCommand;
import com.kanwise.clients.report_service.subscriber.client.SubscriberClient;
import com.kanwise.clients.report_service.subscriber.model.CreateSubscriberCommand;
import com.kanwise.clients.report_service.subscriber.model.EditSubscriberPartiallyCommand;
import com.kanwise.user_service.error.custom.user.UserNotFoundException;
import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.UserPrincipal;
import com.kanwise.user_service.model.user.command.EditUserCommand;
import com.kanwise.user_service.model.user.command.EditUserPartiallyCommand;
import com.kanwise.user_service.repository.user.UserRepository;
import com.kanwise.user_service.service.notification.email.IEmailNotificationService;
import com.kanwise.user_service.service.password.generator.IPasswordGeneratorService;
import com.kanwise.user_service.service.user.IUserEditValidationService;
import com.kanwise.user_service.service.user.IUserService;
import com.netflix.discovery.shared.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.kanwise.user_service.constant.UserConstant.NO_USER_FOUND_BY_USERNAME;
import static com.kanwise.user_service.model.notification.email.EmailMessageType.ACCOUNT_CREATED;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService, UserDetailsService {

    private final UserRepository userRepository;
    private final MemberClient memberClient;
    private final SubscriberClient subscriberClient;
    private final IPasswordGeneratorService passwordGeneratorService;
    private final IEmailNotificationService<EmailRequest> emailNotificationService;
    private final IUserEditValidationService userEditValidationService;


    @Transactional(readOnly = true)
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserById(long id) {
        return userRepository.findActiveUserById(id).orElseThrow(UserNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @Override
    public User findByUsername(String username) {
        return userRepository.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }


    @Transactional(readOnly = true)
    @Override
    public Set<User> findUsersByUsernames(List<String> usernames) {
        return userRepository.findByUsernameIn(usernames);
    }

    @Override
    public Set<Image> findImagesByUserId(long id) {
        return userRepository.findActiveUserById(id).orElseThrow(UserNotFoundException::new).getImages();
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserByEmail(String username) {
        return userRepository.findUserByEmail(username).orElseThrow(UserNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<User> findUsers(String lastName, Pageable pageable) {
        return userRepository.findByLastNameContaining(lastName, pageable);
    }

    @Transactional
    @Override
    public User createUser(User user, boolean generatePassword, boolean notify) {
        createUserExternally(user);
        handlePasswordGeneration(user, generatePassword, notify);
        return userRepository.saveAndFlush(user);
    }

    private void createUserExternally(User user) {
        memberClient.addMember(CreateMemberRequest.builder().username(user.getUsername()).build());
        subscriberClient.addSubscriber(CreateSubscriberCommand.builder().username(user.getUsername()).email(user.getEmail()).build());
    }

    private void handlePasswordGeneration(User user, boolean generatePassword, boolean notify) {
        if (generatePassword) {
            Pair<String, String> passwordAndEncryptedPassword = passwordGeneratorService.generatePasswordAndEncryptedPassword();
            user.setPassword(passwordAndEncryptedPassword.second());
            if (notify) {
                sendPasswordEmail(user, passwordAndEncryptedPassword.first());
            }
        }
    }

    @Transactional
    @Override
    public void deleteUser(long id) {
        if (userRepository.existsActiveById(id)) {
            userRepository.softDeleteById(id);
        } else {
            throw new UserNotFoundException();
        }
    }

    @Transactional
    @Override
    public User editUser(long id, EditUserCommand command) {
        return userRepository.findActiveUserById(id).map(userToEdit -> {
            userEditValidationService.validateEditUserCommand(command, userToEdit);
            editUserInternally(command, userToEdit);
            editUserExternally(userToEdit);
            return userToEdit;
        }).orElseThrow(UserNotFoundException::new);
    }

    private void editUserInternally(EditUserCommand command, User userToEdit) {
        userToEdit.setFirstName(command.firstName());
        userToEdit.setLastName(command.lastName());
        userToEdit.setUsername(command.username());
        userToEdit.setEmail(command.email());
        userToEdit.setNotificationSubscriptions(command.notificationSubscriptions());
        userToEdit.setTwoFactorSubscriptions(command.twoFactorSubscriptions());
        userToEdit.setTwoFactorEnabled(command.twoFactorEnabled());
        userToEdit.setPhoneNumber(command.phoneNumber());
    }

    @Transactional
    @Override
    public User editUserPartially(long id, EditUserPartiallyCommand command) {
        return userRepository.findActiveUserById(id).map(userToEdit -> {
            userEditValidationService.validateEditUserPartiallyCommand(command, userToEdit);
            editUserPartiallyInternally(command, userToEdit);
            editUserExternally(userToEdit);
            return userToEdit;
        }).orElseThrow(UserNotFoundException::new);
    }

    private void editUserPartiallyInternally(EditUserPartiallyCommand command, User userToEdit) {
        ofNullable(command.firstName()).ifPresent(userToEdit::setFirstName);
        ofNullable(command.lastName()).ifPresent(userToEdit::setLastName);
        ofNullable(command.username()).ifPresent(userToEdit::setUsername);
        ofNullable(command.email()).ifPresent(userToEdit::setEmail);
        ofNullable(command.phoneNumber()).ifPresent(userToEdit::setPhoneNumber);
        ofNullable(command.twoFactorEnabled()).ifPresent(userToEdit::setTwoFactorEnabled);
        ofNullable(command.notificationSubscriptions()).ifPresent(map -> map.forEach((key, value) -> userToEdit.getNotificationSubscriptions().put(key, value)));
        ofNullable(command.twoFactorSubscriptions()).ifPresent(map -> map.forEach((key, value) -> userToEdit.getTwoFactorSubscriptions().put(key, value)));
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username));
    }

    private void editUserExternally(User user) {
        memberClient.editMemberPartially(user.getUsername(), EditMemberPartiallyCommand.builder().username(user.getUsername()).build());
        subscriberClient.editSubscriberPartially(user.getUsername(), EditSubscriberPartiallyCommand.builder().username(user.getUsername()).build());
    }

    private void sendPasswordEmail(User user, String password) {
        emailNotificationService.sendEmail(EmailRequest.builder()
                .to(user.getEmail())
                .subject("Account created 🥳")
                .isHtml(true)
                .type(ACCOUNT_CREATED)
                .data(Map.of("password", password, "firstName", user.getFirstName()))
                .build());
    }
}
