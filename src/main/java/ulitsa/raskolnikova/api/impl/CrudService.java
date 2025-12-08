package ulitsa.raskolnikova.api.impl;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ulitsa.raskolnikova.mapper.CommonMapper;
import ulitsa.raskolnikova.model.PageResponse;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class CrudService<REQUEST, RESPONSE, ENTITY> {

    protected abstract CrudRepository<ENTITY> getRepository();

    protected abstract CommonMapper<REQUEST, RESPONSE, ENTITY> getMapper();

    public Response search(SearchRequest searchRequest, SecurityContext securityContext) {
        List<RESPONSE> responses = getRepository().findAll(searchRequest)
                .stream()
                .map(getMapper()::toResponse)
                .collect(Collectors.toList());

        long totalElements = getRepository().countAll();

        int size = Optional.ofNullable(searchRequest.getSize()).orElse(20);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        PageResponse<RESPONSE> page = new PageResponse<>(
                responses, (int) totalElements, totalPages
        );

        return Response.ok(page).build();
    }


    public Response delete(Integer id, SecurityContext securityContext) {
        return getRepository().findById(id)
                .map(_ -> {
                    getRepository().deleteById(id);
                    return Response.ok();
                })
                .orElse(Response.noContent())
                .build();
    }

    public Response getById(Integer id, SecurityContext securityContext) {
        return getRepository().findById(id)
                .map(getMapper()::toResponse)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    public Response update(Integer id, REQUEST request, SecurityContext securityContext) {
        return getRepository().findById(id)
                .map(entity -> {
                    getMapper().updateEntityFromRequest(request, entity);
                    return getRepository().save(entity);
                })
                .map(getMapper()::toResponse)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    public Response create(REQUEST request, SecurityContext securityContext) {
        ENTITY entity = getMapper().toEntity(request);
        ENTITY saved = getRepository().save(entity);

        return Response.ok()
                .entity(getMapper().toResponse(saved))
                .build();
    }
}
