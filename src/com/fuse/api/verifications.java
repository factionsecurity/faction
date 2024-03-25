package com.fuse.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fuse.api.util.DateParam;
import com.fuse.api.util.Support;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.VulnNotes;
import com.fuse.dao.Vulnerability;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(value="/verifications")
@Path("/verifications")
public class verifications {
	
	
	@GET
	@ApiOperation(
    value = "Gets the the Verification Queue for the user associated with the FACTION-API-KEY token header.", 
    notes = "Must have assessor permission. This returns only verificaitons assigned to the user. Will not show passed/failed verifications.",
    response = Verification.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "Assessor Queue Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queue")
	public Response getVerifications(@HeaderParam("FACTION-API-KEY") String apiKey){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = Support.getUser(em, apiKey);
			JSONArray jarray = new JSONArray();
			if(u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager()) && !Support.getTier().equals("consultant")){
				
				
				List<Verification> ver = (List<Verification>)em.createNativeQuery(
					"{\"assessor_Id\" : "+u.getId() + ",\"completed\" : ISODate(\"1970-01-01T00:00:00Z\")}"
					, Verification.class).getResultList();
				for(Verification v  : ver){
					v.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					JSONObject obj = Support.dao2JSON(v.getVerificationItems().get(0).getVulnerability(), Vulnerability.class);
					obj.put("Start", "" + v.getStart().getTime());
					obj.put("End", "" + v.getEnd().getTime());
					obj.put("AppId", v.getAssessment().getAppId());
					obj.put("AssessmentName", v.getAssessment().getName());
					String tracking = v.getVerificationItems().get(0).getVulnerability().getTracking();
					obj.put("Tracking", tracking==null ? "":tracking);
					obj.put("VerificationId", v.getId());
					obj.put("Completed", v.getCompleted() == null? "":""+v.getCompleted().getTime());
					obj.put("RemediationCompleted", v.getRemediationCompleted() == null? "": ""+v.getRemediationCompleted().getTime());
					obj.put("WorkFlowStatus", v.getWorkflowStatus());
					jarray.add(obj);	
				}
				
				
				return Response.status(200).entity(jarray.toJSONString()).build();
				
			}else{
				return Support.autherror();
			}
		}finally{
			em.close();
		}
		
		
	}
	
	@POST
	@ApiOperation(
    value = "Gets all verifications/retests in the system.", 
    notes = "Must have remeidation permission. This will return all open verifications, including passed/failed items. If a date"
    		+ " range is entered then it will only return passed/failed items for that date range."
    		+ "The API will also return all non-completed verifications reguardless of daterange.",
    response = Verification.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "All Retest Queues Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/all")
	public Response getAllVerifications(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessor Completed Date Range Start", required = false) @FormParam("start") DateParam start,
			@ApiParam(value = "Assessor Completed Date Range End", required = false) @FormParam("end") DateParam end
			){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = Support.getUser(em, apiKey);
			JSONArray jarray = new JSONArray();
			if(u != null && ( u.getPermissions().isRemediation()) && !Support.getTier().equals("consultant")){
				List<Verification> ver = new ArrayList();
				String mongo = "from Verification ";
				if(start != null && end != null) {
					mongo+= " where workflowStatus = :status or  (completed > :start and completed < :end)  order by completed";
					ver = (List<Verification>)em.createQuery(mongo)
							.setParameter("status", Verification.InAssessorQueue)
							.setParameter("start", start.getDate())
							.setParameter("end", end.getDate())
							.getResultList();
				}else {
					ver = (List<Verification>)em.createQuery(mongo).getResultList();
				}
				
				for(Verification v  : ver){
					v.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					JSONObject obj = Support.dao2JSON(v.getVerificationItems().get(0).getVulnerability(), Vulnerability.class);
					obj.put("Start", "" + v.getStart());
					obj.put("End", "" + v.getEnd());
					obj.put("AppId", v.getAssessment().getAppId());
					obj.put("AssessmentName", v.getAssessment().getName());
					String tracking = v.getVerificationItems().get(0).getVulnerability().getTracking();
					obj.put("Tracking", tracking==null ? "":tracking);
					obj.put("VerificationId", v.getId());
					obj.put("Notes",v.getVerificationItems().get(0).getNotes() == null ? "" : v.getVerificationItems().get(0).getNotes());
					obj.put("Completed", v.getCompleted() == null? "":""+v.getCompleted().getTime());
					obj.put("RemediationCompleted", v.getRemediationCompleted() == null? "": ""+v.getRemediationCompleted().getTime());
					obj.put("WorkFlowStatus", v.getWorkflowStatus());
					if(v.getCompleted() == null || v.getCompleted().getTime() == 0l)
						obj.put("Status", "In Progress");
					else {
						if(v.getVerificationItems().get(0).isPass()) {
							obj.put("Status", "Passed");
						}else {
							obj.put("Status", "Failed");
						}
					}
					jarray.add(obj);	
				}
				
				
				return Response.status(200).entity(jarray.toJSONString()).build();
				
			}else{
				return Support.autherror();
			}
		}finally{
			em.close();
		}
		
		
	}
	
	@POST
	@ApiOperation(
    value = "Gets all verifications/retests for a certian user.", 
    notes = "Must have remediation permission. Returns all Open verifications, even verifications that are passed/failed. "
    		+ "Date range searched completed Verifications but the API will return all non-completed verifications reguardless of daterange.",
    response = Verification.class,
    responseContainer = "List"
    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "All Retest Queues Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/userQueue")
	public Response getUserVerifications(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Username", required = true) @FormParam("username") String username,
			@ApiParam(value = "Assessor Completed Date Range Start", required = false) @FormParam("start") DateParam start,
			@ApiParam(value = "Assessor Completed Date Range End", required = false) @FormParam("end") DateParam end){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = Support.getUser(em, apiKey);
			JSONArray jarray = new JSONArray();
			if(u != null && ( u.getPermissions().isRemediation()) && !Support.getTier().equals("consultant")){
				
				User assessor = (User)em.createQuery("from User where username = :uname")
						.setParameter("uname", username)
						.getResultList().stream().findFirst().orElse(null);
				
				if(assessor == null) {
					return Response.status(400).entity(String.format(Support.ERROR,"User does not exist")).build();
				}
				
				List<Verification> ver = new ArrayList();
				String mongo = "from Verification v where v.assessor = :id ";
				if(start != null && end != null) {
					mongo+= " and (v.workflowStatus = :status or  (v.completed > :start and v.completed < :end))  order by completed";
					ver = (List<Verification>)em.createQuery(mongo)
							.setParameter("id", assessor)
							.setParameter("status", Verification.InAssessorQueue)
							.setParameter("start", start.getDate())
							.setParameter("end", end.getDate())
							.getResultList();
				}else {
					ver = (List<Verification>)em.createQuery(mongo).setParameter("id", assessor).getResultList();
				}
				for(Verification v  : ver){
					v.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					JSONObject obj = Support.dao2JSON(v.getVerificationItems().get(0).getVulnerability(), Vulnerability.class);
					obj.put("Start", "" + v.getStart());
					obj.put("End", "" + v.getEnd());
					obj.put("AppId", v.getAssessment().getAppId());
					obj.put("AssessmentName", v.getAssessment().getName());
					String tracking = v.getVerificationItems().get(0).getVulnerability().getTracking();
					obj.put("Tracking", tracking==null ? "":tracking);
					obj.put("VerificationId", v.getId());
					obj.put("Notes",v.getVerificationItems().get(0).getNotes() == null ? "" : v.getVerificationItems().get(0).getNotes() );
					obj.put("Completed", v.getCompleted() == null? "":""+v.getCompleted().getTime());
					obj.put("RemediationCompleted", v.getRemediationCompleted() == null? "": ""+v.getRemediationCompleted().getTime());
					obj.put("WorkFlowStatus", v.getWorkflowStatus());
					if(v.getCompleted() == null || v.getCompleted().getTime() == 0l)
						obj.put("Status", "In Progress");
					else {
						if(v.getVerificationItems().get(0).isPass()) {
							obj.put("Status", "Passed");
						}else {
							obj.put("Status", "Failed");
						}
					}
					jarray.add(obj);	
				}
				
				
				return Response.status(200).entity(jarray.toJSONString()).build();
				
			}else{
				return Support.autherror();
			}
		}finally{
			em.close();
		}
		
		
	}
	
	@POST
	@ApiOperation(
		    value = "Pass or Fail a verification/retest.", 
		    notes = "."
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			 @ApiResponse(code = 400, message = "Bad Request."),
			 @ApiResponse(code = 200, message = "Post Successful.")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("passfail")
	public Response retest(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Verification Id", required = true) @FormParam("verificationID") Long verId,
			@ApiParam(value = "Notes", required = false) @FormParam("notes") String notes,
			@ApiParam(value = "Did the Test Pass (boolean)", required = true) @FormParam("passed") boolean isPass,
			@ApiParam(value = "Close finding as passed in production. Otherwise is set to closed in development", required = false) @FormParam("inProd") boolean closeinprod,
			@ApiParam(value = "Date of Completion. Blank will set current date.", required = false) @FormParam("completedDate") DateParam completed
			){
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = Support.getUser(em, apiKey);
			JSONArray jarray = new JSONArray();
			if(u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isAssessor()|| u.getPermissions().isManager()) || Support.getTier().equals("consultant"))
				return Support.error("Not Authorized");
			
			Verification ver = em.find(Verification.class, verId);
			if(ver == null)
				return Support.error("Verification does not exist for givern verificationID.");
								
			
			Vulnerability vuln = ver.getVerificationItems().get(0).getVulnerability();
			
			if(completed == null) {
				ver.setCompleted(new Date());
				if(isPass) {
					vuln.setDevClosed(new Date());
					if(closeinprod)
						vuln.setClosed(new Date());
				}
			}else {
				ver.setCompleted(completed.getDate());
				if(isPass) {
					vuln.setDevClosed(completed.getDate());
					if(closeinprod)
						vuln.setClosed(completed.getDate());
				}
			}
				
			
			VulnNotes vulnNote = new VulnNotes();
			vulnNote.setCreatorObj(u);
			vulnNote.setCreator(u.getId());
			vulnNote.setCreated(new Date());
			String status ="Failed";
			if(isPass)
				status = "Passed";
			vulnNote.setNote("<i>Verification Closed by " + u.getFname() + " " + u.getLname() + " via API</i><br/>The issue is now in a " + status + " state.<br>" + notes);
			vulnNote.setUuid("nodelete");
			vulnNote.setVulnId(ver.getVerificationItems().get(0).getVulnerability().getId());
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(vuln);
			em.persist(vulnNote);
			em.remove(ver.getVerificationItems().get(0));
			em.remove(ver);
			
			HibHelper.getInstance().commit();
			
			return Support.success();
			
				
		}finally{
			em.close();
		}
	}
			
	@POST
	@ApiOperation(
		    value = "Schedule a Retest.", 
		    notes = "You can assign assessors based user id. Vulnerabilities can be assigned based on the Vulnerability ID or the Tracking ID."
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			 @ApiResponse(code = 400, message = "Bad Request."),
			 @ApiResponse(code = 200, message = "Post Successful. Returns the Verification ID.")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("retest")
	public Response retest(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assign an assesor username.", required = true) @FormParam("assessorId") String assessorname,
			@ApiParam(value = "Assign an remediation username.", required = false) @FormParam("remediationId") String remediationname,
			@ApiParam(value = "Vulnerability ID to Retested.", required = false) @FormParam("vulnId") Long vulnid,
			@ApiParam(value = "Vulnerability Tracking ID to Retested.", required = false) @FormParam("trackingId") String tracking,
			@ApiParam(value = "Date to Start the verification", required = true) @FormParam("start") DateParam start,
			@ApiParam(value = "Date to when the verification should complete", required = true) @FormParam("end") DateParam end,
			@ApiParam(value = "Scope, credentials, additional info for the assessor to re-test", required = true) @FormParam("notes") String notes
			
			){
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			User u = Support.getUser(em, apiKey);
			JSONArray jarray = new JSONArray();
			if(u == null || !(u.getPermissions().isRemediation()) || Support.getTier().equals("consultant"))
				return Support.error("Not Authorized");
			User assessor;
			User remediation;
		
			assessor = (User) em.createQuery("from User where username = :name")
					.setParameter("name", assessorname)
					.getResultList().stream().findFirst().orElse(null);
			if(assessor == null)
				return Support.error("Assessor Not Found");
		
			remediation = (User) em.createQuery("from User where username = :name")
					.setParameter("name", remediationname)
					.getResultList().stream().findFirst().orElse(null);
			if(remediation == null)
				return Support.error("Remediation User Not Found");
				
			
			Vulnerability vuln = null;
			if(vulnid != null)
				vuln = em.find(Vulnerability.class, vulnid);
			else{
				vuln = (Vulnerability)em.createQuery("from Vulnerability where tracking = :track")
						.setParameter("track", tracking)
						.getResultList().stream().findFirst().orElse(null);
			}
			if(vuln == null)
				return Support.error("Vulnerability Id or Tracking ID not found.");
			
			List<Verification> vers = em.createQuery("from Verification").getResultList();
			for(Verification vvv : vers) {
				if(vvv.getVerificationItems().get(0).getVulnerability().getId() == vuln.getId()) {
					return Support.error("This Verificaiton is already scheduled");
				}
			}
			
			Assessment asmt = em.find(Assessment.class, vuln.getAssessmentId());
			Verification ver = new Verification();
			ver.setAssessment(asmt);
			ver.setAssessor(assessor);
			ver.setAssignedRemediation(remediation);
			ver.setStart(start.getDate());
			ver.setEnd(end.getDate());
			ver.setNotes(notes);
			VerificationItem item = new VerificationItem();
			item.setVulnerability(vuln);
			ver.setVerificationItems(new ArrayList<VerificationItem>());
			ver.getVerificationItems().add(item);
			ver.setCompleted(new Date(0));
			ver.setWorkflowStatus(Verification.InAssessorQueue);
			
			VulnNotes vulnNote = new VulnNotes();
			vulnNote.setCreatorObj(u);
			vulnNote.setCreator(u.getId());
			vulnNote.setCreated(new Date());
			vulnNote.setNote("<i>Verification Assigned to " +assessor.getFname() + " " + assessor.getLname() + " via API</i>");
	
			vulnNote.setUuid("nodelete");
			vulnNote.setVulnId(vuln.getId());
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(item);
			em.persist(ver);
			em.persist(vulnNote);
			HibHelper.getInstance().commit();
			return Support.success("\"verificationId\" : \"" + ver.getId()+"\"");
			//return String.format(this.SUCCESSMSG, "\"verificationId\" : \"" + ver.getId()+"\""); 
		
		}finally{
			em.close();
		}
		
	}
	
	
	
	
	
	

}
