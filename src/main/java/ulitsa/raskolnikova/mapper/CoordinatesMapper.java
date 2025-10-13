package ulitsa.raskolnikova.mapper;

import org.mapstruct.*;
import ulitsa.raskolnikova.entity.CoordinatesEntity;
import ulitsa.raskolnikova.model.CoordinatesRequest;
import ulitsa.raskolnikova.model.CoordinatesResponse;

@Mapper(componentModel = "cdi")
public interface CoordinatesMapper extends CommonMapper<CoordinatesRequest, CoordinatesResponse, CoordinatesEntity> {

    CoordinatesEntity toEntity(CoordinatesRequest request);

    CoordinatesResponse toResponse(CoordinatesEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(CoordinatesRequest request, @MappingTarget CoordinatesEntity entity);
}
