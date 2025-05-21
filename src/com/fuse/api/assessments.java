package com.fuse.api;

import java.util.ArrayList;
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

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fuse.api.util.DateParam;
import com.fuse.api.util.Support;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.utils.FSUtils;


import ch.qos.logback.core.pattern.parser.Node;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/assessments")
@Path("/assessments")
public class assessments {
	@GET
	@ApiOperation(value = "Gets the the Assessment Queue for the user associated with the FACTION-API-KEY token header.", notes = "Gets the the Assessment Queue for the user associated with the FACTION-API-KEY token header.", response = Assessment.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 200, message = "Assessor Queue Returned") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queue")
	public Response getAssessments(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {
				;
				try {
					List<Assessment> asmts = AssessmentQueries.getAssessmentsByUserId(em, u, u.getId(),
							AssessmentQueries.OnlyNonCompleted);
					for (Assessment a : asmts) {
						AssessmentQueries.updateImages(a);
						jarray.add(Support.dao2JSON(a, Assessment.class));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			} else {
				// String.format(this.ERROR,"Not Authorized");
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}

		return Response.status(200).entity(jarray.toJSONString()).build();
	}
	/*
	 * getVulns - get vulns for current assessment
	 * 
	 */

	@GET
	@ApiOperation(value = "Gets the the Vulnererabilies for the Application.", notes = "Application ID is not the same as Assessment ID. An Application ID can span multiple assessments.", response = Vulnerability.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Application Does not exist."),
			@ApiResponse(code = 200, message = "Returns json array of vulnerbilities") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/vulns/{aid}")
	public Response getVulns(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

				List<Assessment> asmts = (List<Assessment>) em.createQuery("from Assessment where id = :aid")
						.setParameter("aid", aid).getResultList();
				if (asmts == null)
					return Response.status(401).entity(String.format(Support.ERROR, "No Assessment")).build();
				for (Assessment a : asmts) {
					for (Vulnerability v : a.getVulns()) {
						AssessmentQueries.updateImages(a, v);
						jarray.add(Support.dao2JSON(v, Vulnerability.class));
					}
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
		return Response.status(200).entity(jarray.toJSONString()).build();

	}

	/*
	 * getHistory - getAssessment History
	 * 
	 */

	@GET
	@ApiOperation(value = "Gets the the Vulnererabilies for the Application.", notes = "Application ID is not the same as Assessment ID. An Application ID can span multiple assessments.", response = Vulnerability.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Assessment Does not exist."),
			@ApiResponse(code = 200, message = "Returns json array of vulnerbilities") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/history/{appid}")
	public Response getHistory(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Application ID Header", required = true) @PathParam("appid") String appid) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

				List<Assessment> asmts = (List<Assessment>) em.createQuery("from Assessment where appId = :appid")
						.setParameter("appid", appid).getResultList();
				if (asmts == null)
					return Response.status(401).entity(String.format(Support.ERROR, "No Assessment")).build();
				for (Assessment a : asmts) {
					for (Vulnerability v : a.getVulns()) {
						AssessmentQueries.updateImages(a, v);
						v.updateRiskLevels(em);
						jarray.add(Support.dao2JSON(v, Vulnerability.class));
					}
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
		return Response.status(200).entity(jarray.toJSONString()).build();

	}

	/*
	 * updateNotes - Add notes to an existing assessment
	 * 
	 */

	@POST
	@ApiOperation(value = "Update the Assessment Notes.", notes = "Application ID is not the same as Assessment ID. An Application ID can span multiple assessments.")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Assessment Does not exist."),
			@ApiResponse(code = 200, message = "Request Successfull") })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/notes/{aid}")
	public Response updateNotes(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Integer aid,
			@Context HttpServletRequest req) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

				Assessment asmt = (Assessment) em.createQuery("from Assessment where Id = :aid")
						.setParameter("aid", aid).getResultList().stream().findFirst().orElse(null);
				if (asmt == null)
					return Response.status(400).entity(String.format(Support.ERROR, "No Assessment")).build();
				for (User hacker : asmt.getAssessor()) {
					if (hacker.getId() == u.getId()) {
						asmt.setNotes(req.getParameter("notes"));
						return Response.status(200).entity(Support.SUCCESS).build();
					} else {
						return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
					}
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
		return Response.status(200).entity(Support.SUCCESS).build();

	}

	/*
	 * getVuln - get details for a specific vulnerability
	 * 
	 */

	@GET
	@ApiOperation(value = "Gets details for a specific vulnerability.", notes = "Pulls add details and exploits steps for a vulnerability id.", response = Vulnerability.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Bad Request."),
			@ApiResponse(code = 200, message = "Returns Vulnerability and Exploit Step Information") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/vuln/{vid}")
	public Response getVuln(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Vulnerability ID", required = true) @PathParam("vid") Long vid) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

				Vulnerability v = (Vulnerability) em.createQuery("from Vulnerability where id = :id")
						.setParameter("id", vid).getResultList().stream().findFirst().orElse(null);

				if (v.getDescription() == null && v.getDefaultVuln() != null) {
					v.setDescription(v.getDefaultVuln().getDescription());
				} else if (v.getDescription() == null) {
					v.setDescription("");
				}
				if (v.getRecommendation() == null && v.getDefaultVuln() != null) {
					v.setRecommendation(v.getDefaultVuln().getRecommendation());
				} else if (v.getRecommendation() == null) {
					v.setRecommendation("");
				}
				if (v.getDetails() == null) {
					v.setDetails("");
				}else {
					v.setDetails(v.getDetails().replaceAll("\n", "\r\n"));
				}
				
				AssessmentQueries.updateImages(em, v);
				
				if (v == null)
					return Response.status(400).entity(String.format(Support.ERROR, "Vulnerability Does Not Exist."))
							.build();
				v.updateRiskLevels(em);
				JSONObject j = Support.dao2JSON(v, Vulnerability.class);
				jarray.add(j);

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
		return Response.status(200).entity(jarray.toJSONString()).build();

	}

	/*
	 * addNewVuln - Creates a new vulnerability for the the specified assessment.
	 */

	@POST
	@ApiOperation(value = "Add a new vulnerability to the assessment. All Base64 encoded inputs supoort HTML and Markdown syntax", notes = "Suports HTML and MarkDown Syntax", response = Assessment.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Bad Request."),
			@ApiResponse(code = 200, message = "Returns the ID of the newly created vulnerability.") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/addVuln/{aid}")
	public Response addNewVuln(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
			@ApiParam(value = "Vulnerability Name", required = true) @FormParam("name") String name,
			@ApiParam(value = "Vulnerability Description (Base64 Encoded)", required = false) @FormParam("description") String description,
			@ApiParam(value = "Vulnerability Recommendation (Base64 Encoded)", required = false) @FormParam("recommendation") String recommendation,
			@ApiParam(value = "Exploit Details Information (Base64 Encoded)", required = false) @FormParam("details") String details,
			@ApiParam(value = "Default Vulnerability ID", required = false) @FormParam("default_vuln_id") Long defaultVulnId,
			@ApiParam(value = "Severity ID 0-9", required = true) @FormParam("severity") Long severity) {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);

			if (severity == null)
				severity = 0l;
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {
				String query = "{ \"assessor\" : " + u.getId() + ", \"_id\" : " + aid
						+ ",   \"completed\" : {$exists: false} }";
				Assessment a = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList().stream()
						.findFirst().orElse(null);
				if (a == null) {
					return Response.status(400).entity(String.format(Support.ERROR, "Assessment Not Found.")).build();
				}

				// Check if we are in Peer review
				/// Assessment should be locked and not editable when PR is enabled.
				PeerReview prTemp = null;
				try {
					
					prTemp = (PeerReview) em
							.createNativeQuery("{\"assessment_id\" : " + a.getId() + "}", PeerReview.class)
							.getResultList().stream().findFirst().orElse(null);

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				Boolean prSubmitted = false;
				Boolean prComplete = false;
				if (prTemp != null) {
					prSubmitted = true;

					if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
						prComplete = true;
					} else
						prComplete = false;
				} else
					prSubmitted = false;

				if (!(!prSubmitted || prComplete)) {
					return Response.status(400)
							.entity(String.format(Support.ERROR, "Assessment Locked for Peer Review")).build();
				}

				if (name != null && !name.equals("")) {
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					Vulnerability v = new Vulnerability();
					DefaultVulnerability dv = null;
					if (defaultVulnId != null) {
						dv = (DefaultVulnerability) em
								.createQuery("from DefaultVulnerability where id = :id")
								.setParameter("id", defaultVulnId).getResultList().stream().findFirst().orElse(null);
						v.setDefaultVuln(dv);
						v.setDescription(dv.getDescription());
						v.setRecommendation(dv.getRecommendation());
						v.setCategory(dv.getCategory());
					}else {
						dv = em.createQuery("from DefaultVulnerability where name = 'Generic Vulnerability'", DefaultVulnerability.class).getResultList().stream().findFirst().orElse(null);
						Category cat = em.createQuery("from Category where name = 'Uncategorized'", Category.class).getResultList().stream().findFirst().orElse(null);
						if(cat == null) {
							cat = new Category();
							cat.setName("Uncategorized");
							HibHelper.getInstance().preJoin();
							em.joinTransaction();
							em.persist(cat);
							em.persist(dv);
							HibHelper.getInstance().commit();
						}
						if(dv == null) {
							dv = new DefaultVulnerability();
							dv.setActive(true);
							dv.setCategory(cat);
							dv.setName("Generic Vulnerability");
							dv.setLikelyhood(4);
							dv.setOverall(4);
							dv.setImpact(4);
							dv.setDescription("");
							dv.setRecommendation("");
							HibHelper.getInstance().preJoin();
							em.joinTransaction();
							em.persist(cat);
							em.persist(dv);
							HibHelper.getInstance().commit();
						}
						v.setDefaultVuln(dv);
						v.setCategory(cat);
					}

					v.setName(name);
					v.setLikelyhood(severity);
					v.setImpact(severity);
					v.setOverall(severity);
					v.setAssessmentId(a.getId());
					if (description != null)
						v.setDescription(decodeAndSanitize(description));
					if (recommendation != null)
						v.setRecommendation(decodeAndSanitize(recommendation));
					if (details != null)
						v.setDetails(decodeAndSanitize(details));

					if (a.getVulns() == null) {
						List<Vulnerability> vs = new ArrayList<Vulnerability>();
						vs.add(v);
						a.setVulns(vs);
					} else {
						a.getVulns().add(v);
					}

					em.persist(a);
					HibHelper.getInstance().commit();

					// Return the vulnerability ID of the newly created Vuln.
					String returnMsg = String.format(Support.SUCCESSMSG, "\"vid\":" + v.getId());
					return Response.status(200).entity(returnMsg).build();
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
		return Response.status(400).entity(String.format(Support.ERROR, "Unknown Error")).build();

	}

	@POST
	@ApiOperation(value = "Add a new vulnerability to the assessment based on the internal default vulnerabilities.", notes = "Auto populates the Description and Recommendations", response = Assessment.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Bad Request."),
			@ApiResponse(code = 200, message = "Post Successful.") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/addDefaultVuln/{aid}/{default_vuln_id}")
	public Response addNewVuln(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
			@ApiParam(value = "Vulnerability Name", required = true) @FormParam("name") String name,
			@ApiParam(value = "Exploit Details Information", required = false) @FormParam("details") String details,
			@ApiParam(value = "Severity ID 0-9", required = true) @FormParam("severity") Long severity,
			@ApiParam(value = "Default Vulnerability ID", required = true) @PathParam("default_vuln_id") Long defaultVulnId) {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (severity == null)
				severity = 0l;
			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {
				String query = "{ \"assessor\" : " + u.getId() + ", \"_id\" : " + aid
						+ ",   \"completed\" : {$exists: false} }";
				Assessment a = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList().stream()
						.findFirst().orElse(null);
				if (a == null) {
					return Response.status(400).entity(String.format(Support.ERROR, "Assessment Not Found")).build();
				}

				// Check if we are in Peer review
				/// Assessment should be locked and not editable when PR is enabled.
				PeerReview prTemp = null;
				try {
					prTemp = (PeerReview) em
							.createNativeQuery("{\"assessment_id\" : " + a.getId() + "}", PeerReview.class)
							.getResultList().stream().findFirst().orElse(null);

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				Boolean prSubmitted = false;
				Boolean prComplete = false;
				if (prTemp != null) {
					prSubmitted = true;

					if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
						prComplete = true;
					} else
						prComplete = false;
				} else
					prSubmitted = false;

				if (!(!prSubmitted || prComplete)) {
					return Response.status(400)
							.entity(String.format(Support.ERROR, "Assessment Locked for Peer Review")).build();
				}

				if (name != null && details != null) {
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					DefaultVulnerability dv = (DefaultVulnerability) em
							.createQuery("from DefaultVulnerability where id = :id").setParameter("id", defaultVulnId)
							.getResultList().stream().findFirst().orElse(null);
					Vulnerability v = new Vulnerability();
					v.setDefaultVuln(dv);
					v.setDescription(dv.getDescription());
					v.setRecommendation(dv.getRecommendation());
					v.setCategory(dv.getCategory());
					v.setName(name);
					v.setLikelyhood((long) dv.getLikelyhood());
					v.setImpact((long) dv.getImpact());
					v.setOverall(severity);
					v.setAssessmentId(a.getId());
					v.setDetails(decodeAndSanitize(details));

					if (a.getVulns() == null) {
						List<Vulnerability> vs = new ArrayList<Vulnerability>();
						vs.add(v);
						a.setVulns(vs);
					} else {
						a.getVulns().add(v);
					}

					em.persist(a);
					HibHelper.getInstance().commit();

					// em.close();
					return Response.status(200).entity(Support.SUCCESS).build();
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
		return Response.status(400).entity(Support.ERROR).build();

	}

	/*
	 * addVuln adds exploit steps to an existing vulnerability.
	 */
	@POST
	@ApiOperation(value = "Add Exploit Details to an existing vulnerability.", notes = "Vulnerability must already exist.", response = Assessment.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Bad Request."),
			@ApiResponse(code = 200, message = "Post Successful.") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/addVuln/{aid}/{vid}")
	public Response addVuln(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
			@ApiParam(value = "Vulnerability ID", required = true) @PathParam("vid") Long vid,
			@ApiParam(value = "Exploit Detail Information", required = true) @FormParam("details") String details) {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);

			if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {
				String query = "{ \"assessor\" : " + u.getId() + ", \"_id\" : " + aid
						+ ",  \"completed\" : {$exists: false}}";
				Assessment a = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList().stream()
						.findFirst().orElse(null);
				if (a == null) {
					// em.close();
					return Response.status(400).entity(String.format(Support.ERROR, "Assessment Not Found")).build();
				}

				// Check if we are in Peer review
				/// Assessment should be locked and not editable when PR is enabled.
				PeerReview prTemp = null;
				try {
					prTemp = (PeerReview) em
							.createNativeQuery("{\"assessment_id\" : " + a.getId() + "}", PeerReview.class)
							.getResultList().stream().findFirst().orElse(null);

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
				Boolean prSubmitted = false;
				Boolean prComplete = false;
				if (prTemp != null) {
					prSubmitted = true;

					if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
						prComplete = true;
					} else
						prComplete = false;
				} else
					prSubmitted = false;

				if (!(!prSubmitted || prComplete)) {
					return Response.status(400)
							.entity(String.format(Support.ERROR, "Assessment Locked for Peer Review")).build();
				}

				if (details != null) {
					for (Vulnerability v : a.getVulns()) {
						if (v.getId() == vid) {
							HibHelper.getInstance().preJoin();
							em.joinTransaction();
							String previousDetails = v.getDetails();
							previousDetails = previousDetails == null ? "" : previousDetails;
							v.setDetails(previousDetails + "<br/>" + decodeAndSanitize(details));
							em.persist(v);
							HibHelper.getInstance().commit();
							return Response.status(200).entity(Support.SUCCESS).build();

						}
					}

				}
			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();

			}
		} finally {
			em.close();
		}
		return Response.status(400).entity(Support.ERROR).build();

	}

	@POST
	@ApiOperation(value = "Creates an Assessment Record and schedules it to an assessor or assessors.", notes = "It can auto create user's and teams if they are not in the system. If a User's email address is not in the system it will create the account and send an email notification to the user to register with Faction.")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Bad Request."),
			@ApiResponse(code = 200, message = "Post Successful.") })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create")
	public Response createAssessment(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Application ID", required = false) @FormParam("appid") String appid,
			@ApiParam(value = "Assessment Start Date", required = true) @FormParam("start") DateParam start,
			@ApiParam(value = "Assessment End Date", required = true) @FormParam("end") DateParam end,
			@ApiParam(value = "Semicolon Delimted list of assessor user names", required = true) @FormParam("assessors") String assessor_names,
			@ApiParam(value = "Semicolon Delimted distriburtion list", required = true) @FormParam("distro") String distro,
			@ApiParam(value = "Engagement User Name", required = true) @FormParam("engagement_username") String engagement_username,
			@ApiParam(value = "Remediation User Name", required = true) @FormParam("remediation_username") String remediation_username,
			@ApiParam(value = "Application Name", required = true) @FormParam("appName") String appName,
			@ApiParam(value = "Assessment Type", required = true) @FormParam("type") String type,
			@ApiParam(value = "Scope and credentials for the assessors", required = false) @FormParam("scope") String scope,
			@ApiParam(value = "Assessment Campaign Name", required = true) @FormParam("campaign") String campaign,
			// @ApiParam(value = "Option to auto add users if the email does not exist",
			// required = true) @FormParam("auto_create_users") Boolean autoCreateUsers,
			@ApiParam(value = "Option to auto create a campaign if it does not exist", required = true) @FormParam("auto_create_campaigns") Boolean autoCreateCamp) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		JSONArray jarray = new JSONArray();
		try {
			User u = Support.getUser(em, apiKey);
			if (u != null && u.getPermissions().isEngagement() || u.getPermissions().isManager()) {
				Assessment asmt = new Assessment();
				if (appid != null) {
					List<Assessment> asmts = em.createQuery("from Assessment where appId = :id")
							.setParameter("id", appid).getResultList();
					if (asmts != null && asmts.size() != 0) {
						asmt.setAppId(appid);
						asmt.setName(asmts.get(0).getName());
					} else {
						asmt.setName(appName);
						asmt.setAppId(appid);
					}
				} else if (appName != null && !appName.trim().equals("")) {
					List<Assessment> asmts = em.createQuery("from Assessment where appName = :name")
							.setParameter("name", appName).getResultList();
					if (asmts != null) {
						asmt.setName(appName);
						asmt.setAppId(asmts.get(0).getAppId());
					} else {
						asmt.setName(appName);
						Random r = new Random();
						int rand = r.nextInt((1000 - 500000) + 1) + 1000;
						asmt.setAppId("" + rand);
					}
				} else {
					return Support.error("Application ID or Application name is missing.");

				}

				asmt.setStart(start.getDate());
				asmt.setEnd(end.getDate());
				asmt.setNotes(scope);
				List<User> assessors = new ArrayList<User>();
				for (String e : assessor_names.split(";")) {

					User tmp = (User) em.createQuery("from User where username = :username").setParameter("username", e)
							.getResultList().stream().findFirst().orElse(null);

					if (tmp != null) {
						assessors.add(tmp);
					} else {
						return Support.error("Assessor User Name not found [" + assessor_names + "]");
					}

				}
				if (assessors.size() == 0)
					return Support.error("Assessors User Names not found");

				asmt.setAssessor(assessors);

				User engagement = (User) em.createQuery("from User where username = :email")
						.setParameter("email", engagement_username).getResultList().stream().findFirst().orElse(null);
				if (engagement != null) {
					asmt.setEngagement(engagement);
				} else {
					return Support.error("Engagment User Name not found");
				}

				User remediation = (User) em.createQuery("from User where username = :email")
						.setParameter("email", remediation_username).getResultList().stream().findFirst().orElse(null);
				if (remediation != null) {
					asmt.setRemediation(remediation);
				} else {
					return Support.error("Remediation User Name not found");
				}
				AssessmentType aType = (AssessmentType) em.createQuery("from AssessmentType where type = :type")
						.setParameter("type", type).getResultList().stream().findFirst().orElse(null);
				if (aType != null)
					asmt.setType(aType);
				else
					return Support.error("Assessment Type not found");

				asmt.setDistributionList(distro);
				if (campaign != null) {
					Campaign camp = (Campaign) em.createQuery("from Campaign where name = :name")
							.setParameter("name", campaign).getResultList().stream().findFirst().orElse(null);
					if (camp != null)
						asmt.setCampaign(camp);
					else if (autoCreateCamp) {
						camp = new Campaign();
						camp.setName(campaign);
						em.persist(camp);
						asmt.setCampaign(camp);
					}
				}
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(asmt);
				HibHelper.getInstance().commit();
				return Support.success();
			} else {
				return Support.autherror();
			}
		} finally {
			em.close();
		}

	}


	private String decodeAndSanitize(String encoded) {
		String decoded = "";
		try {
			decoded = new String(Base64.decodeBase64(encoded));
			decoded = FSUtils.convertFromMarkDown(decoded);
			return FSUtils.sanitizeHTML(decoded);
		} catch (Exception e) {
			e.printStackTrace();
			return "";

		}
	}

}
