package ulitsa.raskolnikova.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private int lineNumber;
    private String fileName;
    private String field;
    private String message;
    private String recordData;
}

