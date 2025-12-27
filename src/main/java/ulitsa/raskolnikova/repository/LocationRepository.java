package ulitsa.raskolnikova.repository;

import jakarta.persistence.EntityManager;
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
        return super.save(entity);
    }

    @Override
    public Optional<LocationEntity> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public List<LocationEntity> findAll(SearchRequest searchRequest) {
        return super.findAll(searchRequest);
    }

    @Override
    public void deleteById(Integer id) {
        super.deleteById(id);
    }

    @Override
    public List<LocationEntity> findAll() {
        return super.findAll();
    }

    @Override
    public long countAll() {
        return super.countAll();
    }

    public boolean existsByXAndYAndZ(Double x, double y, int z) {
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
        return count > 0;
    }
}

