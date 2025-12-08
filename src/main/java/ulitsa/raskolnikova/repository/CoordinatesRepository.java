package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ulitsa.raskolnikova.entity.CoordinatesEntity;
import ulitsa.raskolnikova.model.SearchRequest;

import java.util.List;
import java.util.Optional;

public class CoordinatesRepository extends CrudRepository<CoordinatesEntity> {

    private final EntityManager em;

    public CoordinatesRepository(EntityManager em) {
        super(em, CoordinatesEntity.class);
        this.em = em;
    }

    @Override
    public CoordinatesEntity save(CoordinatesEntity entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CoordinatesEntity result = super.save(entity);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Optional<CoordinatesEntity> findById(Integer id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Optional<CoordinatesEntity> result = super.findById(id);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public List<CoordinatesEntity> findAll(SearchRequest searchRequest) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<CoordinatesEntity> result = super.findAll(searchRequest);
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
    public List<CoordinatesEntity> findAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<CoordinatesEntity> result = super.findAll();
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
}
