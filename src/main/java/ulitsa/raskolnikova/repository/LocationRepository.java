package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.model.SearchRequest;

import java.util.List;
import java.util.Optional;

public class LocationRepository extends CrudRepository<LocationEntity> {

    private final EntityManager em;

    public LocationRepository(EntityManager em) {
        super(em, LocationEntity.class);
        this.em = em;
    }

    @Override
    public LocationEntity save(LocationEntity entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LocationEntity result = super.save(entity);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Optional<LocationEntity> findById(Integer id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Optional<LocationEntity> result = super.findById(id);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public List<LocationEntity> findAll(SearchRequest searchRequest) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<LocationEntity> result = super.findAll(searchRequest);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void deleteById(Integer id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            super.deleteById(id);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public List<LocationEntity> findAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<LocationEntity> result = super.findAll();
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public long countAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            long result = super.countAll();
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public boolean existsByXAndYAndZ(Double x, double y, int z) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            jakarta.persistence.criteria.CriteriaBuilder cb = em.getCriteriaBuilder();
            jakarta.persistence.criteria.CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            jakarta.persistence.criteria.Root<LocationEntity> root = cq.from(LocationEntity.class);
            cq.select(cb.count(root));
            cq.where(
                cb.and(
                    cb.equal(root.get("x"), x),
                    cb.equal(root.get("y"), y),
                    cb.equal(root.get("z"), z)
                )
            );
            Long count = em.createQuery(cq).getSingleResult();
            tx.commit();
            return count > 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}

