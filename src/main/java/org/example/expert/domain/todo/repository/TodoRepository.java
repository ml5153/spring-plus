package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // Q) @Query 즉 JPQL을 언제써야할까?
    // A) @Query가 없어도 동작은 한다만 JPA에서 쿼리를 자기맘대로 실행할 수 있어 예상치 못한 N+1오류가 발생할 수 있다.
    // 따라서 이름이 너무 길거나, N+1 문제를 해결(FETCH JOIN)해야 할 때: @Query를 달아서 JPQL을 직접 작성해야함.


    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    @Query("SELECT t FROM Todo t WHERE t.weather = :weather AND t.modifiedAt BETWEEN :startDateTime AND :endDateTime ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByWeatherAndModifiedAtBetween(String weather, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.weather = :weather ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByWeather(String weather, Pageable pageable);

    @Query("SELECT t FROM  Todo t WHERE t.modifiedAt BETWEEN :startDateTime AND :endDateTime ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByModifiedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
