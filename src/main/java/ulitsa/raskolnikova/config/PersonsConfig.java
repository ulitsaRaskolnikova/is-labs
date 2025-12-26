package ulitsa.raskolnikova.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PersonsConfig {

    private static final HikariDataSource hikariDataSource = createHikariDataSource();
    private static final EntityManagerFactory EMF = createEntityManagerFactory();

    private static HikariDataSource createHikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setDriverClassName("org.postgresql.Driver");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("HikariCP-Pool");
        return new HikariDataSource(config);
    }

    private static EntityManagerFactory createEntityManagerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.nonJtaDataSource", hikariDataSource);
        properties.put("eclipselink.logging.level", "FINE");
        properties.put("eclipselink.ddl-generation", "none");
        properties.put("eclipselink.target-database", "PostgreSQL");
        properties.put("eclipselink.query-results-cache", "true");
        properties.put("eclipselink.query-results-cache.size", "100");
        properties.put("eclipselink.profiler", "PerformanceProfiler");
        return Persistence.createEntityManagerFactory("local", properties);
    }

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
        return new LocationRepository(EMF.createEntityManager());
    }

    @Produces
    @CoordinatesRepo
    public CoordinatesRepository coordinatesRepository() {
        return new CoordinatesRepository(EMF.createEntityManager());
    }

    @Produces
    @FileImportHistoryRepo
    public FileImportHistoryRepository fileImportHistoryRepository() {
        return new FileImportHistoryRepository(EMF.createEntityManager());
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