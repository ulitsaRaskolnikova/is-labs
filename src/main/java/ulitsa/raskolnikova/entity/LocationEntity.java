package ulitsa.raskolnikova.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "location",
        uniqueConstraints = @UniqueConstraint(columnNames = {"x", "y", "z"})
)
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "x", nullable = false)
    private Double x;

    @Column(name = "y")
    private double y;

    @Column(name = "z")
    private int z;
}