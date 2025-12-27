package ulitsa.raskolnikova.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "coordinates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"x", "y"})
)
@jakarta.persistence.Cacheable
@org.eclipse.persistence.annotations.Cache(
    type = org.eclipse.persistence.annotations.CacheType.FULL,
    size = 500,
    expiry = 1800000
)
public class CoordinatesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "x")
    private double x;

    @Column(name = "y", nullable = false)
    private Float y;
}