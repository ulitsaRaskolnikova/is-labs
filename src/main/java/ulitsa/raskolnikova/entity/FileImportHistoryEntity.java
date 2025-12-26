package ulitsa.raskolnikova.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_import_history")
@jakarta.persistence.Cacheable
@org.eclipse.persistence.annotations.Cache(
    type = org.eclipse.persistence.annotations.CacheType.FULL,
    size = 100,
    expiry = 1800000
)
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

