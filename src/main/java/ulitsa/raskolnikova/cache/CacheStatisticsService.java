package ulitsa.raskolnikova.cache;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.eclipse.persistence.jpa.JpaEntityManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@ApplicationScoped
public class CacheStatisticsService {

    private static final Logger logger = Logger.getLogger(CacheStatisticsService.class.getName());

    private EntityManagerFactory emf;
    
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong queryCacheHits = new AtomicLong(0);
    private final AtomicLong queryCacheMisses = new AtomicLong(0);

    @PostConstruct
    public void init() {
        System.out.println("[CacheStatisticsService] @PostConstruct init() called");
        try {
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
            HikariDataSource dataSource = new HikariDataSource(config);

            Map<String, Object> properties = new HashMap<>();
            properties.put("jakarta.persistence.nonJtaDataSource", dataSource);
            properties.put("eclipselink.logging.level", "FINE");
            properties.put("eclipselink.ddl-generation", "none");
            properties.put("eclipselink.target-database", "PostgreSQL");
            properties.put("eclipselink.query-results-cache", "true");
            properties.put("eclipselink.query-results-cache.size", "100");
            properties.put("eclipselink.profiler", "PerformanceProfiler");
            emf = Persistence.createEntityManagerFactory("local", properties);
            System.out.println("[CacheStatisticsService] EntityManagerFactory created successfully");
        } catch (Exception e) {
            System.err.println("[CacheStatisticsService] ERROR: Failed to initialize EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            logger.warning("Failed to initialize EntityManagerFactory: " + e.getMessage());
        }
    }

    private volatile boolean loggingEnabled = true;

    public void setLoggingEnabled(boolean enabled) {
        this.loggingEnabled = enabled;
        logger.info("Cache statistics logging " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("cacheHits", cacheHits.get());
        stats.put("cacheMisses", cacheMisses.get());
        stats.put("queryCacheHits", queryCacheHits.get());
        stats.put("queryCacheMisses", queryCacheMisses.get());
        
        long totalCacheRequests = cacheHits.get() + cacheMisses.get();
        long totalQueryCacheRequests = queryCacheHits.get() + queryCacheMisses.get();
        
        double cacheHitRatio = totalCacheRequests > 0 ? (double) cacheHits.get() / totalCacheRequests * 100 : 0.0;
        double queryCacheHitRatio = totalQueryCacheRequests > 0 ? (double) queryCacheHits.get() / totalQueryCacheRequests * 100 : 0.0;
        
        stats.put("cacheHitRatio", String.format("%.2f%%", cacheHitRatio));
        stats.put("queryCacheHitRatio", String.format("%.2f%%", queryCacheHitRatio));
        stats.put("loggingEnabled", loggingEnabled);
        
        if (emf == null) {
            stats.put("error", "EntityManagerFactory not initialized");
            return stats;
        }

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            if (em instanceof JpaEntityManager) {
                JpaEntityManager jpaEm = (JpaEntityManager) em;
                org.eclipse.persistence.sessions.Session session = jpaEm.getActiveSession();
                
                if (session != null) {
                    org.eclipse.persistence.sessions.IdentityMapAccessor accessor = session.getIdentityMapAccessor();
                    
                    boolean cacheEnabled = accessor != null;
                    
                    stats.put("cacheEnabled", cacheEnabled);
                    stats.put("sessionName", session.getName() != null ? session.getName() : "default");
                    
                    try {
                        org.eclipse.persistence.descriptors.ClassDescriptor personDescriptor = session.getDescriptor(ulitsa.raskolnikova.entity.PersonEntity.class);
                        if (personDescriptor != null) {
                            stats.put("personEntityCached", personDescriptor.shouldAlwaysRefreshCache() == false);
                        }
                    } catch (Exception e) {
                        logger.fine("Could not get cache descriptor: " + e.getMessage());
                    }
                } else {
                    stats.put("cacheEnabled", false);
                    stats.put("sessionName", "no active session");
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to retrieve cache statistics: " + e.getMessage());
            stats.put("error", e.getMessage());
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        
        return stats;
    }
    
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }
    
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }
    
    public void recordQueryCacheHit() {
        queryCacheHits.incrementAndGet();
    }
    
    public void recordQueryCacheMiss() {
        queryCacheMisses.incrementAndGet();
    }

    public void logCacheStatistics(String context) {
        System.out.println("[CacheStatisticsService] logCacheStatistics called with context: " + context);
        System.out.println("[CacheStatisticsService] loggingEnabled: " + loggingEnabled);
        
        if (!loggingEnabled) {
            System.out.println("[CacheStatisticsService] Logging is disabled, returning");
            return;
        }

        Map<String, Object> stats = getCacheStatistics();
        System.out.println("[CacheStatisticsService] Stats retrieved: " + stats);
        
        if (stats.containsKey("error")) {
            String errorMsg = "L2 Cache Statistics [%s] - Error: %s".formatted(context, stats.get("error"));
            System.out.println("[CACHE STATS] " + errorMsg);
            logger.warning(errorMsg);
            return;
        }

        String logMsg = String.format(
            "L2 Cache Statistics [%s] - Cache Hits: %s | Cache Misses: %s | Cache Hit Ratio: %s | Query Cache Hits: %s | Query Cache Misses: %s | Query Cache Hit Ratio: %s",
            context,
            stats.getOrDefault("cacheHits", 0),
            stats.getOrDefault("cacheMisses", 0),
            stats.getOrDefault("cacheHitRatio", "0.00%"),
            stats.getOrDefault("queryCacheHits", 0),
            stats.getOrDefault("queryCacheMisses", 0),
            stats.getOrDefault("queryCacheHitRatio", "0.00%")
        );
        System.out.println("[CACHE STATS] " + logMsg);
        logger.info(logMsg);
    }
}

