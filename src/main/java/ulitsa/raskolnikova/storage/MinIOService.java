package ulitsa.raskolnikova.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ApplicationScoped
public class MinIOService {

    private static final Logger logger = Logger.getLogger(MinIOService.class.getName());

    @Inject
    private MinIOConfig minIOConfig;

    public String saveFile(String fileName, InputStream inputStream, long fileSize, String contentType) {
        if (!minIOConfig.isEnabled()) {
            logger.warning("MinIO is disabled, file not saved: " + fileName);
            return null;
        }

        try {
            MinioClient client = minIOConfig.getMinioClient();
            String bucketName = minIOConfig.getBucketName();

            ensureBucketExists(client, bucketName);

            String objectName = generateObjectName(fileName);

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            client.putObject(putObjectArgs);

            String fileUrl = minIOConfig.getEndpoint() + "/" + bucketName + "/" + objectName;
            logger.info("File saved to MinIO: " + fileUrl);
            return fileUrl;

        } catch (MinioException e) {
            logger.severe("MinIO error saving file " + fileName + ": " + e.getMessage());
            throw new RuntimeException("Failed to save file to MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Error saving file to MinIO " + fileName + ": " + e.getMessage());
            throw new RuntimeException("Failed to save file to MinIO: " + e.getMessage(), e);
        }
    }

    public String saveFile(String fileName, InputStream inputStream, long fileSize) {
        return saveFile(fileName, inputStream, fileSize, null);
    }

    private void ensureBucketExists(MinioClient client, String bucketName) {
        try {
            boolean found = client.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!found) {
                client.makeBucket(io.minio.MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                logger.info("Created MinIO bucket: " + bucketName);
            }
        } catch (Exception e) {
            logger.warning("Error checking/creating bucket " + bucketName + ": " + e.getMessage());
        }
    }

    private String generateObjectName(String fileName) {
        long timestamp = System.currentTimeMillis();
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return timestamp + "_" + sanitizedFileName;
    }

    public InputStream getFile(String storagePath) {
        if (!minIOConfig.isEnabled()) {
            logger.warning("MinIO is disabled, cannot retrieve file: " + storagePath);
            throw new RuntimeException("MinIO is disabled");
        }

        try {
            MinioClient client = minIOConfig.getMinioClient();
            String bucketName = minIOConfig.getBucketName();

            String objectName = extractObjectName(storagePath);

            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            return client.getObject(getObjectArgs);

        } catch (MinioException e) {
            logger.severe("MinIO error retrieving file " + storagePath + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve file from MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Error retrieving file from MinIO " + storagePath + ": " + e.getMessage());
            throw new RuntimeException("Failed to retrieve file from MinIO: " + e.getMessage(), e);
        }
    }

    public String getFileContentType(String storagePath) {
        if (!minIOConfig.isEnabled()) {
            return "application/octet-stream";
        }

        try {
            MinioClient client = minIOConfig.getMinioClient();
            String bucketName = minIOConfig.getBucketName();
            String objectName = extractObjectName(storagePath);

            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            StatObjectResponse stat = client.statObject(statObjectArgs);
            return stat.contentType() != null ? stat.contentType() : "application/octet-stream";

        } catch (Exception e) {
            logger.warning("Error getting file content type: " + e.getMessage());
            return "application/octet-stream";
        }
    }

    public String getFileNameFromPath(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return "file";
        }

        String objectName = extractObjectName(storagePath);
        int underscoreIndex = objectName.indexOf('_');
        if (underscoreIndex > 0 && underscoreIndex < objectName.length() - 1) {
            return objectName.substring(underscoreIndex + 1);
        }
        return objectName;
    }

    private String extractObjectName(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            throw new IllegalArgumentException("Storage path cannot be null or empty");
        }

        String bucketName = minIOConfig.getBucketName();
        String endpoint = minIOConfig.getEndpoint();

        if (storagePath.startsWith(endpoint)) {
            String pathWithoutEndpoint = storagePath.substring(endpoint.length());
            if (pathWithoutEndpoint.startsWith("/")) {
                pathWithoutEndpoint = pathWithoutEndpoint.substring(1);
            }
            if (pathWithoutEndpoint.startsWith(bucketName + "/")) {
                return pathWithoutEndpoint.substring(bucketName.length() + 1);
            }
            return pathWithoutEndpoint;
        }

        if (storagePath.startsWith(bucketName + "/")) {
            return storagePath.substring(bucketName.length() + 1);
        }

        return storagePath;
    }

    public String generatePresignedDownloadUrl(String storagePath, int expiryInSeconds) {
        if (!minIOConfig.isEnabled()) {
            logger.warning("MinIO is disabled, cannot generate presigned URL");
            throw new RuntimeException("MinIO is disabled");
        }

        try {
            MinioClient client = minIOConfig.getMinioClient();
            String bucketName = minIOConfig.getBucketName();
            String objectName = extractObjectName(storagePath);

            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiryInSeconds, TimeUnit.SECONDS)
                    .build();

            String presignedUrl = client.getPresignedObjectUrl(args);
            logger.info("Generated presigned download URL for: " + storagePath);
            return presignedUrl;

        } catch (MinioException e) {
            logger.severe("MinIO error generating presigned download URL: " + e.getMessage());
            throw new RuntimeException("Failed to generate presigned download URL: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Error generating presigned download URL: " + e.getMessage());
            throw new RuntimeException("Failed to generate presigned download URL: " + e.getMessage(), e);
        }
    }
}

