package ulitsa.raskolnikova.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_import_history")
public class FileImportHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "processed_count")
    private Integer processedCount;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount;

    @Column(name = "import_date", nullable = false, updatable = false)
    private LocalDateTime importDate = LocalDateTime.now();
}

