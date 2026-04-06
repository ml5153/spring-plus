package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "log")
@NoArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long todoId;
    private Long userId;
    private Long managerUserId;
    private String result;
    private LocalDateTime createdAt;

    public Log(Long todoId, Long userId, Long managerUserId, String result) {
        this.todoId = todoId;
        this.userId = userId;
        this.managerUserId = managerUserId;
        this.result = result;
        this.createdAt = LocalDateTime.now();
    }
}

