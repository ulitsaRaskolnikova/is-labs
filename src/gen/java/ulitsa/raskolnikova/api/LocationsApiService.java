package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.api.*;
import ulitsa.raskolnikova.model.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import ulitsa.raskolnikova.model.LocationRequest;
import ulitsa.raskolnikova.model.LocationResponse;
import ulitsa.raskolnikova.model.PageLocation;
import ulitsa.raskolnikova.model.SearchRequest;

import java.util.List;

import java.io.InputStream;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")
public interface LocationsApiService {
      public Response createLocation(LocationRequest locationRequest, SecurityContext securityContext);
      public Response deleteLocation(Integer id, SecurityContext securityContext);
      public Response getLocationById(Integer id, SecurityContext securityContext);
      public Response searchLocations(SearchRequest searchRequest, SecurityContext securityContext);
      public Response updateLocation(Integer id, LocationRequest locationRequest, SecurityContext securityContext);
}
