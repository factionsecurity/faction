package com.fuse.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.annotations.Parameter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fuse.dao.APIKeys;
import com.fuse.dao.Assessment;
import com.fuse.dao.Campaign;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.Feed;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
//import com.fuse.dao.VTImage;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;


@Api(value="/vulnerabilities")
@Path("/vulnerabilities")
public class vulnerabilities {
	private String SUCCESS = "[{ \"result\" : \"SUCCESS\"}]";
	private String SUCCESSMSG = "[{ \"result\" : \"SUCCESS\": %s}]";
	private String ERROR = "[{ \"result\" : \"ERROR\", \"message\",\"%s\"}]";
	
	private User getUser(EntityManager em, String apiKey){
		APIKeys keys = (APIKeys)em.createQuery("from APIKeys where key = :key").setParameter("key", apiKey).getResultList().stream().findFirst().orElse(null);
		if ( keys != null){
			return keys.getUser();
		}else{
			return null;
		}
		
	}
	
	///uses Reflection to create a JSON object out of a class
	private JSONObject dao2JSON(Object obj, Class cls){
		 JSONObject json = new JSONObject();
		 Method[] declaredMethods = cls.getDeclaredMethods();
		 for (Method dmethod : declaredMethods) {
			 
			 if(dmethod.getName().startsWith("get") && 
					 (
							 dmethod.getReturnType().equals(Integer.TYPE) ||
							 dmethod.getReturnType().equals(Long.TYPE) ||
							 dmethod.getReturnType().equals(Integer.class) ||
							 dmethod.getReturnType().equals(Long.class) ||
							 dmethod.getReturnType().equals(String.class) ||
							 dmethod.getReturnType().equals(Date.class)
							 )){
				 try {
					 Object o = dmethod.invoke(obj,  null);
					 if(o != null){
						 String name = dmethod.getName().replace("get", "");
						 if(dmethod.getReturnType().equals(Date.class))
							 json.put(name, "" + o);	
						 else
							 json.put(name, o);	 
					 }
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 
		 }
		 return json;

	}

	
	@GET
	@ApiOperation(
		    value = "Gets All Default Vulnerabilities Stored in the System.", 
		    notes = "",
		    response = Vulnerability.class,
		    responseContainer = "List"
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Default Vulnerabilites Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/default")
	public Response getalldefault(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey
			){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		JSONArray jarray = new JSONArray();
		try{
			User u = this.getUser(em, apiKey);
			if(u != null){
				
				try{
					List<DefaultVulnerability> vulns = (List<DefaultVulnerability>)em.createQuery("from DefaultVulnerability").getResultList();
					for(DefaultVulnerability v : vulns){
						v.updateRiskLevels(em);
						jarray.add(this.dao2JSON(v, DefaultVulnerability.class));	
					}
				}catch(Exception ex){
					
					ex.printStackTrace();
					return Response.status(400).entity(String.format(this.ERROR,"Unknown Error. Check logs.")).build();
					
				}
				
				
				
			}else{
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			}
		}finally{
			em.close();
		}
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	
	@GET
	@ApiOperation(
		    value = "Search for default vulnerbilities based on vulnerability name. ", 
		    notes = "Performs partial matching on the name parameter.",
		    response = Vulnerability.class,
		    responseContainer = "List"
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Matching Vulnerabilites Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/default/{name}")
	public Response getalldefault(
			@ApiParam(value = "Authentication Header", required = true)  @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Vulnerability name to search", required = true)  @PathParam("name") String name) 
	{
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		User u = this.getUser(em, apiKey);
		try{
			if(u != null){
				
				try{
					if(name.contains("'") || name.contains("\\") ){
						name = "";
					}
					List<DefaultVulnerability> vulns = (List<DefaultVulnerability>)em
							.createNativeQuery("{ name : {'$regex': '" + name+"', $options: 'i'} }", DefaultVulnerability.class)
							.getResultList();
					for(DefaultVulnerability v : vulns){
						v.updateRiskLevels(em);
						jarray.add(this.dao2JSON(v, DefaultVulnerability.class));	
					}
				}catch(Exception ex){
					
					ex.printStackTrace();
					return Response.status(400).entity(String.format(this.ERROR,"Unknown Error. Check logs.")).build();
				}
				
				
				
			}else{
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			}
		}finally{
			em.close();
		}
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	@GET
	@ApiOperation(
		    value = "Get a vulnerability based on its vuln id. ", 
		    notes = "",
		    response = Vulnerability.class,
		    responseContainer = "List"
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "Matching Vulnerability Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getvuln/{id}")
	public Response getVuln(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Vulnerability ID", required = true) @PathParam("id") Long vulnid
			){
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = this.getUser(em, apiKey);
			if(u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			Vulnerability vuln = em.find(Vulnerability.class, vulnid);
			
			if(vuln == null)
				return Response.status(400).entity(String.format(this.ERROR,"No Matching Result for vulnid.")).build();
				
			
			vuln.updateRiskLevels(em);
			return Response.status(200).entity(this.dao2JSON(vuln, Vulnerability.class).toJSONString()).build();
			//return this.dao2JSON(vuln, Vulnerability.class).toJSONString();	
		}finally{
			em.close();
		}
	}
	@GET
	@ApiOperation(
		    value = "Get Customized Risk Rankings ", 
		    notes = "",
		    response = RiskLevel.class,
		    responseContainer = "List"
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Risk Levels Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getrisklevels")
	public Response getRiskLevels(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey){
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = this.getUser(em, apiKey);
			if(u == null )
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			
			List<RiskLevel> levels = em.createQuery("from RiskLevel order by riskId desc").getResultList();
			JSONArray array = new JSONArray();
			for(RiskLevel level : levels){
				JSONObject obj = new JSONObject();
				obj.put("name", level.getRisk());
				obj.put("id", level.getRiskId());
				array.add(obj);
			}
			return Response.status(200).entity(array.toJSONString()).build();
		}finally{
			em.close();
		}
	}
	@GET
	@ApiOperation(
		    value = "Get Vulnerability Info based on it's assigned tracking id. ", 
		    notes = "",
		    response = Vulnerability.class,
		    responseContainer = "List"
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "Matching Vulnerability Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("gettracking/{track}")
	public Response getVuln(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Vulnerability Tracking ID", required = true) @PathParam("track") String tracking
			){
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = this.getUser(em, apiKey);
			if(u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			
			Vulnerability vuln = (Vulnerability)em.createQuery("from Vulnerability where tracking = :track")
					.setParameter("track", tracking)
					.getResultList().stream().findFirst().orElse(null);
			if(vuln == null)
				return Response.status(400).entity(String.format(this.ERROR,"No Matching Result for tracking id.")).build();
				
			vuln.updateRiskLevels(em);
			//return this.dao2JSON(vuln, Vulnerability.class).toJSONString();
			return Response.status(200).entity(this.dao2JSON(vuln, Vulnerability.class).toJSONString()).build();
		}finally{
			em.close();
		}
	}
	@POST
	@ApiOperation(
		    value = "Assign a tracking number to a vulnerability.", 
		    notes = ""
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "Vulnerability Successfully Assigned a Tracking ID.")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("settracking")
	public Response setTracking(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Vulnerability ID", required = true) @FormParam("vulnId") Long vulnid,
			@ApiParam(value = "Tracking ID", required = true) @FormParam("trackingId") String trackingid
			){
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = this.getUser(em, apiKey);
			if(u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			Vulnerability vuln = em.find(Vulnerability.class, vulnid);
			if(vuln == null){
				return Response.status(400).entity(String.format(this.ERROR,"No Matching Result for vulnid.")).build();
			}
			vuln.setTracking(trackingid);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(vuln);
			HibHelper.getInstance().commit();
			return Response.status(200).entity(this.SUCCESS).build();
		}finally{
			em.close();
		}
			
			
	}
	@POST
	@ApiOperation(
		    value = "Set the status (open/closed) of a vulnerability in production or development.", 
		    notes = "Set status to open or closed and set remediation dates for the vulnerability. "
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "Vulnerability Successfully Updated.")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("setstatus")
	public Response setStatus(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Vulnerability ID",  required = true) @FormParam("vulnId") Long vulnid,
			@ApiParam(value = "Tracking ID", required = true) @FormParam("trackingId") String trackingid,
			@ApiParam(value = "Set Status to Fixed in the development environment", required = false) @FormParam("isClosedDev") boolean statusDev,
			@ApiParam(value = "Set Status to Fixed in the production environment", required = false) @FormParam("isClosedProd") boolean statusProd,
			@ApiParam(value = "Set Date of remediation for development", required = false) @FormParam("devClosedDate") Date devClosed,
			@ApiParam(value = "Set Date of remediation for production", required = false) @FormParam("prodClosedDate") Date prodClosed
			) 
	{
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = this.getUser(em, apiKey);
			if(u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			
			if(vulnid != null && !vulnid.equals("")){
				
				Vulnerability vuln = em.find(Vulnerability.class, vulnid);
				if(vuln == null){
					return Response.status(400).entity(String.format(this.ERROR,"No Matching Result for vulnid.")).build();
				}
					
				if(statusDev){
					vuln.setDevClosed(devClosed);
				}
				if(statusDev){
					vuln.setClosed(prodClosed);
				}
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(vuln);
				HibHelper.getInstance().commit();
				return Response.status(200).entity(this.SUCCESS).build();
				
			}else if (trackingid !=null && !trackingid.equals("")){
				Vulnerability vuln = (Vulnerability)em.createQuery("from Vulnerability where tracking = :track")
						.setParameter("track", trackingid)
						.getResultList().stream().findFirst().orElse(null);
				if(vuln == null){
					return Response.status(400).entity(String.format(this.ERROR,"No Matching Result for trackingid.")).build();
				}
				if(statusDev){
					vuln.setDevClosed(devClosed);
				}
				if(statusDev){
					vuln.setClosed(prodClosed);
				}
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(vuln);
				HibHelper.getInstance().commit();
				return Response.status(200).entity(this.SUCCESS).build();
			}else{
				return Response.status(400).entity(String.format(this.ERROR,"Vulnid or tracking id not valid.")).build();
				
			}
		}finally{
			em.close();
		}
	}
	@POST
	@ApiOperation(
		    value = "Get All Vulnerabilities for the specified timeframe.", 
		    notes = ""
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "List of matching vulnerabilities.")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("all")
	public Response getall(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Start Date of Search", required = true) @FormParam("start") String start,
			@ApiParam(value = "End Date of Search", required = true) @FormParam("end") String end) 
	{
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try{
		
			User u = this.getUser(em, apiKey);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			if(u!=null && (u.getPermissions().isAssessor() || u.getPermissions().isManager() || u.getPermissions().isRemediation())){
				
				List<Vulnerability> vulns = null;
				if(end != null){
					vulns = em.createQuery("from Vulnerability as v where v.opened >= :start and v.opened <= :end")
							.setParameter("start", sdf.parse(start))
							.setParameter("end", sdf.parse(end))
							.getResultList();
				}else{
					vulns = em.createQuery("from Vulnerability as v where v.opened >= :start")
							.setParameter("start", sdf.parse(start))
							.getResultList();
				}
				
				for(Vulnerability v : vulns){
					v.updateRiskLevels(em);
					jarray.add(this.dao2JSON(v, Vulnerability.class));	
				}
			}else{
				return Response.status(401).entity(String.format(this.ERROR,"Not Authorized")).build();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}finally{
			em.close();
		}
		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	
}