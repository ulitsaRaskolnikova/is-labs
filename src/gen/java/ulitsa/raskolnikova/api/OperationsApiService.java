package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.api.*;
import ulitsa.raskolnikova.model.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import ulitsa.raskolnikova.model.Color;
import ulitsa.raskolnikova.model.Country;

import java.util.List;

import java.io.InputStream;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")
public interface OperationsApiService {
      public Response countByHairColorAndLocation(Color hairColor, Integer locationId, SecurityContext securityContext);
      public Response countByHeight(Integer height, SecurityContext securityContext);
      public Response deleteByNationality(Country nationality, SecurityContext securityContext);
      public Response eyeColorShare(Color eyeColor, SecurityContext securityContext);
      public Response sumHeight(SecurityContext securityContext);
}
