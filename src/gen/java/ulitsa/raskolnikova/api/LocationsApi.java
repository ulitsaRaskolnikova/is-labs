package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.model.LocationRequest;
import ulitsa.raskolnikova.model.LocationResponse;
import ulitsa.raskolnikova.model.PageLocation;
import ulitsa.raskolnikova.model.SearchRequest;
import ulitsa.raskolnikova.api.LocationsApiService;

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
@Path("/locations")
@RequestScoped

@Api(description = "the locations API")


@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")

public class LocationsApi  {

  @Context SecurityContext securityContext;

  @Inject LocationsApiService delegate;


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "создать новую Location", notes = "", response = LocationResponse.class, tags={ "locations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "созданная Location", response = LocationResponse.class) })
    public Response createLocation(@ApiParam(value = "" ,required=true) LocationRequest locationRequest) {
        return delegate.createLocation(locationRequest, securityContext);
    }

    @DELETE
    @Path("/{id}")
    
    
    @ApiOperation(value = "", notes = "", response = Void.class, tags={ "locations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "удалена", response = Void.class) })
    public Response deleteLocation( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id) {
        return delegate.deleteLocation(id, securityContext);
    }

    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = LocationResponse.class, tags={ "locations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "найденная Location", response = LocationResponse.class),
        @ApiResponse(code = 404, message = "не найдено", response = Void.class) })
    public Response getLocationById( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id) {
        return delegate.getLocationById(id, securityContext);
    }

    @POST
    @Path("/search")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "поиск и фильтрация Location", notes = "", response = PageLocation.class, tags={ "locations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "страница Location", response = PageLocation.class) })
    public Response searchLocations(@ApiParam(value = "" ,required=true) SearchRequest searchRequest) {
        return delegate.searchLocations(searchRequest, securityContext);
    }

    @PUT
    @Path("/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "", notes = "", response = LocationResponse.class, tags={ "locations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "обновлённая Location", response = LocationResponse.class) })
    public Response updateLocation( @Min(1)@ApiParam(value = "идентификатор ресурса",required=true) @PathParam("id") Integer id, @ApiParam(value = "" ,required=true) LocationRequest locationRequest) {
        return delegate.updateLocation(id, locationRequest, securityContext);
    }
}
