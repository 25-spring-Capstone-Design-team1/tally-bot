package config;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EntityScan("com.tallybot.backend.tallybot_back.domain")
@Transactional
public abstract class DatabaseTestBase {
    private static MySQLContainer<?> initialize() {
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
            // For a list of exceptions thrown, see
            // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
            throw e;
        }

        String secret = getSecretValueResponse.secretString();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(secret);

            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            String host = jsonNode.get("host").asText();

            try (MySQLContainer<?> container = new MySQLContainer<>("mysql:8.0")) {
                        container.withCreateContainerCmdModifier(cmd -> {
                            String ip;
                            try {
                                ip = InetAddress.getByName(host).getHostAddress();
                            } catch (UnknownHostException e) {
                                throw new RuntimeException(e);
                            }

                            Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                                    new PortBinding(Ports.Binding.bindIpAndPort(ip, 3306),
                                            ExposedPort.tcp(3306))
                            );
                        })
                        .withDatabaseName("tallybot_test")
                        .withUsername(username)
                        .withPassword(password)
                        .withCommand(
                                "--character-set-server=utf8mb4",
                                "--collation-server=utf8mb4_bin",
                                "--lower_case_table_names=1"  // 테이블명 대소문자 구분 제거
                        );
                return container;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Container
    protected static final MySQLContainer<?> mysql = initialize();

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