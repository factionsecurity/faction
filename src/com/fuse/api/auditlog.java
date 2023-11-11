package com.fuse.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fuse.api.util.DateParam;
import com.fuse.api.util.Support;
import com.fuse.dao.APIKeys;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="/auditlog")
@Path("/auditlog")
public class auditlog {
	
	
	
	@POST
	@ApiOperation(
    value = "Get the audit log for a user given time frame.", 
    notes = "",
    response = AuditLog.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "Audit Log Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/log")
	public Response getLog(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Start Date", required = true) @FormParam("start") DateParam start,
			@ApiParam(value = "End Date", required = true) @FormParam("end") DateParam end
			){
		JSONArray jarray = new JSONArray();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
	
		try {
			User user = Support.getUser(em, apiKey);
			if(user == null)
				return Response.status(401).build();
			if(!user.getPermissions().isAdmin())
				return Response.status(401).build();
			
			List<AuditLog>logs = em
					.createQuery("from AuditLog where timestamp > :start and timestamp < :end  order by timestamp")
					.setParameter("start", start.getDate())
					.setParameter("end", end.getDate())
					.getResultList();
			
			
			for(AuditLog al : logs) {
				JSONObject json = Support.dao2JSON(al, AuditLog.class);
				jarray.add(json);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			em.close();
		}
		
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	
	@POST
	@ApiOperation(
    value = "Get the audit log for a assessments", 
    notes = "",
    response = AuditLog.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "Audit Log Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/assessmentlog")
	public Response getAssessmentLog(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Start Date", required = true) @FormParam("start") DateParam start,
			@ApiParam(value = "End Date", required = true) @FormParam("end") DateParam end
			){
		JSONArray jarray = new JSONArray();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
	
		try {
			User user = Support.getUser(em, apiKey);
			if(user == null)
				return Response.status(401).build();
			if(!user.getPermissions().isAdmin())
				return Response.status(401).build();
			
			List<AuditLog>logs = em
					.createQuery("from AuditLog where timestamp > :start and timestamp < :end  and compname = :comp order by timestamp")
					.setParameter("start", start.getDate())
					.setParameter("end", end.getDate())
					.setParameter("comp", AuditLog.CompAssessment)
					.getResultList();
			
			
			for(AuditLog al : logs) {
				JSONObject json = Support.dao2JSON(al, AuditLog.class);
				jarray.add(json);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			em.close();
		}
		
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	@POST
	@ApiOperation(
    value = "Get the audit log for a assessments", 
    notes = "",
    response = AuditLog.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "Audit Log Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/assessmentlog/{aid}")
	public Response getAssessmentLog(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
			@ApiParam(value = "Start Date", required = true) @FormParam("start") DateParam start,
			@ApiParam(value = "End Date", required = true) @FormParam("end") DateParam end
			){
		JSONArray jarray = new JSONArray();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
	
		try {
			User user = Support.getUser(em, apiKey);
			if(user == null)
				return Response.status(401).build();
			if(!user.getPermissions().isAdmin())
				return Response.status(401).build();
			
			List<AuditLog>logs = em
					.createQuery("from AuditLog where timestamp > :start and timestamp < :end  "
							+ "and compname = 'Assessment' and compid = :id order by timestamp")
					.setParameter("start", start.getDate())
					.setParameter("end", end.getDate())
					.setParameter("compid", aid)
					.getResultList();
			
			
			for(AuditLog al : logs) {
				JSONObject json = Support.dao2JSON(al, AuditLog.class);
				jarray.add(json);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			em.close();
		}
		
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	
	@POST
	@ApiOperation(
    value = "Get the audit log for a user for given time frame.", 
    notes = "",
    response = AuditLog.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { 
			@ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "Audit Log Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/userlog")
	public Response getUserLog(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "username", required = true) @FormParam("username") String username,
			@ApiParam(value = "Start Date", required = true) @FormParam("start") DateParam start,
			@ApiParam(value = "End Date", required = true) @FormParam("end") DateParam end
			){
		JSONArray jarray = new JSONArray();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
	
		try {
			User user = Support.getUser(em, apiKey);
			if(user == null)
				return Response.status(401).build();
			if(!user.getPermissions().isAdmin())
				return Response.status(401).build();
			
			List<AuditLog>logs = em
					.createQuery("from AuditLog where timestamp > :start and timestamp < :end  and username = :username order by timestamp")
					.setParameter("start", start.getDate())
					.setParameter("end", end.getDate())
					.setParameter("username", username)
					.getResultList();
			
			
			for(AuditLog al : logs) {
				JSONObject json = Support.dao2JSON(al, AuditLog.class);
				jarray.add(json);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			em.close();
		}
		
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	
	


}
