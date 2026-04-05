package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.util.StringUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    /// SELECT *
    /// FROM todos t
    /// LEFT JOIN users u ON t.userid = u.id
    /// WHERE t.id = ?
    /// ;
    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user)
                .fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }


    /**
     * 쿼리 무한연습!!!
     SELECT t.tile, COUNT(DISTINICT m.id), COUNT(DISTINICT c.id)
     FROM todos t
     LEFT JOIN manager m ON m.todo_id = t.id
     LEFT JOIN users u ON u.id = m.user_id
     LEFT JOIN comment c ON c.todo_id = t.id
     WHERE t.title LIKE '% ${keyword}%'
     AND u.nickname LIKE '% ${nickname}'
     AND t.created_at >= '${startDate}'
     AND t.created_at <= '${endDate}'
     GROUP BY t.id
     ORDER BY t.created_at DESC
     LIMIT 10 OFFSET 0
     ;
     */

    @Override
    public Page<TodoSearchResponse> searchTodos(
            String keyword,
            String nickname,
            String startDate,
            String endDate,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        if (!StringUtils.isNullOrEmpty(keyword)) {
            builder.and(todo.title.containsIgnoreCase(keyword));
        }
        if (!StringUtils.isNullOrEmpty(nickname)) {
            builder.and(user.nickname.containsIgnoreCase(nickname));
        }
        if (!StringUtils.isNullOrEmpty(startDate)) {
            builder.and(todo.createdAt.goe(LocalDateTime.parse(startDate)));
        }
        if (!StringUtils.isNullOrEmpty(endDate)) {
            builder.and(todo.createdAt.loe(LocalDateTime.parse(endDate)));
        }

        List<TodoSearchResponse> results = jpaQueryFactory
                .select(
                        Projections.constructor(TodoSearchResponse.class,
                                todo.id,
                                todo.title,
                                manager.countDistinct().intValue(),
                                comment.countDistinct().intValue(),
                                todo.createdAt,
                                todo.modifiedAt
                        )
                )
                .from(todo)
                .leftJoin(manager).on(manager.todo.id.eq(todo.id))
                .leftJoin(user).on(user.id.eq(manager.user.id))
                .leftJoin(comment).on(comment.todo.id.eq(todo.id))
                .where(builder)
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetch();

        Long total = jpaQueryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(manager).on(manager.todo.eq(todo))
                .leftJoin(user).on(user.id.eq(manager.user.id))
                .where(builder)
                .fetchOne();
        // fetchOne()은 결과가 없으면 null을 반환해서 NPE가 터질수있어 null 예외처리 반드시 필!!
        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }
}
