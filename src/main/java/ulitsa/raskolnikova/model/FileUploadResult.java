package ulitsa.raskolnikova.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {
    private int processedCount;
    private int errorCount;
    private List<ValidationError> errors;
    private String message;

    public FileUploadResult(int processedCount, int errorCount, List<ValidationError> errors) {
        this.processedCount = processedCount;
        this.errorCount = errorCount;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.message = String.format("Processed %d records, %d errors", processedCount, errorCount);
    }
}

