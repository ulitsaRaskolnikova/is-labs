package ulitsa.raskolnikova.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import ulitsa.raskolnikova.entity.CoordinatesEntity;
import ulitsa.raskolnikova.entity.FileImportHistoryEntity;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.entity.PersonEntity;
import ulitsa.raskolnikova.model.*;
import ulitsa.raskolnikova.qualifier.CoordinatesRepo;
import ulitsa.raskolnikova.qualifier.FileImportHistoryRepo;
import ulitsa.raskolnikova.qualifier.LocationRepo;
import ulitsa.raskolnikova.qualifier.PersonRepo;
import ulitsa.raskolnikova.repository.CoordinatesRepository;
import ulitsa.raskolnikova.repository.CrudRepository;
import ulitsa.raskolnikova.repository.FileImportHistoryRepository;
import ulitsa.raskolnikova.repository.LocationRepository;
import ulitsa.raskolnikova.service.FileUploadService;
import ulitsa.raskolnikova.storage.MinIOService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ApplicationScoped
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = Logger.getLogger(FileUploadServiceImpl.class.getName());

    @Inject
    @LocationRepo
    private LocationRepository locationRepository;

    @Inject
    @CoordinatesRepo
    private CoordinatesRepository coordinatesRepository;

    @Inject
    @PersonRepo
    private CrudRepository<PersonEntity> personRepository;

    @Inject
    @FileImportHistoryRepo
    private FileImportHistoryRepository fileImportHistoryRepository;

    @Inject
    private MinIOService minIOService;

    @Inject
    private UserTransaction userTransaction;

    @Transactional
    @Override
    public FileUploadResult processFile(String fileName, InputStream inputStream) {
        return processFile(fileName, inputStream, null);
    }

    @Override
    public FileUploadResult processFile(String fileName, InputStream inputStream, String contentType) {
        logger.info("Processing file: " + fileName);
        
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            byte[] fileData = readInputStreamToByteArray(inputStream);
            logger.info("File read successfully, size: " + (fileData != null ? fileData.length : 0) + " bytes");
            InputStream processingStream = new ByteArrayInputStream(fileData);
            
            if (fileName.endsWith(".csv")) {
                logger.info("File is CSV, processing CSV file...");
                FileUploadResult result = processCsvFile(processingStream, fileName, fileData, contentType);
                return result;
            } else if (fileName.endsWith(".zip")) {
                logger.info("File is ZIP, processing ZIP archive...");
                FileUploadResult result = processZipFile(processingStream, fileName, fileData, contentType);
                return result;
            } else {
                logger.warning("Unsupported file type: " + fileName);
                errors.add(new ValidationError(0, fileName, null, "Unsupported file type. Only CSV and ZIP files are allowed.", null));
                FileUploadResult result = new FileUploadResult(0, 1, errors);
                try {
                    saveImportHistoryInTransaction(fileName, result, null);
                } catch (Exception e) {
                    logger.warning("Failed to save import history for unsupported file type: " + e.getMessage());
                }
                return result;
            }
        } catch (RuntimeException e) {
            if (isDatabaseError(e)) {
                logger.severe("Database error processing file " + fileName + ": " + e.getMessage());
                throw e;
            }
            logger.severe("Error processing file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            errors.add(new ValidationError(0, fileName, null, "Error processing file: " + e.getMessage(), null));
            FileUploadResult result = new FileUploadResult(0, 1, errors);
            try {
                saveImportHistoryInTransaction(fileName, result, null);
            } catch (Exception saveException) {
                if (isDatabaseError(saveException)) {
                    logger.severe("Database error saving import history: " + saveException.getMessage());
                    throw new RuntimeException("Database is unavailable: " + saveException.getMessage(), saveException);
                }
                logger.warning("Failed to save import history: " + saveException.getMessage());
            }
            return result;
        } catch (Exception e) {
            logger.severe("Error processing file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            errors.add(new ValidationError(0, fileName, null, "Error processing file: " + e.getMessage(), null));
            FileUploadResult result = new FileUploadResult(0, 1, errors);
            try {
                saveImportHistoryInTransaction(fileName, result, null);
            } catch (Exception saveException) {
                if (isDatabaseError(saveException)) {
                    logger.severe("Database error saving import history: " + saveException.getMessage());
                    throw new RuntimeException("Database is unavailable: " + saveException.getMessage(), saveException);
                }
                logger.warning("Failed to save import history: " + saveException.getMessage());
            }
            return result;
        }
    }

    private boolean isDatabaseError(Throwable e) {
        if (e == null) {
            return false;
        }
        
        Throwable cause = e.getCause();
        if (cause == null) {
            cause = e;
        }
        
        String message = cause.getMessage();
        if (message == null) {
            message = "";
        }
        
        return e instanceof java.sql.SQLTimeoutException ||
               cause instanceof java.sql.SQLTimeoutException ||
               e instanceof java.util.concurrent.TimeoutException ||
               cause instanceof java.util.concurrent.TimeoutException ||
               (cause instanceof java.sql.SQLException && 
                (message.contains("timeout") || 
                 message.contains("Timeout") ||
                 message.contains("Connection refused") ||
                 message.contains("could not connect") ||
                 message.contains("Connection to") ||
                 message.contains("is not available"))) ||
               cause instanceof java.net.ConnectException ||
               (e.getMessage() != null && e.getMessage().contains("Database is unavailable"));
    }
    
    private byte[] readInputStreamToByteArray(InputStream inputStream) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private FileUploadResult processZipFile(InputStream zipStream, String zipFileName, byte[] fileData, String contentType) {
        List<ValidationError> allErrors = new ArrayList<>();
        List<List<ValidatedRecord>> allValidatedRecords = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".csv")) {
                    String currentFileName = zipFileName + "/" + entry.getName();
                    logger.info("Processing CSV from ZIP: " + entry.getName());
                    
                    List<RecordWithLineNumber> records = parseCsvRecords(zis, currentFileName, allErrors);
                    if (!allErrors.isEmpty()) {
                        continue;
                    }
                    
                    List<ValidatedRecord> validatedRecords = new ArrayList<>();
                    for (RecordWithLineNumber recordWithLine : records) {
                        ValidatedRecord validated = validateRecord(recordWithLine.record, recordWithLine.lineNumber, currentFileName, recordWithLine.originalLine, allErrors);
                        if (validated != null) {
                            validatedRecords.add(validated);
                        }
                    }
                    
                    if (!allErrors.isEmpty()) {
                        continue;
                    }
                    
                    allValidatedRecords.add(validatedRecords);
                    fileNames.add(currentFileName);
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            logger.severe("Error processing ZIP file: " + e.getMessage());
            allErrors.add(new ValidationError(0, zipFileName, null, "Error reading ZIP file: " + e.getMessage(), null));
        }
        
        if (!allErrors.isEmpty()) {
            logger.info("Validation errors found in ZIP file, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, allErrors.size(), allErrors);
            saveImportHistoryInTransaction(zipFileName, result, null);
            return result;
        }
        
        List<ValidatedRecord> allRecords = new ArrayList<>();
        for (List<ValidatedRecord> fileRecords : allValidatedRecords) {
            allRecords.addAll(fileRecords);
        }
        checkUniquenessInFile(allRecords, zipFileName, allErrors);
        
        if (!allErrors.isEmpty()) {
            logger.info("Uniqueness errors found in ZIP file, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, allErrors.size(), allErrors);
            saveImportHistoryInTransaction(zipFileName, result, null);
            return result;
        }
        
        try {
            checkExistenceInDatabaseInTransaction(allRecords, zipFileName, allErrors);
        } catch (RuntimeException e) {
            if (isDatabaseError(e)) {
                throw e;
            }
            throw e;
        }
        
        if (!allErrors.isEmpty()) {
            logger.info("Existence errors found in database, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, allErrors.size(), allErrors);
            try {
                saveImportHistoryInTransaction(zipFileName, result, null);
            } catch (RuntimeException e) {
                if (isDatabaseError(e)) {
                    throw e;
                }
                logger.warning("Failed to save import history: " + e.getMessage());
            }
            return result;
        }
        
        String storagePath = null;
        boolean fileSavedToMinIO = false;

        try {
            if (fileData != null && fileData.length > 0) {
                try {
                    logger.info("Phase 1 (Prepare): Attempting to save file to MinIO, size: " + fileData.length + " bytes");
                    storagePath = minIOService.saveFile(zipFileName, new ByteArrayInputStream(fileData), fileData.length, contentType);
                    if (storagePath != null) {
                        fileSavedToMinIO = true;
                        logger.info("Phase 1 (Prepare): File saved to MinIO: " + storagePath);
                    } else {
                        logger.warning("Phase 1 (Prepare): MinIO returned null storage path (MinIO may be disabled)");
                    }
                } catch (Exception e) {
                    logger.warning("Phase 1 (Prepare): Failed to save file to MinIO: " + e.getMessage());
                    e.printStackTrace();
                    String userFriendlyMessage = getMinIOErrorMessage(e);
                    allErrors.add(new ValidationError(0, zipFileName, null, userFriendlyMessage, null));
                    FileUploadResult result = new FileUploadResult(0, allErrors.size(), allErrors);
                    try {
                        saveImportHistoryInTransaction(zipFileName, result, null);
                    } catch (RuntimeException dbError) {
                        if (isDatabaseError(dbError)) {
                            throw dbError;
                        }
                        logger.warning("Failed to save import history: " + dbError.getMessage());
                    }
                    return result;
                }
            } else {
                logger.warning("Phase 1 (Prepare): File data is null or empty, skipping MinIO save");
            }

            saveRecordsInTransaction(allRecords, allErrors);
            int totalProcessed = allRecords.size() - allErrors.size();

            FileUploadResult result = new FileUploadResult(totalProcessed, allErrors.size(), allErrors);

            if (result.getErrorCount() > 0 && fileSavedToMinIO && storagePath != null) {
                try {
                    logger.info("Phase 3 (Rollback): Rolling back - deleting file from MinIO due to database errors");
                    minIOService.deleteFile(storagePath);
                    storagePath = null;
                    fileSavedToMinIO = false;
                } catch (Exception e) {
                    logger.severe("Phase 3 (Rollback): Failed to delete file from MinIO during rollback: " + e.getMessage());
                }
            }

            if (result.getErrorCount() == 0) {
                if (fileSavedToMinIO && storagePath != null) {
                    logger.info("Phase 2 (Commit): All records saved successfully, file remains in MinIO: " + storagePath);
                } else {
                    logger.warning("Phase 2 (Commit): All records saved successfully, but file was not saved to MinIO (MinIO may be disabled or file was empty)");
                }
            }

            try {
                logger.info("Saving import history with storagePath: " + (storagePath != null ? storagePath : "null"));
                saveImportHistoryInTransaction(zipFileName, result, storagePath);
            } catch (RuntimeException e) {
                if (isDatabaseError(e)) {
                    throw e;
                }
                logger.warning("Failed to save import history: " + e.getMessage());
            }
            return result;

        } catch (Exception e) {
            if (fileSavedToMinIO && storagePath != null) {
                try {
                    logger.severe("Phase 3 (Rollback): Exception occurred, rolling back - deleting file from MinIO");
                    minIOService.deleteFile(storagePath);
                } catch (Exception rollbackException) {
                    logger.severe("Phase 3 (Rollback): Failed to delete file from MinIO during rollback: " + rollbackException.getMessage());
                }
            }
            if (isDatabaseError(e)) {
                logger.severe("Database error in processZipFile: " + e.getMessage());
                throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    private List<RecordWithLineNumber> parseCsvRecords(InputStream csvStream, String fileName, List<ValidationError> errors) {
        List<RecordWithLineNumber> records = new ArrayList<>();
        
        InputStream nonClosingStream = new FilterInputStream(csvStream) {
            @Override
            public void close() {
            }
        };
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(nonClosingStream, StandardCharsets.UTF_8))) {
            String line;
            String[] headers = null;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                String originalLine = line;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] values;
                try {
                    values = parseCsvLine(line);
                } catch (Exception e) {
                    errors.add(new ValidationError(lineNumber, fileName, null, "Error parsing CSV line: " + e.getMessage(), originalLine));
                    continue;
                }

                if (lineNumber == 1) {
                    headers = values;
                    continue;
                }

                if (headers == null) {
                    errors.add(new ValidationError(lineNumber, fileName, null, "No headers found in CSV file", originalLine));
                    continue;
                }

                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                    record.put(headers[i].toLowerCase().trim(), values[i].trim());
                }
                records.add(new RecordWithLineNumber(lineNumber, record, originalLine));
            }
        } catch (Exception e) {
            logger.severe("Error reading CSV file: " + e.getMessage());
            errors.add(new ValidationError(0, fileName, null, "Error reading CSV file: " + e.getMessage(), null));
        }
        
        return records;
    }

    private FileUploadResult processCsvFile(InputStream csvStream, String fileName, byte[] fileData, String contentType) {
        List<ValidationError> errors = new ArrayList<>();
        
        List<RecordWithLineNumber> records = parseCsvRecords(csvStream, fileName, errors);
        if (!errors.isEmpty()) {
            FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
            saveImportHistoryInTransaction(fileName, result, null);
            return result;
        }

        List<ValidatedRecord> validatedRecords = new ArrayList<>();
        for (RecordWithLineNumber recordWithLine : records) {
            ValidatedRecord validated = validateRecord(recordWithLine.record, recordWithLine.lineNumber, fileName, recordWithLine.originalLine, errors);
            if (validated != null) {
                validatedRecords.add(validated);
            }
        }

        if (!errors.isEmpty()) {
            logger.info("Validation errors found, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
            saveImportHistoryInTransaction(fileName, result, null);
            return result;
        }

        checkUniquenessInFile(validatedRecords, fileName, errors);

        if (!errors.isEmpty()) {
            logger.info("Uniqueness errors found in file, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
            saveImportHistoryInTransaction(fileName, result, null);
            return result;
        }

        try {
            checkExistenceInDatabaseInTransaction(validatedRecords, fileName, errors);
        } catch (RuntimeException e) {
            if (isDatabaseError(e)) {
                throw e;
            }
            throw e;
        }

        if (!errors.isEmpty()) {
            logger.info("Existence errors found in database, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
            try {
                saveImportHistoryInTransaction(fileName, result, null);
            } catch (RuntimeException e) {
                if (isDatabaseError(e)) {
                    throw e;
                }
                logger.warning("Failed to save import history: " + e.getMessage());
            }
            return result;
        }

        String storagePath = null;
        boolean fileSavedToMinIO = false;

        try {
            if (fileData != null && fileData.length > 0) {
                try {
                    logger.info("Phase 1 (Prepare): Attempting to save file to MinIO, size: " + fileData.length + " bytes");
                    storagePath = minIOService.saveFile(fileName, new ByteArrayInputStream(fileData), fileData.length, contentType);
                    if (storagePath != null) {
                        fileSavedToMinIO = true;
                        logger.info("Phase 1 (Prepare): File saved to MinIO: " + storagePath);
                    } else {
                        logger.warning("Phase 1 (Prepare): MinIO returned null storage path (MinIO may be disabled)");
                    }
                } catch (Exception e) {
                    logger.warning("Phase 1 (Prepare): Failed to save file to MinIO: " + e.getMessage());
                    e.printStackTrace();
                    String userFriendlyMessage = getMinIOErrorMessage(e);
                    errors.add(new ValidationError(0, fileName, null, userFriendlyMessage, null));
                    FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
                    try {
                        saveImportHistoryInTransaction(fileName, result, null);
                    } catch (RuntimeException dbError) {
                        if (isDatabaseError(dbError)) {
                            throw dbError;
                        }
                        logger.warning("Failed to save import history: " + dbError.getMessage());
                    }
                    return result;
                }
            } else {
                logger.warning("Phase 1 (Prepare): File data is null or empty, skipping MinIO save");
            }

            saveRecordsInTransaction(validatedRecords, errors);
            int processedCount = validatedRecords.size() - errors.size();

            FileUploadResult result = new FileUploadResult(processedCount, errors.size(), errors);

            if (result.getErrorCount() > 0 && fileSavedToMinIO && storagePath != null) {
                try {
                    logger.info("Phase 3 (Rollback): Rolling back - deleting file from MinIO due to database errors");
                    minIOService.deleteFile(storagePath);
                    storagePath = null;
                    fileSavedToMinIO = false;
                } catch (Exception e) {
                    logger.severe("Phase 3 (Rollback): Failed to delete file from MinIO during rollback: " + e.getMessage());
                }
            }

            if (result.getErrorCount() == 0) {
                if (fileSavedToMinIO && storagePath != null) {
                    logger.info("Phase 2 (Commit): All records saved successfully, file remains in MinIO: " + storagePath);
                } else {
                    logger.warning("Phase 2 (Commit): All records saved successfully, but file was not saved to MinIO (MinIO may be disabled or file was empty)");
                }
            }

            try {
                logger.info("Saving import history with storagePath: " + (storagePath != null ? storagePath : "null"));
                saveImportHistoryInTransaction(fileName, result, storagePath);
            } catch (RuntimeException e) {
                if (isDatabaseError(e)) {
                    throw e;
                }
                logger.warning("Failed to save import history: " + e.getMessage());
            }
            return result;

        } catch (Exception e) {
            if (fileSavedToMinIO && storagePath != null) {
                try {
                    logger.severe("Phase 3 (Rollback): Exception occurred, rolling back - deleting file from MinIO");
                    minIOService.deleteFile(storagePath);
                } catch (Exception rollbackException) {
                    logger.severe("Phase 3 (Rollback): Failed to delete file from MinIO during rollback: " + rollbackException.getMessage());
                }
            }
            if (isDatabaseError(e)) {
                logger.severe("Database error in processZipFile: " + e.getMessage());
                throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    private void saveImportHistoryInTransaction(String fileName, FileUploadResult result) {
        saveImportHistoryInTransaction(fileName, result, null);
    }

    private void saveImportHistoryInTransaction(String fileName, FileUploadResult result, String storagePath) {
        try {
            userTransaction.begin();
            try {
                FileImportHistoryEntity history = new FileImportHistoryEntity();
                history.setFileName(fileName);
                history.setErrorCount(result.getErrorCount());
                history.setStoragePath(storagePath);
                
                if (result.getErrorCount() == 0) {
                    history.setStatus("SUCCESS");
                    history.setProcessedCount(result.getProcessedCount());
                } else {
                    history.setStatus("FAILED");
                    history.setProcessedCount(null);
                }
                
                fileImportHistoryRepository.save(history);
                logger.info("Saved import history for file: " + fileName);
                userTransaction.commit();
            } catch (Exception e) {
                userTransaction.rollback();
                logger.severe("Failed to save import history: " + e.getMessage());
                throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.severe("Failed to begin transaction for import history: " + e.getMessage());
            throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
        }
    }

    private static class RecordWithLineNumber {
        final int lineNumber;
        final Map<String, String> record;
        final String originalLine;

        RecordWithLineNumber(int lineNumber, Map<String, String> record, String originalLine) {
            this.lineNumber = lineNumber;
            this.record = record;
            this.originalLine = originalLine;
        }
    }

    private static class ValidatedRecord {
        final int lineNumber;
        final String fileName;
        final LocationEntity locationRequest;
        final CoordinatesEntity coordinatesRequest;
        final PersonEntity personRequest;
        final String originalLine;

        ValidatedRecord(int lineNumber, String fileName, LocationEntity locationRequest, CoordinatesEntity coordinatesRequest, PersonEntity personRequest, String originalLine) {
            this.lineNumber = lineNumber;
            this.fileName = fileName;
            this.locationRequest = locationRequest;
            this.coordinatesRequest = coordinatesRequest;
            this.personRequest = personRequest;
            this.originalLine = originalLine;
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim());

        return fields.toArray(new String[0]);
    }

    private ValidatedRecord validateRecord(Map<String, String> record, int lineNumber, String fileName, String originalLine, List<ValidationError> errors) {
        logger.info("Validating record at line " + lineNumber + ": " + record);
        
        LocationEntity locationRequest = null;
        if (hasLocationFields(record)) {
            locationRequest = parseLocation(record, lineNumber, fileName, errors);
        }

        CoordinatesEntity coordinatesRequest = parseCoordinates(record, lineNumber, fileName, errors);
        if (coordinatesRequest == null) {
            return null;
        }

        PersonEntity personRequest = parsePerson(record, null, null, lineNumber, fileName, errors);
        if (personRequest == null) {
            return null;
        }

        return new ValidatedRecord(lineNumber, fileName, locationRequest, coordinatesRequest, personRequest, originalLine);
    }

    private void saveRecordsInTransaction(List<ValidatedRecord> validatedRecords, List<ValidationError> errors) {
        try {
            userTransaction.begin();
            try {
                for (ValidatedRecord validated : validatedRecords) {
                    try {
                        saveRecord(validated);
                    } catch (Exception e) {
                        if (isDatabaseError(e)) {
                            logger.severe("Phase 2 (Commit): Database error saving record: " + e.getMessage());
                            userTransaction.rollback();
                            throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
                        }
                        logger.warning("Phase 2 (Commit): Error saving record at line " + validated.lineNumber + ": " + e.getMessage());
                        errors.add(new ValidationError(validated.lineNumber, validated.fileName, null, "Error saving record: " + e.getMessage(), validated.originalLine));
                    }
                }
                userTransaction.commit();
            } catch (RuntimeException e) {
                userTransaction.rollback();
                throw e;
            } catch (Exception e) {
                userTransaction.rollback();
                logger.severe("Phase 2 (Commit): Error in transaction: " + e.getMessage());
                throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to begin transaction for saving records: " + e.getMessage());
            throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
        }
    }

    private void saveRecord(ValidatedRecord validated) {
        logger.info("Saving validated record at line " + validated.lineNumber);

        boolean coordinatesExists = coordinatesRepository.existsByXAndY(
            validated.coordinatesRequest.getX(),
            validated.coordinatesRequest.getY()
        );
        if (coordinatesExists) {
            throw new RuntimeException("Coordinates (x=" + validated.coordinatesRequest.getX() + 
                ", y=" + validated.coordinatesRequest.getY() + ") already exists in database");
        }

        LocationEntity location = null;
        if (validated.locationRequest != null) {
            boolean locationExists = locationRepository.existsByXAndYAndZ(
                validated.locationRequest.getX(),
                validated.locationRequest.getY(),
                validated.locationRequest.getZ()
            );
            if (locationExists) {
                throw new RuntimeException("Location (x=" + validated.locationRequest.getX() + 
                    ", y=" + validated.locationRequest.getY() + 
                    ", z=" + validated.locationRequest.getZ() + ") already exists in database");
            }
            location = locationRepository.save(validated.locationRequest);
            logger.info("Created Location with ID: " + location.getId());
        }

        CoordinatesEntity coordinates = coordinatesRepository.save(validated.coordinatesRequest);
        Integer coordinatesId = coordinates.getId();
        logger.info("Created Coordinates with ID: " + coordinatesId);
            
        validated.personRequest.setCoordinates(coordinates);
        if (location != null) {
            validated.personRequest.setLocation(location);
        }

        PersonEntity person = personRepository.save(validated.personRequest);
        logger.info("Created Person with ID: " + person.getId());
    }

    private boolean hasLocationFields(Map<String, String> record) {
        return record.containsKey("locationx") || record.containsKey("location_x") ||
               record.containsKey("locationy") || record.containsKey("location_y") ||
               record.containsKey("locationz") || record.containsKey("location_z");
    }

    private LocationEntity parseLocation(Map<String, String> record, int lineNumber, String fileName, List<ValidationError> errors) {
        boolean hasErrors = false;
        
        String xStr = getValue(record, "locationx", "location_x");
        String yStr = getValue(record, "locationy", "location_y");
        String zStr = getValue(record, "locationz", "location_z");
        
        if ((xStr == null || xStr.isEmpty()) && (yStr == null || yStr.isEmpty()) && (zStr == null || zStr.isEmpty())) {
            return null;
        }
        LocationEntity locationRequest = new LocationEntity();
        if (xStr == null || xStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "locationX", "Location X is required", null));
            hasErrors = true;
        } else {
            try {
                locationRequest.setX(Double.parseDouble(xStr));
            } catch (NumberFormatException e) {
                errors.add(new ValidationError(lineNumber, fileName, "locationX", "Invalid number format: " + xStr, null));
                hasErrors = true;
            }
        }
        
        if (yStr == null || yStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "locationY", "Location Y is required", null));
            hasErrors = true;
        } else {
            try {
                locationRequest.setY(Double.parseDouble(yStr));
            } catch (NumberFormatException e) {
                errors.add(new ValidationError(lineNumber, fileName, "locationY", "Invalid number format: " + yStr, null));
                hasErrors = true;
            }
        }
        
        if (zStr == null || zStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "locationZ", "Location Z is required", null));
            hasErrors = true;
        } else {
            try {
                locationRequest.setZ(Integer.parseInt(zStr));
            } catch (NumberFormatException e) {
                errors.add(new ValidationError(lineNumber, fileName, "locationZ", "Invalid integer format: " + zStr, null));
                hasErrors = true;
            }
        }
        
        return hasErrors ? null : locationRequest;
    }

    private CoordinatesEntity parseCoordinates(Map<String, String> record, int lineNumber, String fileName, List<ValidationError> errors) {
        CoordinatesEntity coordinatesRequest = new CoordinatesEntity();
        boolean hasErrors = false;
        
        String xStr = getValue(record, "coordinatesx", "coordinates_x", "coordinatex", "coordinate_x");
        String yStr = getValue(record, "coordinatesy", "coordinates_y", "coordinatey", "coordinate_y");
        
        if (xStr == null || xStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "coordinatesX", "Coordinates X is required", null));
            hasErrors = true;
        } else {
            try {
                coordinatesRequest.setX(Double.parseDouble(xStr));
            } catch (NumberFormatException e) {
                errors.add(new ValidationError(lineNumber, fileName, "coordinatesX", "Invalid number format: " + xStr, null));
                hasErrors = true;
            }
        }
        
        if (yStr == null || yStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "coordinatesY", "Coordinates Y is required", null));
            hasErrors = true;
        } else {
            try {
                float y = Float.parseFloat(yStr);
                if (y > 258) {
                    errors.add(new ValidationError(lineNumber, fileName, "coordinatesY", "Coordinates Y must be <= 258, got: " + y, null));
                    hasErrors = true;
                } else {
                    coordinatesRequest.setY(y);
                }
            } catch (NumberFormatException e) {
                errors.add(new ValidationError(lineNumber, fileName, "coordinatesY", "Invalid number format: " + yStr, null));
                hasErrors = true;
            }
        }
        
        return hasErrors ? null : coordinatesRequest;
    }

    private PersonEntity parsePerson(Map<String, String> record, Integer coordinatesId, Integer locationId, int lineNumber, String fileName, List<ValidationError> errors) {
        PersonEntity personRequest = new PersonEntity();
        boolean hasErrors = false;
        
        String name = getValue(record, "name");
        if (name == null || name.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "name", "Name is required", null));
            hasErrors = true;
        } else {
            personRequest.setName(name);
        }
        
        String eyeColorStr = getValue(record, "eyecolor", "eye_color", "eyecolour", "eye_colour");
        if (eyeColorStr == null || eyeColorStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "eyeColor", "Eye color is required", null));
            hasErrors = true;
        } else {
            try {
                personRequest.setEyeColor(Color.valueOf(eyeColorStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError(lineNumber, fileName, "eyeColor", "Invalid eye color: " + eyeColorStr + ". Valid values: RED, BLACK, BLUE, WHITE, BROWN", null));
                hasErrors = true;
            }
        }
        
        String hairColorStr = getValue(record, "haircolor", "hair_color", "haircolour", "hair_colour");
        if (hairColorStr == null || hairColorStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "hairColor", "Hair color is required", null));
            hasErrors = true;
        } else {
            try {
                personRequest.setHairColor(Color.valueOf(hairColorStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError(lineNumber, fileName, "hairColor", "Invalid hair color: " + hairColorStr + ". Valid values: RED, BLACK, BLUE, WHITE, BROWN", null));
                hasErrors = true;
            }
        }
        
        String heightStr = getValue(record, "height");
        if (heightStr == null || heightStr.isEmpty()) {
            errors.add(new ValidationError(lineNumber, fileName, "height", "Height is required", null));
            hasErrors = true;
        } else {
            try {
                int height = Integer.parseInt(heightStr);
                if (height < 1) {
                    errors.add(new ValidationError(lineNumber, fileName, "height", "Height must be >= 1, got: " + height, null));
                    hasErrors = true;
                } else {
                    personRequest.setHeight(height);
                }
            } catch (NumberFormatException e) {
                errors.add(new ValidationError(lineNumber, fileName, "height", "Invalid integer format: " + heightStr, null));
                hasErrors = true;
            }
        }
        
        String nationalityStr = getValue(record, "nationality", "country");
        if (nationalityStr != null && !nationalityStr.isEmpty()) {
            try {
                personRequest.setNationality(Country.valueOf(nationalityStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError(lineNumber, fileName, "nationality", "Invalid nationality: " + nationalityStr + ". Valid values: RUSSIA, UNITED_KINGDOM, SPAIN, ITALY, SOUTH_KOREA", null));
                hasErrors = true;
            }
        }
        
        return hasErrors ? null : personRequest;
    }

    private String getValue(Map<String, String> record, String... keys) {
        for (String key : keys) {
            if (record.containsKey(key)) {
                String value = record.get(key);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    private void checkUniquenessInFile(List<ValidatedRecord> validatedRecords, String defaultFileName, List<ValidationError> errors) {
        Map<String, List<ValidatedRecord>> coordinatesMap = new HashMap<>();
        for (ValidatedRecord record : validatedRecords) {
            String key = record.coordinatesRequest.getX() + "," + record.coordinatesRequest.getY();
            coordinatesMap.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        for (Map.Entry<String, List<ValidatedRecord>> entry : coordinatesMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String[] parts = entry.getKey().split(",");
                List<String> duplicateInfo = new ArrayList<>();
                for (ValidatedRecord record : entry.getValue()) {
                    duplicateInfo.add(record.fileName + ":" + record.lineNumber);
                }
                for (ValidatedRecord record : entry.getValue()) {
                    errors.add(new ValidationError(
                        record.lineNumber,
                        record.fileName,
                        "coordinates",
                        "Duplicate coordinates (x=" + parts[0] + ", y=" + parts[1] + ") found at: " + String.join(", ", duplicateInfo),
                        record.originalLine
                    ));
                }
            }
        }

        Map<String, List<ValidatedRecord>> locationMap = new HashMap<>();
        for (ValidatedRecord record : validatedRecords) {
            if (record.locationRequest != null) {
                String key = record.locationRequest.getX() + "," + record.locationRequest.getY() + "," + record.locationRequest.getZ();
                locationMap.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
            }
        }
        for (Map.Entry<String, List<ValidatedRecord>> entry : locationMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String[] parts = entry.getKey().split(",");
                List<String> duplicateInfo = new ArrayList<>();
                for (ValidatedRecord record : entry.getValue()) {
                    duplicateInfo.add(record.fileName + ":" + record.lineNumber);
                }
                for (ValidatedRecord record : entry.getValue()) {
                    errors.add(new ValidationError(
                        record.lineNumber,
                        record.fileName,
                        "location",
                        "Duplicate location (x=" + parts[0] + ", y=" + parts[1] + ", z=" + parts[2] + ") found at: " + String.join(", ", duplicateInfo),
                        record.originalLine
                    ));
                }
            }
        }
    }

    private void checkExistenceInDatabaseInTransaction(List<ValidatedRecord> validatedRecords, String defaultFileName, List<ValidationError> errors) {
        try {
            userTransaction.begin();
            try {
                for (ValidatedRecord record : validatedRecords) {
                    boolean coordinatesExists = coordinatesRepository.existsByXAndY(
                        record.coordinatesRequest.getX(),
                        record.coordinatesRequest.getY()
                    );
                    if (coordinatesExists) {
                        errors.add(new ValidationError(
                            record.lineNumber,
                            record.fileName,
                            "coordinates",
                            "Coordinates (x=" + record.coordinatesRequest.getX() + ", y=" + record.coordinatesRequest.getY() + ") already exists in database",
                            record.originalLine
                        ));
                    }

                    if (record.locationRequest != null) {
                        boolean locationExists = locationRepository.existsByXAndYAndZ(
                            record.locationRequest.getX(),
                            record.locationRequest.getY(),
                            record.locationRequest.getZ()
                        );
                        if (locationExists) {
                            errors.add(new ValidationError(
                                record.lineNumber,
                                record.fileName,
                                "location",
                                "Location (x=" + record.locationRequest.getX() + ", y=" + record.locationRequest.getY() + ", z=" + record.locationRequest.getZ() + ") already exists in database",
                                record.originalLine
                            ));
                        }
                    }
                }
                userTransaction.commit();
            } catch (Exception e) {
                userTransaction.rollback();
                logger.severe("Database error during existence check: " + e.getMessage());
                throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to begin transaction for existence check: " + e.getMessage());
            throw new RuntimeException("Database is unavailable: " + e.getMessage(), e);
        }
    }
    
    private String getMinIOErrorMessage(Throwable e) {
        return "File storage service is unavailable. Please try again later.";
    }
}


