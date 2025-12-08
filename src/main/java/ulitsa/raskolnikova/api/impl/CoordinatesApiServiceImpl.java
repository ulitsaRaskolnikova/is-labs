package ulitsa.raskolnikova.api.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ulitsa.raskolnikova.api.CoordinatesApiService;
import ulitsa.raskolnikova.entity.CoordinatesEntity;
import ulitsa.raskolnikova.mapper.CommonMapper;
import ulitsa.raskolnikova.mapper.CoordinatesMapper;
import ulitsa.raskolnikova.model.CoordinatesRequest;
import ulitsa.raskolnikova.model.CoordinatesResponse;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.qualifier.CoordinatesRepo;
import ulitsa.raskolnikova.repository.CoordinatesRepository;
import ulitsa.raskolnikova.repository.CrudRepository;

@ApplicationScoped
public class CoordinatesApiServiceImpl
        extends CrudService<CoordinatesRequest, CoordinatesResponse, CoordinatesEntity>
        implements CoordinatesApiService {

    @Inject
    @CoordinatesRepo
    private CoordinatesRepository repository;

    @Inject
    private CoordinatesMapper mapper;

    @Override
    protected CoordinatesRepository getRepository() {
        return repository;
    }

    @Override
    protected CommonMapper<CoordinatesRequest, CoordinatesResponse, CoordinatesEntity> getMapper() {
        return mapper;
    }

    @Transactional
    @Override
    public Response createCoordinates(CoordinatesRequest coordinates, SecurityContext securityContext) {
        return create(coordinates, securityContext);
    }

    @Transactional
    @Override
    public Response deleteCoordinates(Integer id, SecurityContext securityContext) {
        return delete(id, securityContext);
    }

    @Override
    public Response getCoordinatesById(Integer id, SecurityContext securityContext) {
        return getById(id, securityContext);
    }

    @Override
    public Response searchCoordinates(SearchRequest searchRequest, SecurityContext securityContext) {
        return search(searchRequest, securityContext);
    }

    @Transactional
    @Override
    public Response updateCoordinates(Integer id, CoordinatesRequest coordinates, SecurityContext securityContext) {
        return update(id, coordinates, securityContext);
    }
}
