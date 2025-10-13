package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.api.*;
import ulitsa.raskolnikova.model.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import ulitsa.raskolnikova.model.PagePerson;
import ulitsa.raskolnikova.model.PersonRequest;
import ulitsa.raskolnikova.model.PersonResponse;
import ulitsa.raskolnikova.model.SearchRequest;

import java.util.List;

import java.io.InputStream;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")
public interface PersonsApiService {
      public Response createPerson(PersonRequest personRequest, SecurityContext securityContext);
      public Response deletePerson(Integer id, SecurityContext securityContext);
      public Response getPersonById(Integer id, SecurityContext securityContext);
      public Response searchPersons(SearchRequest searchRequest, SecurityContext securityContext);
      public Response updatePerson(Integer id, PersonRequest personRequest, SecurityContext securityContext);
}
