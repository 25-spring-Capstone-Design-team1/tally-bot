package config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EntityScan("com.tallybot.backend.tallybot_back.domain")
@Transactional
public abstract class DatabaseTestBase {
    @Container
    protected static final MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("testdb")
                    .withUsername("testtbot")
                    .withPassword("Kagayaki4572")
                    .withCommand(
                            "--character-set-server=utf8mb4",
                            "--collation-server=utf8mb4_bin",
                            "--lower_case_table_names=1"  // 테이블명 대소문자 구분 제거
                    );
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        System.out.println("=== DatabaseTestBase 설정 시작 ===");
        System.out.println("MySQL URL: " + mysql.getJdbcUrl());
        // 기본 데이터소스 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        System.out.println("=== DatabaseTestBase 설정 완료 ===");
    }
}