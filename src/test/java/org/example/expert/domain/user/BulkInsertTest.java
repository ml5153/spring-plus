package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

@SpringBootTest
public class BulkInsertTest {

    // DB 연결을 관리하는 객체
    @Autowired
    private DataSource dataSource;

    @Test
    void 유저_500만건_bulk_insert() throws Exception {
        int totalCount = 5_000_000;
        int batchSize = 1_000;  // 1000개씩 묶어서

        String sql = "INSERT INTO users (email, password, user_role, nickname) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);  // 자동 커밋 끄기

            // FK 체크 끄고 TRUNCATE
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE users");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                conn.commit();
                System.out.println("기존 데이터 삭제 완료");
            }

            for (int i = 1; i <= totalCount; i++) {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                pstmt.setString(1, "user" + i + "@test.com");
                pstmt.setString(2, "password");
                pstmt.setString(3, "USER");
                pstmt.setString(4, uuid.substring(0, 20));  // 닉네임 20자로 자르기

                pstmt.addBatch();  // 배치에 추가

                if (i % batchSize == 0) {
                    pstmt.executeBatch();  // 1000개마다 실행
                    conn.commit();
                    pstmt.clearBatch();
                    System.out.println(i + "건 완료");
                }
            }

            // 남은 것 처리
            pstmt.executeBatch();
            conn.commit();
        }
    }
}

