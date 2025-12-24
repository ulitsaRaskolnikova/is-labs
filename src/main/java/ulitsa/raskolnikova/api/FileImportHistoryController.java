package ulitsa.raskolnikova.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ulitsa.raskolnikova.entity.FileImportHistoryEntity;
import ulitsa.raskolnikova.model.FileImportHistoryResponse;
import ulitsa.raskolnikova.model.PageResponse;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.qualifier.FileImportHistoryRepo;
import ulitsa.raskolnikova.repository.FileImportHistoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
@Path("/import-history")
public class FileImportHistoryController {

    @Inject
    @FileImportHistoryRepo
    private FileImportHistoryRepository fileImportHistoryRepository;

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchHistory(SearchRequest searchRequest) {
        try {
            List<FileImportHistoryEntity> entities = fileImportHistoryRepository.findAll(searchRequest);
            List<FileImportHistoryResponse> responses = entities.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            long totalElements = fileImportHistoryRepository.countAll();
            int size = Optional.ofNullable(searchRequest.getSize()).orElse(20);
            int totalPages = (int) Math.ceil((double) totalElements / size);

            PageResponse<FileImportHistoryResponse> page = new PageResponse<>(
                    responses, (int) totalElements, totalPages
            );

            return Response.ok(page).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\": \"Error searching import history: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") Integer id) {
        try {
            return fileImportHistoryRepository.findById(id)
                    .map(this::toResponse)
                    .map(Response::ok)
                    .orElse(Response.status(Response.Status.NOT_FOUND))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\": \"Error getting import history: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private FileImportHistoryResponse toResponse(FileImportHistoryEntity entity) {
        FileImportHistoryResponse response = new FileImportHistoryResponse();
        response.setId(entity.getId());
        response.setFileName(entity.getFileName());
        response.setStatus(entity.getStatus());
        response.setProcessedCount(entity.getProcessedCount());
        response.setErrorCount(entity.getErrorCount());
        if (entity.getImportDate() != null) {
            response.setImportDate(entity.getImportDate());
        }
        return response;
    }
}

