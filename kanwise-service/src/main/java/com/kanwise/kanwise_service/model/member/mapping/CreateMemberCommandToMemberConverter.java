package com.kanwise.kanwise_service.model.member.mapping;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.command.CreateMemberCommand;
import com.kanwise.kanwise_service.model.notification.ProjectNotificationType;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.util.EnumMap;

import static java.lang.Boolean.TRUE;

@Service
public class CreateMemberCommandToMemberConverter implements Converter<CreateMemberCommand, Member> {
    @Override
    public Member convert(MappingContext<CreateMemberCommand, Member> mappingContext) {
        CreateMemberCommand command = mappingContext.getSource();
        return Member.builder()
                .username(command.username())
                .notificationSubscriptions(generateDefaultNotificationSubscriptions())
                .active(TRUE)
                .build();
    }

    private EnumMap<ProjectNotificationType, Boolean> generateDefaultNotificationSubscriptions() {
        EnumMap<ProjectNotificationType, Boolean> subscriptions = new EnumMap<>(ProjectNotificationType.class);
        for (ProjectNotificationType type : ProjectNotificationType.values()) {
            subscriptions.put(type, TRUE);
        }
        return subscriptions;
    }
}
