package ulitsa.raskolnikova.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.logging.Logger;

@ApplicationScoped
public class HikariCPConfig {

    private static final Logger logger = Logger.getLogger(HikariCPConfig.class.getName());

    @Produces
    @ApplicationScoped
    public HikariDataSource createHikariDataSource() {
        HikariConfig config = new HikariConfig();

        String dbHost = getEnvOrProperty("HIKARI_DB_HOST", "hikari.db.host", "localhost");
        String dbPort = getEnvOrProperty("HIKARI_DB_PORT", "hikari.db.port", "5432");
        String dbName = getEnvOrProperty("HIKARI_DB_NAME", "hikari.db.name", "postgres");
        String socketTimeout = getEnvOrProperty("HIKARI_SOCKET_TIMEOUT", "hikari.socket.timeout", "5");
        String connectTimeout = getEnvOrProperty("HIKARI_CONNECT_TIMEOUT", "hikari.connect.timeout", "5");
        
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?socketTimeout=%s&connectTimeout=%s",
                dbHost, dbPort, dbName, socketTimeout, connectTimeout);
        config.setJdbcUrl(jdbcUrl);

        config.setUsername(getEnvOrProperty("HIKARI_DB_USER", "hikari.db.user", "postgres"));
        config.setPassword(getEnvOrProperty("HIKARI_DB_PASSWORD", "hikari.db.password", "postgres"));

        config.setDriverClassName("org.postgresql.Driver");

        config.setMinimumIdle(getIntEnvOrProperty("HIKARI_MIN_IDLE", "hikari.min.idle", 5));
        config.setMaximumPoolSize(getIntEnvOrProperty("HIKARI_MAX_POOL_SIZE", "hikari.max.pool.size", 20));

        config.setConnectionTimeout(getIntEnvOrProperty("HIKARI_CONNECTION_TIMEOUT", "hikari.connection.timeout", 5000));
        config.setIdleTimeout(getIntEnvOrProperty("HIKARI_IDLE_TIMEOUT", "hikari.idle.timeout", 600000));
        config.setMaxLifetime(getIntEnvOrProperty("HIKARI_MAX_LIFETIME", "hikari.max.lifetime", 1800000));
        config.setValidationTimeout(getIntEnvOrProperty("HIKARI_VALIDATION_TIMEOUT", "hikari.validation.timeout", 3000));

        config.setLeakDetectionThreshold(getIntEnvOrProperty("HIKARI_LEAK_DETECTION_THRESHOLD",
                "hikari.leak.detection.threshold", 60000));

        config.setPoolName(getEnvOrProperty("HIKARI_POOL_NAME", "hikari.pool.name", "HikariCP-Pool"));

        HikariDataSource dataSource = new HikariDataSource(config);
        
        logger.info(String.format("HikariCP DataSource created: jdbcUrl=%s, poolName=%s, minIdle=%d, maxPoolSize=%d",
                jdbcUrl, config.getPoolName(), config.getMinimumIdle(), config.getMaximumPoolSize()));
        
        return dataSource;
    }

    private String getEnvOrProperty(String envKey, String propKey, String defaultValue) {
        String value = System.getenv(envKey);
        if (value == null || value.isEmpty()) {
            value = System.getProperty(propKey);
        }
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private int getIntEnvOrProperty(String envKey, String propKey, int defaultValue) {
        String value = getEnvOrProperty(envKey, propKey, null);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warning(String.format("Invalid integer value for %s/%s: %s, using default: %d", 
                        envKey, propKey, value, defaultValue));
            }
        }
        return defaultValue;
    }
}

