package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.UUID;

/**
 * 위 코드는 동료코드다. (공부용)
 1. CSV 파일 생성
    500만 건의 유저 데이터를 쉼표로 구분된 텍스트 파일로 저장
     BufferedWriter로 대용량 파일을 빠르게 씀

 2. LOAD DATA INFILE로 MySQL에 적재
 Java에서 INSERT 반복 대신, MySQL이 CSV 파일을 직접 읽어서 한 번에 INSERT
 네트워크 통신을 1번만 하면되어서  Batch Insert보다 훨씬 빠름
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BulkInsertTest2 {

    // 요구사황에서 꼭 JDBC 템플릿을 쓰라곤 안했다. DataSource의 Connection을 사용하면 원래 그게 JDBC를 직접 사용 하는 거다ㅇㅇ
    @Autowired
    private DataSource dataSource;

    private static final String FILE_PATH = "users.csv";

    @Test
    void bulkInsertUsers() throws Exception {

        int total = 5_000_000;

        long start = System.currentTimeMillis();

        generateCsv(total);
        loadData();

        long end = System.currentTimeMillis();

        System.out.println("총 소요 시간: " + (end - start) / 1000 + "초");
    }


    private void generateCsv(int total) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH), 1024 * 1024)) {

            writer.write("created_at,modified_at,email,password,user_role,nickname\n");


            String now = "2026-04-06 00:00:00";
            String nickname = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

            for (int i = 1; i <= total; i++) {

                writer.write(now);
                writer.write(",");
                writer.write(now);
                writer.write(",");
                writer.write("user" + i + "@test.com");
                writer.write(",");
                writer.write("1234");
                writer.write(",");
                writer.write((i % 2 == 0) ? "USER" : "ADMIN");
                writer.write(",");
                writer.write(nickname);
                writer.write("\n");

                if (i % 100_000 == 0) {
                    System.out.println(i + "건 생성 완료");
                }
            }
        }
    }


    private void loadData() throws Exception {

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {


            stmt.execute("SET foreign_key_checks = 0;");
            stmt.execute("SET autocommit = 0;");
            dropIndexes(stmt);

            // 기존 데이터 제거
            stmt.execute("TRUNCATE TABLE spring_plus.users");

            String sql = "LOAD DATA LOCAL INFILE '" + FILE_PATH + "' " +
                    "INTO TABLE spring_plus.users " +
                    "FIELDS TERMINATED BY ',' " +
                    "LINES TERMINATED BY '\\n' " +
                    "IGNORE 1 ROWS " +
                    "(created_at, modified_at, email, password, user_role, nickname);";

            stmt.execute(sql);

            createIndexes(stmt);
            stmt.execute("SET autocommit = 1;");
            stmt.execute("SET foreign_key_checks = 1;");
        }
    }

    private void dropIndexes(Statement stmt) throws Exception {
        try {
            stmt.execute("ALTER TABLE spring_plus.users DROP INDEX uk_users_email");
            stmt.execute("ALTER TABLE spring_plus.users DROP INDEX uk_users_email");
        } catch (SQLSyntaxErrorException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createIndexes(Statement stmt) throws Exception {
        try {
            stmt.execute("CREATE INDEX idx_users_nickname ON spring_plus.users (nickname)");
            stmt.execute("ALTER TABLE spring_plus.users ADD UNIQUE (email)");
        } catch (SQLSyntaxErrorException e) {
            System.out.println(e.getMessage());
        }
    }
}
