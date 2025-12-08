package ulitsa.raskolnikova.api;

import ulitsa.raskolnikova.model.Color;
import ulitsa.raskolnikova.model.Country;
import ulitsa.raskolnikova.api.OperationsApiService;

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
@Path("/operations")
@RequestScoped

@Api(description = "the operations API")


@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSCXFCDIServerCodegen", comments = "Generator version: 7.7.0")

public class OperationsApi  {

  @Context SecurityContext securityContext;

  @Inject OperationsApiService delegate;


    @GET
    @Path("/countByHairColorAndLocation")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "вывести количество людей с заданным цветом волос в пределах указанной локации", notes = "", response = Integer.class, tags={ "operations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "количество людей", response = Integer.class) })
    public Response countByHairColorAndLocation( @NotNull @ApiParam(value = "",required=true, allowableValues="RED, BLACK, BLUE, WHITE, BROWN")  @QueryParam("hairColor") Color hairColor,  @NotNull  @Min(1)@ApiParam(value = "",required=true)  @QueryParam("locationId") Integer locationId) {
        return delegate.countByHairColorAndLocation(hairColor, locationId, securityContext);
    }

    @GET
    @Path("/countByHeight")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "вернуть количество Person с заданным height", notes = "", response = Integer.class, tags={ "operations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "количество людей", response = Integer.class) })
    public Response countByHeight( @NotNull  @Min(1)@ApiParam(value = "",required=true)  @QueryParam("height") Integer height) {
        return delegate.countByHeight(height, securityContext);
    }

    @DELETE
    @Path("/deleteByNationality")
    
    
    @ApiOperation(value = "удалить один объект с заданной национальностью", notes = "", response = Void.class, tags={ "operations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "объект удалён", response = Void.class),
        @ApiResponse(code = 404, message = "объект не найден", response = Void.class) })
    public Response deleteByNationality( @NotNull @ApiParam(value = "",required=true, allowableValues="RUSSIA, UNITED_KINGDOM, SPAIN, ITALY, SOUTH_KOREA")  @QueryParam("nationality") Country nationality) {
        return delegate.deleteByNationality(nationality, securityContext);
    }

    @GET
    @Path("/eyeColorShare")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "вывести долю людей с заданным цветом глаз в процентах", notes = "", response = Double.class, tags={ "operations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "процент людей с данным цветом глаз", response = Double.class) })
    public Response eyeColorShare( @NotNull @ApiParam(value = "",required=true, allowableValues="RED, BLACK, BLUE, WHITE, BROWN")  @QueryParam("eyeColor") Color eyeColor) {
        return delegate.eyeColorShare(eyeColor, securityContext);
    }

    @GET
    @Path("/sumHeight")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "рассчитать сумму значений height для всех Person", notes = "", response = Double.class, tags={ "operations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "сумма высот", response = Double.class) })
    public Response sumHeight() {
        return delegate.sumHeight(securityContext);
    }
}
