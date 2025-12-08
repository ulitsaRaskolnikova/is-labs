package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import ulitsa.raskolnikova.model.FilterField;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.model.SortDirection;
import ulitsa.raskolnikova.model.SortField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CrudRepository<T> {

    private final EntityManager em;

    private final Class<T> entityClass;

    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public List<T> findAll(SearchRequest searchRequest) {
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
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 20;
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    public void deleteById(Integer id) {
        T person = em.find(entityClass, id);
        if (person != null) {
            em.remove(person);
        }
    }

    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public long countAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}
