package com.tallybot.backend.tallybot_back.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile({"rds", "prod"}) // RDS 사용하는 프로파일에서만 활성화
public class SecretsManagerConfig {

    @Value("${aws.secrets.db-secret-name:tallybot-test-db}")
    private String dbSecretName;

    @Value("${aws.region:ap-northeast-2}")
    private String awsRegion;

    @Bean
    public DataSource dataSource() {
        try {
            log.info("🔐 AWS Secrets Manager에서 DB 정보를 가져오는 중... (Secret: {})", dbSecretName);

            // Secrets Manager 클라이언트 생성
            SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                    .region(Region.of(awsRegion))
                    .build();

            // Secret 값 가져오기
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(dbSecretName)
                    .build();

            GetSecretValueResponse getSecretValueResponse = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = getSecretValueResponse.secretString();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode secretJson = objectMapper.readTree(secretString);

            // DB 연결 정보 추출
            String host = getJsonValue(secretJson, "host");
            String port = getJsonValue(secretJson, "port", "3306");
            String dbname = getJsonValue(secretJson, "dbname", "tallybot");
            String username = getJsonValue(secretJson, "username");
            String password = getJsonValue(secretJson, "password");

            // MySQL URL 생성
            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true",
                    host, port, dbname
            );

            log.info("✅ DB 연결 정보 설정 완료 - Host: {}, DB: {}, User: {}", host, dbname, username);

            // DataSource 생성
            return DataSourceBuilder.create()
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();

        } catch (Exception e) {
            log.error("❌ AWS Secrets Manager에서 DB 정보 가져오기 실패: {}", e.getMessage());
            throw new RuntimeException("Failed to configure DataSource from AWS Secrets Manager", e);
        }
    }

    /**
     * JSON에서 값 추출 (기본값 지원)
     */
    private String getJsonValue(JsonNode jsonNode, String key) {
        return getJsonValue(jsonNode, key, null);
    }

    private String getJsonValue(JsonNode jsonNode, String key, String defaultValue) {
        JsonNode valueNode = jsonNode.get(key);
        if (valueNode != null && !valueNode.isNull()) {
            return valueNode.asText();
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        throw new IllegalArgumentException("Required secret key not found: " + key);
    }
}