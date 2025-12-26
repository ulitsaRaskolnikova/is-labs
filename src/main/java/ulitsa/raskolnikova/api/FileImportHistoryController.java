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
import ulitsa.raskolnikova.model.SortDirection;
import ulitsa.raskolnikova.model.SortField;
import ulitsa.raskolnikova.qualifier.FileImportHistoryRepo;
import ulitsa.raskolnikova.repository.FileImportHistoryRepository;
import ulitsa.raskolnikova.storage.MinIOService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
@Path("/import-history")
public class FileImportHistoryController {

    @Inject
    @FileImportHistoryRepo
    private FileImportHistoryRepository fileImportHistoryRepository;

    @Inject
    private MinIOService minIOService;

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchHistory(SearchRequest searchRequest) {
        try {
            if (searchRequest.getSort() == null || searchRequest.getSort().isEmpty()) {
                SortField defaultSort = new SortField();
                defaultSort.setField("importDate");
                defaultSort.setDirection(SortDirection.DESC);
                searchRequest.setSort(java.util.Arrays.asList(defaultSort));
            }
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

    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDownloadUrl(@PathParam("id") Integer id) {
        try {
            Optional<FileImportHistoryEntity> entityOpt = fileImportHistoryRepository.findById(id);
            if (entityOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"File import history not found\"}")
                        .build();
            }

            FileImportHistoryEntity entity = entityOpt.get();
            String storagePath = entity.getStoragePath();

            if (storagePath == null || storagePath.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"File not available in storage\"}")
                        .build();
            }

            try {
                String presignedUrl = minIOService.generatePresignedDownloadUrl(storagePath, 3600);
                String fileName = minIOService.getFileNameFromPath(storagePath);
                
                Map<String, String> response = new HashMap<>();
                response.put("downloadUrl", presignedUrl);
                response.put("fileName", fileName);
                
                return Response.ok(response).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Error generating download URL: " + e.getMessage() + "\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\": \"Error generating download URL: " + e.getMessage() + "\"}")
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
        response.setStoragePath(entity.getStoragePath());
        if (entity.getImportDate() != null) {
            response.setImportDate(entity.getImportDate());
        }
        return response;
    }
}

