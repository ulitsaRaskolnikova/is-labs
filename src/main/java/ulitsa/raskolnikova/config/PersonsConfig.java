package ulitsa.raskolnikova.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import ulitsa.raskolnikova.entity.FileImportHistoryEntity;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.entity.PersonEntity;
import ulitsa.raskolnikova.qualifier.CoordinatesRepo;
import ulitsa.raskolnikova.qualifier.FileImportHistoryRepo;
import ulitsa.raskolnikova.qualifier.LocationRepo;
import ulitsa.raskolnikova.qualifier.PersonRepo;
import ulitsa.raskolnikova.repository.CoordinatesRepository;
import ulitsa.raskolnikova.repository.CrudRepository;
import ulitsa.raskolnikova.repository.FileImportHistoryRepository;
import ulitsa.raskolnikova.repository.LocationRepository;

import java.util.Map;

@ApplicationScoped
public class PersonsConfig {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("local", Map.of(
                    "jakarta.persistence.jdbc.driver", "org.postgresql.Driver",
                    "jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/postgres",
                    "jakarta.persistence.jdbc.user", "postgres",
                    "jakarta.persistence.jdbc.password", "postgres",
                    "eclipselink.logging.level", "FINE",
                    "eclipselink.ddl-generation", "none",
                    "eclipselink.target-database", "PostgreSQL"
            ));

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Produces
    @PersonRepo    public CrudRepository<PersonEntity> personRepository() {
        return new CrudRepository<>(em, PersonEntity.class);
    }

    @Produces
    @LocationRepo    public LocationRepository locationRepository() {
        return new LocationRepository(EMF.createEntityManager());
    }

    @Produces
    @CoordinatesRepo    public CoordinatesRepository coordinatesRepository() {
        return new CoordinatesRepository(EMF.createEntityManager());
    }

    @Produces
    @FileImportHistoryRepo
    public FileImportHistoryRepository fileImportHistoryRepository() {
        return new FileImportHistoryRepository(EMF.createEntityManager());
    }
}