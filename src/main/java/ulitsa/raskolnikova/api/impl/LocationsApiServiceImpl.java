package ulitsa.raskolnikova.api.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ulitsa.raskolnikova.api.LocationsApiService;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.mapper.CommonMapper;
import ulitsa.raskolnikova.mapper.LocationMapper;
import ulitsa.raskolnikova.model.LocationRequest;
import ulitsa.raskolnikova.model.LocationResponse;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.qualifier.LocationRepo;
import ulitsa.raskolnikova.repository.CrudRepository;
import ulitsa.raskolnikova.repository.LocationRepository;

@ApplicationScoped
public class LocationsApiServiceImpl
        extends CrudService<LocationRequest, LocationResponse, LocationEntity> implements LocationsApiService {

    @Inject
    @LocationRepo
    private LocationRepository repository;

    @Inject
    private LocationMapper mapper;

    @Override
    protected CrudRepository<LocationEntity> getRepository() {
        return repository;
    }

    @Override
    protected CommonMapper<LocationRequest, LocationResponse, LocationEntity> getMapper() {
        return mapper;
    }

    @Transactional
    @Override
    public Response createLocation(LocationRequest location, SecurityContext securityContext) {
        LocationEntity entity = getMapper().toEntity(location);
        
        boolean exists = repository.existsByXAndYAndZ(entity.getX(), entity.getY(), entity.getZ());
        if (exists) {
            throw new RuntimeException("Location (x=" + entity.getX() + 
                ", y=" + entity.getY() + 
                ", z=" + entity.getZ() + ") already exists in database");
        }
        
        LocationEntity saved = repository.save(entity);
        
        return Response.ok()
                .entity(getMapper().toResponse(saved))
                .build();
    }

    @Transactional
    @Override
    public Response deleteLocation(Integer id, SecurityContext securityContext) {
        return delete(id, securityContext);
    }

    @Override
    public Response getLocationById(Integer id, SecurityContext securityContext) {
        return getById(id, securityContext);
    }

    @Override
    public Response searchLocations(SearchRequest searchRequest, SecurityContext securityContext) {
        return search(searchRequest, securityContext);
    }

    @Transactional
    @Override
    public Response updateLocation(Integer id, LocationRequest location, SecurityContext securityContext) {
        return update(id, location, securityContext);
    }
}
