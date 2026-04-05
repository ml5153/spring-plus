package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class TodoSearchResponse {
    private final Long id;
    private final String title;
    private final Integer managerCount;
    private final Integer commentCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
