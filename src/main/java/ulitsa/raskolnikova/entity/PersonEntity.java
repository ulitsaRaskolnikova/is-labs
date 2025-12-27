package ulitsa.raskolnikova.entity;

import jakarta.persistence.*;
import lombok.Data;
import ulitsa.raskolnikova.model.Color;
import ulitsa.raskolnikova.model.Country;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "person")
@jakarta.persistence.Cacheable
@org.eclipse.persistence.annotations.Cache(
    type = org.eclipse.persistence.annotations.CacheType.FULL,
    size = 500,
    expiry = 1800000
)
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "coordinates_id", nullable = false)
    private CoordinatesEntity coordinates;

    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", nullable = false)
    private Color eyeColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "hair_color", nullable = false)
    private Color hairColor;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "nationality", nullable = false)
    private Country nationality;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();
}
