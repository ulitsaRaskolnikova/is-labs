package ulitsa.raskolnikova.service;

import ulitsa.raskolnikova.model.FileUploadResult;

import java.io.InputStream;

public interface FileUploadService {
    FileUploadResult processFile(String fileName, InputStream inputStream);
}

