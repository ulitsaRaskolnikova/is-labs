package ulitsa.raskolnikova.mapper;

import org.mapstruct.*;
import ulitsa.raskolnikova.entity.LocationEntity;
import ulitsa.raskolnikova.model.LocationRequest;
import ulitsa.raskolnikova.model.LocationResponse;

@Mapper(componentModel = "cdi")
public interface LocationMapper extends CommonMapper<LocationRequest, LocationResponse, LocationEntity> {

    LocationEntity toEntity(LocationRequest request);

    LocationResponse toResponse(LocationEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(LocationRequest request, @MappingTarget LocationEntity entity);
}
