package ulitsa.raskolnikova.mapper;

public interface CommonMapper<REQUEST, RESPONSE, ENTITY> {
    ENTITY toEntity(REQUEST request);

    RESPONSE toResponse(ENTITY entity);

    void updateEntityFromRequest(REQUEST request, ENTITY entity);
}
