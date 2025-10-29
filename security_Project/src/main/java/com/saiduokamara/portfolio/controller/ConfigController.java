ConfigControllerpackage com.saiduokamara.portfolio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    @Value("${spring.datasource.url:NOT_SET}")
    private String dbUrl;

    @Value("${spring.datasource.username:NOT_SET}")
    private String dbUsername;

    @Value("${DATABASE_URL:NOT_SET}")
    private String databaseUrlEnv;

    @Value("${DATABASE_USERNAME:NOT_SET}")
    private String databaseUsernameEnv;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of(
                "dbUrl", dbUrl,
                "dbUsername", dbUsername,
                "databaseUrlEnv", databaseUrlEnv,
                "databaseUsernameEnv", databaseUsernameEnv,
                "status", "Environment variables check"
        );
    }

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of(
                "status", "UP",
                "service", "Portfolio Backend",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }
}.java