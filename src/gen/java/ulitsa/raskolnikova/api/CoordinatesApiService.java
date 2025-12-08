package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.api.*;
import ulitsa.raskolnikova.model.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import ulitsa.raskolnikova.model.CoordinatesRequest;
import ulitsa.raskolnikova.model.CoordinatesResponse;
import ulitsa.raskolnikova.model.PageCoordinates;
import ulitsa.raskolnikova.model.SearchRequest;

import java.util.List;

import java.io.InputStream;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")
public interface CoordinatesApiService {
      public Response createCoordinates(CoordinatesRequest coordinatesRequest, SecurityContext securityContext);
      public Response deleteCoordinates(Integer id, SecurityContext securityContext);
      public Response getCoordinatesById(Integer id, SecurityContext securityContext);
      public Response searchCoordinates(SearchRequest searchRequest, SecurityContext securityContext);
      public Response updateCoordinates(Integer id, CoordinatesRequest coordinatesRequest, SecurityContext securityContext);
}
