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

import java.net.ConnectException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.TimeoutException;

@NoArgsConstructor
@Provider
public class PersonExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        exception.printStackTrace();
        ErrorResponse error;
        
        Throwable cause = exception.getCause();
        if (cause == null) {
            cause = exception;
        }
        
        if (exception instanceof RollbackException && cause != null
                && cause.getMessage() != null && cause.getMessage().contains("duplicate")) {
            error = new ErrorResponse(
                    "BAD_REQUEST",
                    "Already exists"
            );
        } else if (exception instanceof PSQLException || exception instanceof PersistenceException) {
            if (cause instanceof SQLTimeoutException || 
                (cause instanceof SQLException && cause.getMessage() != null && 
                 (cause.getMessage().contains("timeout") || cause.getMessage().contains("Timeout")))) {
                error = new ErrorResponse(
                        "SERVICE_UNAVAILABLE",
                        "Database connection timeout. Please try again later."
                );
                return Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build();
            } else if (cause instanceof ConnectException || 
                      (cause instanceof SQLException && cause.getMessage() != null && 
                       (cause.getMessage().contains("Connection refused") || 
                        cause.getMessage().contains("could not connect")))) {
                error = new ErrorResponse(
                        "SERVICE_UNAVAILABLE",
                        "Database is unavailable. Please try again later."
                );
                return Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(error)
                        .build();
            } else {
                error = new ErrorResponse(
                        "BAD_REQUEST",
                        "Database error: " + (cause.getMessage() != null ? cause.getMessage() : "Unexpected error")
                );
            }
        } else if (exception instanceof TimeoutException || 
                   (cause instanceof TimeoutException) ||
                   (cause instanceof SQLTimeoutException)) {
            error = new ErrorResponse(
                    "SERVICE_UNAVAILABLE",
                    "Request timeout. Database may be unavailable."
            );
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        } else {
            error = new ErrorResponse(
                    "BAD_REQUEST",
                    cause.getMessage() != null ? cause.getMessage() : "Unexpected error occurred"
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
