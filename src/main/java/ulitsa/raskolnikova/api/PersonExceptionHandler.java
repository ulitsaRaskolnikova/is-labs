package ulitsa.raskolnikova.api;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.RollbackException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PSQLException;

@NoArgsConstructor
@Provider
public class PersonExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        exception.printStackTrace();
        ErrorResponse error;
        if (exception instanceof RollbackException && exception.getCause() != null
                && exception.getCause().getMessage().contains("duplicate")
                || exception instanceof PSQLException
                || exception instanceof PersistenceException) {
            error = new ErrorResponse(
                    "BAD_REQUEST",
                    "Already exists"
            );
        } else {
            error = new ErrorResponse(
                    "BAD_REQUEST",
                    exception.getCause() != null ? exception.getCause().getMessage() : "Unexpected error occurred"
            );
        }

        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlRootElement
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
