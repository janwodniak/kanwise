package com.kanwise.user_service.model.user.command;

import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.validation.annotation.common.ClassFields;
import com.kanwise.user_service.validation.annotation.common.ValueOfEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Min;

@Getter
@Setter
public class CreateUserPageCommand {
    @Min(value = 0, message = "PAGE_NUMBER_NOT_NEGATIVE")
    private int pageNumber = 0;
    @Min(value = 1, message = "PAGE_SIZE_NOT_LESS_THAN_ONE")
    private int pageSize = 5;
    @ValueOfEnum(enumClass = Sort.Direction.class, message = "INVALID_SORT_DIRECTION")
    private String sortDirection = "ASC";
    @ClassFields(fieldsSource = User.class, message = "INVALID_SORT_BY_VALUE")
    private String sortBy = "id";
}