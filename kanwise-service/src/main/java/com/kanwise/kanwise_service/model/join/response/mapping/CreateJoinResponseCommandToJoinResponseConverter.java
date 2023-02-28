package com.kanwise.kanwise_service.model.join.response.mapping;

import com.kanwise.kanwise_service.model.join.response.JoinResponse;
import com.kanwise.kanwise_service.model.join.response.command.CreateJoinResponseCommand;
import com.kanwise.kanwise_service.service.join.request.IJoinRequestService;
import com.kanwise.kanwise_service.service.member.IMemberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static com.kanwise.kanwise_service.model.join.request.JoinRequestStatus.valueOf;
import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class CreateJoinResponseCommandToJoinResponseConverter implements Converter<CreateJoinResponseCommand, JoinResponse> {

    private final IJoinRequestService joinRequestService;
    private final IMemberService memberService;
    private final Clock clock;

    @Override
    public JoinResponse convert(MappingContext<CreateJoinResponseCommand, JoinResponse> mappingContext) {
        JoinResponse joinResponse = JoinResponse.builder()
                .message(mappingContext.getSource().message())
                .respondedAt(now(clock))
                .status(valueOf(mappingContext.getSource().status().toUpperCase()))
                .build();

        joinResponse.setJoinRequest(joinRequestService.findJoinRequestById(mappingContext.getSource().joinRequestId()));
        joinResponse.setRespondedBy(memberService.findMemberByUsername(mappingContext.getSource().respondedByUsername()));

        return joinResponse;
    }
}
