package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.model.CoordinatesRequest;
import ulitsa.raskolnikova.model.CoordinatesResponse;
import ulitsa.raskolnikova.model.PageCoordinates;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.api.CoordinatesApiService;

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
@Path("/coordinates")
@RequestScoped

@Api(description = "the coordinates API")


@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")

public class CoordinatesApi  {

  @Context SecurityContext securityContext;

  @Inject CoordinatesApiService delegate;


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = CoordinatesResponse.class, tags={ "coordinates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "созданные Coordinates", response = CoordinatesResponse.class) })
    public Response createCoordinates(@ApiParam(value = "" ,required=true) CoordinatesRequest coordinatesRequest) {
        return delegate.createCoordinates(coordinatesRequest, securityContext);
    }

    @DELETE
    @Path("/{id}")
    
    
    @ApiOperation(value = "", notes = "", response = Void.class, tags={ "coordinates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "удалены", response = Void.class) })
    public Response deleteCoordinates( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id) {
        return delegate.deleteCoordinates(id, securityContext);
    }

    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = CoordinatesResponse.class, tags={ "coordinates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "найденные Coordinates", response = CoordinatesResponse.class) })
    public Response getCoordinatesById( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id) {
        return delegate.getCoordinatesById(id, securityContext);
    }

    @POST
    @Path("/search")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "поиск и фильтрация Coordinates", notes = "", response = PageCoordinates.class, tags={ "coordinates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "страница Coordinates", response = PageCoordinates.class) })
    public Response searchCoordinates(@ApiParam(value = "" ,required=true) SearchRequest searchRequest) {
        return delegate.searchCoordinates(searchRequest, securityContext);
    }

    @PUT
    @Path("/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = CoordinatesResponse.class, tags={ "coordinates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "обновлённые Coordinates", response = CoordinatesResponse.class) })
    public Response updateCoordinates( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id, @ApiParam(value = "" ,required=true) CoordinatesRequest coordinatesRequest) {
        return delegate.updateCoordinates(id, coordinatesRequest, securityContext);
    }
}
