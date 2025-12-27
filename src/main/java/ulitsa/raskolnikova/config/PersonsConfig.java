package ulitsa.raskolnikova.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ulitsa.raskolnikova.entity.PersonEntity;
import ulitsa.raskolnikova.qualifier.CoordinatesRepo;
import ulitsa.raskolnikova.qualifier.FileImportHistoryRepo;
import ulitsa.raskolnikova.qualifier.LocationRepo;
import ulitsa.raskolnikova.qualifier.PersonRepo;
import ulitsa.raskolnikova.cache.CacheStatisticsService;
import ulitsa.raskolnikova.repository.CoordinatesRepository;
import ulitsa.raskolnikova.repository.CrudRepository;
import ulitsa.raskolnikova.repository.FileImportHistoryRepository;
import ulitsa.raskolnikova.repository.LocationRepository;


@ApplicationScoped
public class PersonsConfig {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @Produces
    @PersonRepo
    public CrudRepository<PersonEntity> personRepository() {
        return new CrudRepository<>(em, PersonEntity.class);
    }

    @Produces
    @LocationRepo
    public LocationRepository locationRepository() {
        return new LocationRepository(em);
    }

    @Produces
    @CoordinatesRepo
    public CoordinatesRepository coordinatesRepository() {
        return new CoordinatesRepository(em);
    }

    @Produces
    @FileImportHistoryRepo
    public FileImportHistoryRepository fileImportHistoryRepository() {
        return new FileImportHistoryRepository(em);
    }

    @Inject
    private CacheStatisticsService cacheStatisticsService;

    @PostConstruct
    public void initCacheStatistics() {
        if (cacheStatisticsService != null) {
            CrudRepository.setCacheStatisticsService(cacheStatisticsService);
            System.out.println("[PersonsConfig] CacheStatisticsService injected into CrudRepository");
        }
    }
}