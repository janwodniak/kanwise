package com.kanwise.user_service.service.user;

import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.command.EditUserCommand;
import com.kanwise.user_service.model.user.command.EditUserPartiallyCommand;

public interface IUserEditValidationService {

    void validateEditUserCommand(EditUserCommand command, User user);

    void validateEditUserPartiallyCommand(EditUserPartiallyCommand command, User user);
}
