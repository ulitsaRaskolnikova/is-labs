package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
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
        return super.save(entity);
    }

    @Override
    public Optional<FileImportHistoryEntity> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public List<FileImportHistoryEntity> findAll(SearchRequest searchRequest) {
        return super.findAll(searchRequest);
    }

    @Override
    public void deleteById(Integer id) {
        super.deleteById(id);
    }

    @Override
    public List<FileImportHistoryEntity> findAll() {
        return super.findAll();
    }

    @Override
    public long countAll() {
        return super.countAll();
    }
}

