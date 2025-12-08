package ulitsa.raskolnikova.api.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ulitsa.raskolnikova.api.PersonsApiService;
import ulitsa.raskolnikova.entity.CoordinatesEntity;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.entity.PersonEntity;
import ulitsa.raskolnikova.mapper.CommonMapper;
import ulitsa.raskolnikova.mapper.PersonMapper;
import ulitsa.raskolnikova.model.PersonRequest;
import ulitsa.raskolnikova.model.PersonResponse;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.qualifier.CoordinatesRepo;
import ulitsa.raskolnikova.qualifier.LocationRepo;
import ulitsa.raskolnikova.qualifier.PersonRepo;
import ulitsa.raskolnikova.repository.CrudRepository;

@ApplicationScoped
public class PersonsApiServiceImpl
        extends CrudService<PersonRequest, PersonResponse, PersonEntity> implements PersonsApiService {

    @Inject
    @PersonRepo
    private CrudRepository<PersonEntity> personRepository;

    @Inject
    @LocationRepo
    private CrudRepository<LocationEntity> locationRepository;

    @Inject
    @CoordinatesRepo
    private CrudRepository<CoordinatesEntity> coordinatesRepository;

    @Inject
    private PersonMapper mapper;

    @Override
    protected CrudRepository<PersonEntity> getRepository() {
        return personRepository;
    }

    @Override
    protected CommonMapper<PersonRequest, PersonResponse, PersonEntity> getMapper() {
        return mapper;
    }

    @Transactional
    @Override
    public Response createPerson(PersonRequest personRequest, SecurityContext securityContext) {
        return create(personRequest, securityContext);
    }

    @Transactional
    @Override
    public Response deletePerson(Integer id, SecurityContext securityContext) {
        return delete(id, securityContext);
    }

    @Override
    public Response getPersonById(Integer id, SecurityContext securityContext) {
        return getById(id, securityContext);
    }

    @Override
    public Response searchPersons(SearchRequest searchRequest, SecurityContext securityContext) {
        return search(searchRequest, securityContext);
    }

    @Transactional
    @Override
    public Response updatePerson(Integer id, PersonRequest personRequest, SecurityContext securityContext) {
        return update(id, personRequest, securityContext);
    }

    @Override
    public Response create(PersonRequest request, SecurityContext securityContext) {
        PersonEntity person = getMapper().toEntity(request);
        if (request.getLocationId() != null) {
            person.setLocation(locationRepository.findById(request.getLocationId()).orElseThrow());
        }
        person.setCoordinates(coordinatesRepository.findById(request.getCoordinatesId()).orElseThrow());

        PersonEntity saved = getRepository().save(person);

        return Response.ok()
                .entity(getMapper().toResponse(saved))
                .build();
    }
}
