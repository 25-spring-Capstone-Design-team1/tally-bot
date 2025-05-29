package config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.IOException;

@SpringBootTest
@ActiveProfiles("test")
@EntityScan("com.tallybot.backend.tallybot_back.domain")
@Transactional
public abstract class DatabaseTestBase {

    private static DatabaseConfig getDatabaseConfig() {
        String secretName = "tallybot-test-db";
        Region region = Region.of("ap-northeast-2");

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw new RuntimeException("AWS Secrets Manager에서 데이터베이스 설정을 가져오는데 실패했습니다.", e);
        }

        String secret = getSecretValueResponse.secretString();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(secret);

            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            String host = jsonNode.get("host").asText();

            // 포트와 데이터베이스명 추가 (필요시)
            String port = jsonNode.has("port") ? jsonNode.get("port").asText() : "3306";
            String database = jsonNode.has("database") ? jsonNode.get("database").asText() : "tallybot_test";

            return new DatabaseConfig(username, password, host, port, database);

        } catch (IOException e) {
            throw new RuntimeException("JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        System.out.println("=== DatabaseTestBase 설정 시작 ===");

        DatabaseConfig config = getDatabaseConfig();

        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul",
                config.host, config.port, config.database);

        System.out.println("MySQL URL: " + jdbcUrl);

        // 기본 데이터소스 설정
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> config.username);
        registry.add("spring.datasource.password", () -> config.password);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // JPA 설정 (테스트 환경에 맞게 조정)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");

        System.out.println("=== DatabaseTestBase 설정 완료 ===");
    }

    // 내부 클래스로 설정 정보 관리
    private static class DatabaseConfig {
        final String username;
        final String password;
        final String host;
        final String port;
        final String database;

        DatabaseConfig(String username, String password, String host, String port, String database) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
            this.database = database;
        }
    }
}