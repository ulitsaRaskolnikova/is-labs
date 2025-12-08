package ulitsa.raskolnikova.mapper;

import org.mapstruct.*;
import ulitsa.raskolnikova.entity.PersonEntity;
import ulitsa.raskolnikova.model.PersonRequest;
import ulitsa.raskolnikova.model.PersonResponse;

@Mapper(componentModel = "cdi")
public interface PersonMapper extends CommonMapper<PersonRequest, PersonResponse, PersonEntity> {

    String LOCATION_ID = "locationId";
    String LOCATION_DOT_ID = "location.id";
    String COORDINATES_ID = "coordinatesId";
    String COORDINATES_DOT_ID = "coordinates.id";

    @Mapping(source = LOCATION_ID, target = LOCATION_DOT_ID)
    @Mapping(source = COORDINATES_ID, target = COORDINATES_DOT_ID)
    PersonEntity toEntity(PersonRequest request);

    @Mapping(source = LOCATION_DOT_ID, target = LOCATION_ID)
    @Mapping(source = COORDINATES_DOT_ID, target = COORDINATES_ID)
    PersonResponse toResponse(PersonEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(source = LOCATION_ID, target = LOCATION_DOT_ID)
    @Mapping(source = COORDINATES_ID, target = COORDINATES_DOT_ID)
    void updateEntityFromRequest(PersonRequest request, @MappingTarget PersonEntity entity);
}
