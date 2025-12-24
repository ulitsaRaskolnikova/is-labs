package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ulitsa.raskolnikova.entity.FileImportHistoryEntity;
import ulitsa.raskolnikova.model.SearchRequest;

import java.util.List;
import java.util.Optional;

public class FileImportHistoryRepository extends CrudRepository<FileImportHistoryEntity> {

    private final EntityManager em;

    public FileImportHistoryRepository(EntityManager em) {
        super(em, FileImportHistoryEntity.class);
        this.em = em;
    }

    @Override
    public FileImportHistoryEntity save(FileImportHistoryEntity entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            FileImportHistoryEntity result = super.save(entity);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Optional<FileImportHistoryEntity> findById(Integer id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Optional<FileImportHistoryEntity> result = super.findById(id);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return Optional.empty();
        }
    }

    @Override
    public List<FileImportHistoryEntity> findAll(SearchRequest searchRequest) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<FileImportHistoryEntity> result = super.findAll(searchRequest);
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
    public List<FileImportHistoryEntity> findAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<FileImportHistoryEntity> result = super.findAll();
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

