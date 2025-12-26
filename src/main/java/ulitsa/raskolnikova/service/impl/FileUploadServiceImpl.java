package ulitsa.raskolnikova.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

    @Transactional
    @Override
    public FileUploadResult processFile(String fileName, InputStream inputStream) {
        return processFile(fileName, inputStream, null);
    }

    @Transactional
    @Override
    public FileUploadResult processFile(String fileName, InputStream inputStream, String contentType) {
        logger.info("Processing file: " + fileName);
        
        List<ValidationError> errors = new ArrayList<>();
        
        try {
            byte[] fileData = readInputStreamToByteArray(inputStream);
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
                saveImportHistory(fileName, result, null);
                return result;
            }
        } catch (Exception e) {
            logger.severe("Error processing file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
            errors.add(new ValidationError(0, fileName, null, "Error processing file: " + e.getMessage(), null));
            FileUploadResult result = new FileUploadResult(0, 1, errors);
            saveImportHistory(fileName, result, null);
            return result;
        }
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
            saveImportHistory(zipFileName, result, null);
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
            saveImportHistory(zipFileName, result, null);
            return result;
        }
        
        checkExistenceInDatabase(allRecords, zipFileName, allErrors);
        
        if (!allErrors.isEmpty()) {
            logger.info("Existence errors found in database, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, allErrors.size(), allErrors);
            saveImportHistory(zipFileName, result, null);
            return result;
        }
        
        int totalProcessed = 0;
        for (ValidatedRecord validated : allRecords) {
            try {
                saveRecord(validated);
                totalProcessed++;
            } catch (Exception e) {
                logger.warning("Error saving record: " + e.getMessage());
                allErrors.add(new ValidationError(validated.lineNumber, validated.fileName, null, "Error saving record: " + e.getMessage(), validated.originalLine));
            }
        }
        
        FileUploadResult result = new FileUploadResult(totalProcessed, allErrors.size(), allErrors);
        
        String storagePath = null;
        if (result.getErrorCount() == 0 && fileData != null) {
            try {
                storagePath = minIOService.saveFile(zipFileName, new ByteArrayInputStream(fileData), fileData.length, contentType);
                logger.info("File saved to MinIO after successful validation: " + storagePath);
            } catch (Exception e) {
                logger.warning("Failed to save file to MinIO after validation: " + e.getMessage());
            }
        }
        
        saveImportHistory(zipFileName, result, storagePath);
        return result;
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
            saveImportHistory(fileName, result);
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
            saveImportHistory(fileName, result, null);
            return result;
        }

        checkUniquenessInFile(validatedRecords, fileName, errors);

        if (!errors.isEmpty()) {
            logger.info("Uniqueness errors found in file, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
            saveImportHistory(fileName, result, null);
            return result;
        }

        checkExistenceInDatabase(validatedRecords, fileName, errors);

        if (!errors.isEmpty()) {
            logger.info("Existence errors found in database, skipping database operations");
            FileUploadResult result = new FileUploadResult(0, errors.size(), errors);
            saveImportHistory(fileName, result, null);
            return result;
        }

        int processedCount = 0;
        for (ValidatedRecord validated : validatedRecords) {
            try {
                saveRecord(validated);
                processedCount++;
            } catch (Exception e) {
                logger.warning("Error saving record at line " + validated.lineNumber + ": " + e.getMessage());
                errors.add(new ValidationError(validated.lineNumber, fileName, null, "Error saving record: " + e.getMessage(), validated.originalLine));
            }
        }

        FileUploadResult result = new FileUploadResult(processedCount, errors.size(), errors);
        
        String storagePath = null;
        if (result.getErrorCount() == 0 && fileData != null) {
            try {
                storagePath = minIOService.saveFile(fileName, new ByteArrayInputStream(fileData), fileData.length, contentType);
                logger.info("File saved to MinIO after successful validation: " + storagePath);
            } catch (Exception e) {
                logger.warning("Failed to save file to MinIO after validation: " + e.getMessage());
            }
        }
        
        saveImportHistory(fileName, result, storagePath);
        return result;
    }

    @Transactional
    private void saveImportHistory(String fileName, FileUploadResult result) {
        saveImportHistory(fileName, result, null);
    }

    @Transactional
    private void saveImportHistory(String fileName, FileUploadResult result, String storagePath) {
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
        } catch (Exception e) {
            logger.warning("Failed to save import history: " + e.getMessage());
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

    private void saveRecord(ValidatedRecord validated) {
        logger.info("Saving validated record at line " + validated.lineNumber);

        LocationEntity location = null;
        if (validated.locationRequest != null) {
            location = locationRepository.save(validated.locationRequest);
            logger.info("Created Location with ID: " + location.getId());
        }

        CoordinatesEntity coordinates = coordinatesRepository.save(validated.coordinatesRequest);
        Integer coordinatesId = coordinates.getId();
        logger.info("Created Coordinates with ID: " + coordinatesId);
        CoordinatesEntity onlyIdCoordinates = new CoordinatesEntity();
        onlyIdCoordinates.setId(coordinatesId);
        validated.personRequest.setCoordinates(onlyIdCoordinates);
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

    @Transactional
    private void checkExistenceInDatabase(List<ValidatedRecord> validatedRecords, String defaultFileName, List<ValidationError> errors) {
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
    }
}


