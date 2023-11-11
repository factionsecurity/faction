package com.fuse.api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


import com.fuse.api.util.Support;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(value="/status")
@Path("/")
public class status {
    @GET
	@ApiOperation(
    value = "Gets the Status of Faction."
    )
	@ApiResponses(value = { 
			 @ApiResponse(code = 404, message = "Service is not online"),
			 @ApiResponse(code = 200, message = "Service is online")})
	@Path("/status")
	public Response createUser( @Context HttpServletRequest req){
		return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(Support.SUCCESS).build();
    }

    
}
