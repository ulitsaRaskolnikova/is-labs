package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.model.PagePerson;
import ulitsa.raskolnikova.model.PersonRequest;
import ulitsa.raskolnikova.model.PersonResponse;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.api.PersonsApiService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.PATCH;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
@Path("/persons")
@RequestScoped

@Api(description = "the persons API")


@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")

public class PersonsApi  {

  @Context SecurityContext securityContext;

  @Inject PersonsApiService delegate;


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "создать новый объект Person", notes = "", response = PersonResponse.class, tags={ "persons" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "созданный Person", response = PersonResponse.class) })
    public Response createPerson(@ApiParam(value = "" ,required=true) PersonRequest personRequest) {
        return delegate.createPerson(personRequest, securityContext);
    }

    @DELETE
    @Path("/{id}")
    
    
    @ApiOperation(value = "удалить Person", notes = "", response = Void.class, tags={ "persons" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "удалён", response = Void.class) })
    public Response deletePerson( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id) {
        return delegate.deletePerson(id, securityContext);
    }

    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "получить Person по id", notes = "", response = PersonResponse.class, tags={ "persons" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "найденный Person", response = PersonResponse.class),
        @ApiResponse(code = 404, message = "не найдено", response = Void.class) })
    public Response getPersonById( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id) {
        return delegate.getPersonById(id, securityContext);
    }

    @POST
    @Path("/search")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "поиск и фильтрация Person", notes = "", response = PagePerson.class, tags={ "persons" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "страница Person", response = PagePerson.class) })
    public Response searchPersons(@ApiParam(value = "" ,required=true) SearchRequest searchRequest) {
        return delegate.searchPersons(searchRequest, securityContext);
    }

    @PUT
    @Path("/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "обновить Person", notes = "", response = PersonResponse.class, tags={ "persons" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "обновлённый Person", response = PersonResponse.class),
        @ApiResponse(code = 400, message = "ошибка валидации", response = Void.class),
        @ApiResponse(code = 404, message = "не найдено", response = Void.class) })
    public Response updatePerson( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id, @ApiParam(value = "" ,required=true) PersonRequest personRequest) {
        return delegate.updatePerson(id, personRequest, securityContext);
    }
}
