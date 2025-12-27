package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import ulitsa.raskolnikova.cache.CacheStatisticsService;
import ulitsa.raskolnikova.model.FilterField;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.model.SortDirection;
import ulitsa.raskolnikova.model.SortField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CrudRepository<T> {

    private static final Logger logger = Logger.getLogger(CrudRepository.class.getName());
    private static CacheStatisticsService cacheStatisticsService;

    private final EntityManager em;

    private final Class<T> entityClass;

    public static void setCacheStatisticsService(CacheStatisticsService service) {
        cacheStatisticsService = service;
    }

    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    public Optional<T> findById(Integer id) {
        System.out.println("[CrudRepository] findById(" + id + ") called for " + entityClass.getSimpleName());
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            cacheStatisticsService.logCacheStatistics("Before findById(" + id + "): " + entityClass.getSimpleName());
        }
        
        long startTime = System.nanoTime();
        T result = em.find(entityClass, id);
        long executionTime = (System.nanoTime() - startTime) / 1_000_000;
        boolean found = result != null;
        
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            if (found && executionTime < 1) {
                cacheStatisticsService.recordCacheHit();
            } else if (found) {
                cacheStatisticsService.recordCacheMiss();
            }
        }
        System.out.println("[CrudRepository] findById(" + id + ") " + (found ? "found" : "not found"));
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            cacheStatisticsService.logCacheStatistics("After findById(" + id + "): " + entityClass.getSimpleName() + " - " + (found ? "found" : "not found"));
        }
        return Optional.ofNullable(result);
    }

    public List<T> findAll(SearchRequest searchRequest) {
        System.out.println("[CrudRepository] findAll(SearchRequest) called for " + entityClass.getSimpleName());
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            cacheStatisticsService.logCacheStatistics("Before findAll(SearchRequest): " + entityClass.getSimpleName());
        }
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        if (searchRequest.getFilters() != null) {
            for (FilterField filter : searchRequest.getFilters()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get(filter.getField())),
                                "%" + filter.getPattern().toLowerCase() + "%"
                        )
                );
            }
        }
        cq.where(predicates.toArray(new Predicate[0]));

        if (searchRequest.getSort() != null && !searchRequest.getSort().isEmpty()) {
            List<Order> orders = new ArrayList<>();
            for (SortField sort : searchRequest.getSort()) {
                if (sort.getDirection() == SortDirection.ASC) {
                    orders.add(cb.asc(root.get(sort.getField())));
                } else {
                    orders.add(cb.desc(root.get(sort.getField())));
                }
            }
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.asc(root.get("id")));
        }

        TypedQuery<T> query = em.createQuery(cq);
        query.setHint("eclipselink.query-results-cache", "true");
        query.setHint("eclipselink.query-results-cache.expiry", "300000");
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 20;
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        long startTime = System.nanoTime();
        List<T> result = query.getResultList();
        long executionTime = (System.nanoTime() - startTime) / 1_000_000;
        
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            if (executionTime < 5) {
                cacheStatisticsService.recordQueryCacheHit();
            } else {
                cacheStatisticsService.recordQueryCacheMiss();
            }
        }
        System.out.println("[CrudRepository] findAll(SearchRequest) returned " + result.size() + " results");
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            cacheStatisticsService.logCacheStatistics("After findAll(SearchRequest): " + entityClass.getSimpleName() + " - " + result.size() + " results");
        }
        return result;
    }

    public void deleteById(Integer id) {
        T person = em.find(entityClass, id);
        if (person != null) {
            em.remove(person);
        }
    }

    public List<T> findAll() {
        System.out.println("[CrudRepository] findAll() called for " + entityClass.getSimpleName());
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            cacheStatisticsService.logCacheStatistics("Before findAll(): " + entityClass.getSimpleName());
        }
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        TypedQuery<T> query = em.createQuery(cq);
        query.setHint("eclipselink.query-results-cache", "true");
        query.setHint("eclipselink.query-results-cache.expiry", "300000");
        
        long startTime = System.nanoTime();
        List<T> result = query.getResultList();
        long executionTime = (System.nanoTime() - startTime) / 1_000_000;
        
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            if (executionTime < 5) {
                cacheStatisticsService.recordQueryCacheHit();
            } else {
                cacheStatisticsService.recordQueryCacheMiss();
            }
        }
        System.out.println("[CrudRepository] findAll() returned " + result.size() + " results");
        if (cacheStatisticsService != null && cacheStatisticsService.isLoggingEnabled()) {
            cacheStatisticsService.logCacheStatistics("After findAll(): " + entityClass.getSimpleName() + " - " + result.size() + " results");
        }
        return result;
    }

    public long countAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        TypedQuery<Long> query = em.createQuery(cq);
        query.setHint("eclipselink.query-results-cache", "true");
        query.setHint("eclipselink.query-results-cache.expiry", "300000");
        return query.getSingleResult();
    }
}
