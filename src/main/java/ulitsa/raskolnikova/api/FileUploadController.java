package ulitsa.raskolnikova.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import ulitsa.raskolnikova.model.FileUploadResult;
import ulitsa.raskolnikova.service.FileUploadService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RequestScoped
@Path("/upload")
public class FileUploadController {

    @Inject
    private FileUploadService fileUploadService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            
            if (formDataMap == null || formDataMap.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"No file provided\"}")
                        .build();
            }

            InputPart filePart = null;
            String fileName = null;
            
            for (Map.Entry<String, List<InputPart>> entry : formDataMap.entrySet()) {
                List<InputPart> inputParts = entry.getValue();
                for (InputPart inputPart : inputParts) {
                    String partFileName = getFileName(inputPart);
                    if (partFileName != null && (partFileName.endsWith(".csv") || partFileName.endsWith(".zip"))) {
                        filePart = inputPart;
                        fileName = partFileName;
                        break;
                    }
                }
                if (filePart != null) {
                    break;
                }
            }

            if (filePart == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"No valid CSV or ZIP file found\"}")
                        .build();
            }

            InputStream inputStream = filePart.getBody(InputStream.class, null);
            FileUploadResult result = fileUploadService.processFile(fileName, inputStream);
            
            return Response.ok()
                    .entity(result)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("{\"error\": \"Error processing file: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private String getFileName(InputPart inputPart) {
        try {
            String contentDisposition = inputPart.getHeaders().getFirst("Content-Disposition");
            if (contentDisposition != null) {
                String[] parts = contentDisposition.split(";");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("filename=")) {
                        String fileName = part.substring("filename=".length());
                        if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                            fileName = fileName.substring(1, fileName.length() - 1);
                        }
                        return fileName;
                    } else if (part.startsWith("filename*=")) {
                        int index = part.indexOf("''");
                        if (index != -1) {
                            return part.substring(index + 2);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}

