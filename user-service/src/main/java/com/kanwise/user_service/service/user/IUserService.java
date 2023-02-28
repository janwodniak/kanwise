package com.kanwise.user_service.service.user;

import com.kanwise.user_service.model.image.Image;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.command.EditUserCommand;
import com.kanwise.user_service.model.user.command.EditUserPartiallyCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IUserService {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User findUserById(long id);

    Page<User> findUsers(String lastName, Pageable pageable);

    User createUser(User user, boolean generatePassword, boolean notify);

    void deleteUser(long id);

    User editUser(long id, EditUserCommand command);

    User editUserPartially(long id, EditUserPartiallyCommand command);

    User findUserByEmail(String email);

    User findByUsername(String username);

    Optional<User> findUserByUsername(String username);

    Set<User> findUsersByUsernames(List<String> usernames);

    Set<Image> findImagesByUserId(long id);
}
