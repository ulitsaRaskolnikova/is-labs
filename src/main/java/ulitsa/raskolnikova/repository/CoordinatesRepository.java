package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
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
        return super.save(entity);
    }

    @Override
    public Optional<CoordinatesEntity> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public List<CoordinatesEntity> findAll(SearchRequest searchRequest) {
        return super.findAll(searchRequest);
    }

    @Override
    public void deleteById(Integer id) {
        super.deleteById(id);
    }

    @Override
    public List<CoordinatesEntity> findAll() {
        return super.findAll();
    }

    @Override
    public long countAll() {
        return super.countAll();
    }

    public boolean existsByXAndY(double x, Float y) {
        jakarta.persistence.criteria.CriteriaBuilder cb = em.getCriteriaBuilder();
        jakarta.persistence.criteria.CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        jakarta.persistence.criteria.Root<CoordinatesEntity> root = cq.from(CoordinatesEntity.class);
        cq.select(cb.count(root));
        cq.where(
            cb.and(
                cb.equal(root.get("x"), x),
                cb.equal(root.get("y"), y)
            )
        );
        Long count = em.createQuery(cq).getSingleResult();
        return count > 0;
    }
}
