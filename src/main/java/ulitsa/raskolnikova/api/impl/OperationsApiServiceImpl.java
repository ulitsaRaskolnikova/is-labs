package ulitsa.raskolnikova.api.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ulitsa.raskolnikova.api.OperationsApiService;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.entity.PersonEntity;
import ulitsa.raskolnikova.model.Color;
import ulitsa.raskolnikova.model.Country;
import ulitsa.raskolnikova.qualifier.LocationRepo;
import ulitsa.raskolnikova.qualifier.PersonRepo;
import ulitsa.raskolnikova.repository.CrudRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class OperationsApiServiceImpl implements OperationsApiService {

    @Inject
    @PersonRepo
    private CrudRepository<PersonEntity> personRepository;

    @Inject
    @LocationRepo
    private CrudRepository<LocationEntity> locationRepository;

    @Override
    public Response countByHairColorAndLocation(Color hairColor, Integer locationId, SecurityContext securityContext) {
        Optional<LocationEntity> locationOpt = locationRepository.findById(locationId);

        long count = personRepository.findAll().stream()
                .filter(person -> locationOpt.isPresent() && person.getLocation() != null)
                .filter(person -> person.getLocation().equals(locationOpt.orElse(null)))
                .filter(person -> hairColor.equals(person.getHairColor()))
                .count();

        return Response.ok(Map.of("value", count)).build();
    }


    @Override
    public Response countByHeight(Integer height, SecurityContext securityContext) {
        return Response.ok(personRepository.findAll()
                .stream()
                .filter(person -> person.getHeight().equals(height))
                .count()
        ).build();
    }

    @Transactional
    @Override
    public Response deleteByNationality(Country nationality, SecurityContext securityContext) {
        return personRepository.findAll()
                .stream()
                .filter(person -> person.getNationality().equals(nationality))
                .findFirst()
                .map(person -> {
                    personRepository.deleteById(person.getId());
                    return Response.ok(person).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @Override
    public Response eyeColorShare(Color eyeColor, SecurityContext securityContext) {
        List<PersonEntity> persons = personRepository.findAll();
        long total = persons.size();
        long specific = persons.stream()
                .filter(person -> person.getEyeColor() == eyeColor)
                .count();
        if (total == 0) {
            throw new WebApplicationException("There are no people", Response.Status.BAD_REQUEST);
        }
        return Response.ok(specific * 100 / total).build();
    }

    @Override
    public Response sumHeight(SecurityContext securityContext) {
        return Response.ok(personRepository.findAll()
                .stream()
                .mapToInt(PersonEntity::getHeight)
                .sum()
        ).build();
    }
}
