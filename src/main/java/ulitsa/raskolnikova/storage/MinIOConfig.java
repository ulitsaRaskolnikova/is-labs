package ulitsa.raskolnikova.storage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.minio.MinioClient;

import java.util.logging.Logger;

@ApplicationScoped
public class MinIOConfig {

    private static final Logger logger = Logger.getLogger(MinIOConfig.class.getName());

    private MinioClient minioClient;
    private String bucketName;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private boolean enabled;

    @PostConstruct
    public void init() {
        endpoint = System.getenv("MINIO_ENDPOINT");
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = System.getProperty("minio.endpoint", "http://localhost:9000");
        }

        accessKey = System.getenv("MINIO_ACCESS_KEY");
        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = System.getProperty("minio.accessKey", "minioadmin");
        }

        secretKey = System.getenv("MINIO_SECRET_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = System.getProperty("minio.secretKey", "minioadmin");
        }

        bucketName = System.getenv("MINIO_BUCKET");
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = System.getProperty("minio.bucket", "file-imports");
        }

        String enabledStr = System.getenv("MINIO_ENABLED");
        if (enabledStr == null || enabledStr.isEmpty()) {
            enabledStr = System.getProperty("minio.enabled", "true");
        }
        enabled = Boolean.parseBoolean(enabledStr);

        if (enabled) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials(accessKey, secretKey)
                        .build();

                logger.info("MinIO client initialized. Endpoint: " + endpoint + ", Bucket: " + bucketName);
            } catch (Exception e) {
                logger.warning("Failed to initialize MinIO client: " + e.getMessage());
                enabled = false;
            }
        } else {
            logger.info("MinIO storage is disabled");
        }
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isEnabled() {
        return enabled && minioClient != null;
    }
}

