package com.fuse.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;

import com.faction.reporting.ReportFeatures;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.AssessmentDTO;
import com.fuse.api.dto.CustomTypeDTO;
import com.fuse.api.dto.VulnerabilityDTO;
import com.fuse.api.util.DateParam;
import com.fuse.api.util.Support;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.utils.FSUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/assessments")
@Path("/assessments")
public class assessments {
    @GET
    @ApiOperation(value = "Gets the the Assessment Queue for the user associated with the FACTION-API-KEY token header.", notes = "Gets the the Assessment Queue for the user associated with the FACTION-API-KEY token header.", response = Assessment.class, responseContainer = "List", position = 100)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 200, message = "Assessor Queue Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/queue")
    public Response getAssessments(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<AssessmentDTO> dtos = new ArrayList<>();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {
                try {
                    List<Assessment> asmts = AssessmentQueries.getAssessmentsByUserId(em, u, u.getId(),
                            AssessmentQueries.OnlyNonCompleted);
                    for (Assessment a : asmts) {
                        AssessmentQueries.updateImages(a);
                        AssessmentDTO dto = AssessmentDTO.fromEntity(a);

                        // Add custom fields if any
                        if (a.getCustomFields() != null) {
                            dto.setCustomFieldsFromEntity(a.getCustomFields());
                        }

                        dtos.add(dto);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Response.status(500).entity(String.format(Support.ERROR, "Error retrieving assessments"))
                            .build();
                }

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }

        // Serialize DTOs using Jackson
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dtos);
            return Response.status(200).entity(json).build();
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        }
    }

    /*
     * getAssessment - get assessment details by ID
     */
    @GET
    @ApiOperation(value = "Gets assessment details by ID", notes = "Returns full assessment information including custom fields", response = AssessmentDTO.class, position = 140)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Assessment Does not exist."),
            @ApiResponse(code = 200, message = "Returns assessment details") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{aid}")
    public Response getAssessment(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager()
                    || u.getPermissions().isEngagement())) {

                // Get the assessment
                Assessment assessment = em.find(Assessment.class, aid);
                if (assessment == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Assessment Does Not Exist."))
                            .build();
                }

                // Check permissions - user should be assessor, manager, or
                // engagement/remediation contact
                boolean hasAccess = u.getPermissions().isManager();

                if (!hasAccess && assessment.getAssessor() != null) {
                    for (User assessor : assessment.getAssessor()) {
                        if (assessor.getId() == u.getId()) {
                            hasAccess = true;
                            break;
                        }
                    }
                }

                if (!hasAccess && assessment.getEngagement() != null
                        && assessment.getEngagement().getId() == u.getId()) {
                    hasAccess = true;
                }

                if (!hasAccess && assessment.getRemediation() != null
                        && assessment.getRemediation().getId() == u.getId()) {
                    hasAccess = true;
                }

                if (!hasAccess) {
                    return Response.status(401)
                            .entity(String.format(Support.ERROR, "Not Authorized for this Assessment.")).build();
                }

                // Convert to DTO
                AssessmentQueries.updateImages(assessment);
                AssessmentDTO dto = AssessmentDTO.fromEntity(assessment);

                // Add custom fields from the assessment entity
                dto.setCustomFieldsFromEntity(assessment.getCustomFields());

                // Serialize using Jackson
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(dto);
                return Response.status(200).entity(json).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    /*
     * updateAssessment - update assessment fields
     */
    @POST
    @ApiOperation(value = "Update assessment fields", notes = "Updates allowed assessment fields including custom fields", response = String.class, position = 150)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Update Successful.") })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{aid}")
    public Response updateAssessment(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
            @ApiParam(value = "Assessment Notes", required = false) @FormParam("notes") String notes,
            @ApiParam(value = "Assessment Summary", required = false) @FormParam("summary") String summary,
            @ApiParam(value = "Distribution List", required = false) @FormParam("distributionList") String distributionList,
            @ApiParam(value = "Custom Fields (JSON object with key-value pairs)", required = false) @FormParam("customFields") String customFieldsJson) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager()
                    || u.getPermissions().isEngagement())) {

                // Get the assessment
                Assessment assessment = em.find(Assessment.class, aid);
                if (assessment == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Assessment Does Not Exist."))
                            .build();
                }

                // Check permissions - user should be assessor, manager, or engagement contact
                boolean hasAccess = u.getPermissions().isManager();

                if (!hasAccess && assessment.getAssessor() != null) {
                    for (User assessor : assessment.getAssessor()) {
                        if (assessor.getId() == u.getId()) {
                            hasAccess = true;
                            break;
                        }
                    }
                }

                if (!hasAccess && assessment.getEngagement() != null
                        && assessment.getEngagement().getId() == u.getId()) {
                    hasAccess = true;
                }

                if (!hasAccess) {
                    return Response.status(401)
                            .entity(String.format(Support.ERROR, "Not Authorized for this Assessment.")).build();
                }

                // Check if in peer review
                PeerReview prTemp = null;
                try {
                    prTemp = (PeerReview) em
                            .createNativeQuery("{\"assessment_id\" : " + assessment.getId() + "}", PeerReview.class)
                            .getResultList().stream().findFirst().orElse(null);
                } catch (Exception ex) {
                    // ignore
                }

                Boolean prSubmitted = false;
                Boolean prComplete = false;
                if (prTemp != null) {
                    prSubmitted = true;
                    if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
                        prComplete = true;
                    } else {
                        prComplete = false;
                    }
                } else {
                    prSubmitted = false;
                }

                if (!(!prSubmitted || prComplete)) {
                    return Response.status(400)
                            .entity(String.format(Support.ERROR, "Assessment Locked for Peer Review")).build();
                }

                // Update fields
                HibHelper.getInstance().preJoin();
                em.joinTransaction();

                boolean updated = false;

                if (notes != null) {
                    assessment.setNotes(notes);
                    updated = true;
                }

                if (summary != null) {
                    assessment.setSummary(summary);
                    updated = true;
                }

                if (distributionList != null) {
                    assessment.setDistributionList(distributionList);
                    updated = true;
                }

                // Parse and update custom fields if provided
                if (customFieldsJson != null && !customFieldsJson.trim().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, String> customFieldsMap = mapper.readValue(customFieldsJson,
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                                });

                        // Get allowed custom types for assessments for this assessment type
                        List<CustomType> assessmentCustomTypes = em.createQuery(
                                "from CustomType where type = :type and deleted = false", CustomType.class)
                                .setParameter("type", CustomType.ObjType.ASMT.getValue())
                                .getResultList();

                        // Get existing custom fields
                        List<CustomField> existingFields = assessment.getCustomFields();
                        if (existingFields == null) {
                            existingFields = new ArrayList<>();
                            assessment.setCustomFields(existingFields);
                        }

                        // Create maps for easier lookup
                        Map<String, CustomField> existingFieldsMap = new HashMap<>();
                        for (CustomField cf : existingFields) {
                            if (cf.getType() != null) {
                                existingFieldsMap.put(cf.getType().getKey(), cf);
                            }
                        }

                        // Update or add custom fields
                        for (Map.Entry<String, String> entry : customFieldsMap.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();

                            // Find the matching custom type
                            CustomType matchingType = null;
                            for (CustomType ct : assessmentCustomTypes) {
                                if (ct.getKey().equals(key) &&
                                        (ct.getAssessmentTypes().isEmpty()
                                                || ct.getAssessmentTypes().contains(assessment.getType()))) {
                                    matchingType = ct;
                                    break;
                                }
                            }

                            if (matchingType != null) {
                                // Check if field already exists
                                CustomField existingField = existingFieldsMap.get(key);

                                if (existingField != null) {
                                    // Update existing field
                                    existingField.setValue(value);
                                    em.persist(existingField);
                                } else {
                                    // Create new field
                                    CustomField cf = new CustomField();
                                    cf.setType(matchingType);
                                    cf.setValue(value);
                                    existingFields.add(cf);
                                    em.persist(cf);
                                }
                            }
                        }

                        // Note: We're not removing any fields - only updating/adding
                        // If you want to support deletion, you could add a special value like
                        // "__delete__"
                        // and remove fields with that value

                        updated = true;
                    } catch (Exception e) {
                        return Response.status(400)
                                .entity(String.format(Support.ERROR, "Invalid custom fields format: " + e.getMessage()))
                                .build();
                    }
                }

                if (updated) {
                    em.persist(assessment);
                    HibHelper.getInstance().commit();
                }

                return Response.status(200).entity(Support.SUCCESS).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }
    }

    /*
     * getVulns - get vulns for current assessment
     * 
     */

    @GET
    @ApiOperation(value = "Gets the the Vulnererabilies for the Application.", notes = "Application ID is not the same as Assessment ID. An Application ID can span multiple assessments.", response = Vulnerability.class, responseContainer = "List", position = 130)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Application Does not exist."),
            @ApiResponse(code = 200, message = "Returns json array of vulnerbilities") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/vulns/{aid}")
    public Response getVulns(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<VulnerabilityDTO> dtos = new ArrayList<>();
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
                        v.updateRiskLevels(em);
                        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);

                        // Add any custom fields from the entity
                        dto.setCustomFieldsFromEntity(v.getCustomFields());

                        dtos.add(dto);
                    }
                }

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }

        // Serialize DTOs using Jackson
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dtos);
            return Response.status(200).entity(json).build();
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        }

    }

    /*
     * getHistory - getAssessment History
     * 
     */

    @GET
    @ApiOperation(value = "Gets the the Vulnererabilies for the Application.", notes = "Application ID is not the same as Assessment ID. An Application ID can span multiple assessments.", response = Vulnerability.class, responseContainer = "List", position = 80)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Assessment Does not exist."),
            @ApiResponse(code = 200, message = "Returns json array of vulnerbilities") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/history/{appid}")
    public Response getHistory(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Application ID Header", required = true) @PathParam("appid") String appid) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<VulnerabilityDTO> dtos = new ArrayList<>();
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
                        VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);

                        // Add any custom fields from the entity
                        dto.setCustomFieldsFromEntity(v.getCustomFields());

                        dtos.add(dto);
                    }
                }

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }

        // Serialize DTOs using Jackson
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dtos);
            return Response.status(200).entity(json).build();
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        }

    }

    /*
     * updateNotes - Add notes to an existing assessment
     * 
     */

    @POST
    @ApiOperation(value = "Update the Assessment Notes.", notes = "Application ID is not the same as Assessment ID. An Application ID can span multiple assessments.", position = 90)
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
    @ApiOperation(value = "Gets details for a specific vulnerability.", notes = "Pulls add details and exploits steps for a vulnerability id.", response = Vulnerability.class, responseContainer = "List", position = 110)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Returns Vulnerability and Exploit Step Information") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/vuln/{vid}")
    public Response getVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability ID", required = true) @PathParam("vid") Long vid) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<VulnerabilityDTO> dtos = new ArrayList<>();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

                Vulnerability v = (Vulnerability) em.createQuery("from Vulnerability where id = :id")
                        .setParameter("id", vid).getResultList().stream().findFirst().orElse(null);

                if (v == null)
                    return Response.status(400).entity(String.format(Support.ERROR, "Vulnerability Does Not Exist."))
                            .build();

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
                } else {
                    // v.setDetails(v.getDetails().replaceAll("\n", "\r\n"));
                    v.setDetails(v.getDetails());
                }

                AssessmentQueries.updateImages(em, v);
                v.updateRiskLevels(em);
                VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);

                // Add any custom fields from the entity
                dto.setCustomFieldsFromEntity(v.getCustomFields());

                dtos.add(dto);

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }

        // Serialize DTOs using Jackson
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dtos);
            return Response.status(200).entity(json).build();
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        }

    }

    /*
     * addNewVuln - Creates a new vulnerability for the the specified assessment.
     */

    @POST
    @ApiOperation(value = "Add a new vulnerability to the assessment. All Base64 encoded inputs supoort HTML and Markdown syntax", notes = "Suports HTML and MarkDown Syntax", response = Assessment.class, responseContainer = "List", position = 30)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Returns the ID of the newly created vulnerability.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/addVuln/{aid}")
    public Response addNewVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
            @ApiParam(value = "Vulnerability Name", required = true) @FormParam("name") String name,
            @ApiParam(value = "Vulnerability Template ID", required = false) @FormParam("vulnTemplateId") Long defaultVulnId,
            @ApiParam(value = "Vulnerability Description (Base64 Encoded)", required = false) @FormParam("description") String description,
            @ApiParam(value = "Vulnerability Recommendation (Base64 Encoded)", required = false) @FormParam("recommendation") String recommendation,
            @ApiParam(value = "Exploit Details Information (Base64 Encoded)", required = false) @FormParam("details") String details,
            @ApiParam(value = "Vulnerability Category", required = false) @FormParam("categoryId") Long categoryId,
            @ApiParam(value = "Severity ID 0-9", required = false) @FormParam("severity") Long severity,
            @ApiParam(value = "Impact (0-9)", required = false) @FormParam("impact") Long impact,
            @ApiParam(value = "Likelihood (0-9)", required = false) @FormParam("likelihood") Long likelihood,
            @ApiParam(value = "CVSS Score", required = false) @FormParam("cvssScore") String cvssScore,
            @ApiParam(value = "CVSS String", required = false) @FormParam("cvssString") String cvssString,
            @ApiParam(value = "Section (Enterprise Feature)", required = false) @FormParam("section") String section,
            @ApiParam(value = "Custom Fields (JSON object with key-value pairs)", required = false) @FormParam("customFields") String customFieldsJson) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);

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

                HibHelper.getInstance().preJoin();
                em.joinTransaction();
                Category category = em.createQuery("from Category where name = 'Uncategorized'", Category.class)
                        .getResultList().stream().findFirst().orElse(null);

                if (categoryId != null) {
                    category = em.createQuery("from Category where id = :id", Category.class)
                            .setParameter("id", categoryId)
                            .getResultList().stream().findFirst().orElse(null);
                    if (category == null) {
                        return Response.status(400)
                                .entity(String.format(Support.ERROR, "Invalid Category Id")).build();
                    }

                }
                Vulnerability v = new Vulnerability();
                DefaultVulnerability dv = null;

                // Set Up Vuln with a Vulnerability Template
                if (defaultVulnId != null) {
                    dv = (DefaultVulnerability) em
                            .createQuery("from DefaultVulnerability where id = :id")
                            .setParameter("id", defaultVulnId).getResultList().stream().findFirst().orElse(null);
                    if (dv == null) {
                        return Response.status(400)
                                .entity(String.format(Support.ERROR, "Invalid Vulnerability Template Id")).build();
                    }
                    v.setName(dv.getName());
                    v.setDefaultVuln(dv);
                    v.setDescription(dv.getDescription());
                    v.setRecommendation(dv.getRecommendation());
                    v.setCategory(dv.getCategory());
                    v.setOverall((long) dv.getOverall());
                    v.setImpact((long) dv.getImpact());
                    v.setLikelyhood((long) dv.getLikelyhood());
                    if (a.getType().isCvss31()) {
                        v.setCvssString(dv.getCvss31String());
                        v.setCvssScore(dv.getCvss31Score());
                    } else if (a.getType().isCvss40()) {
                        v.setCvssString(dv.getCvss40String());
                        v.setCvssScore(dv.getCvss40Score());
                    }
                } else {
                    // Don't use a vulnerability template and just add a generic vulnerability
                    dv = em.createQuery("from DefaultVulnerability where name = 'Generic Vulnerability'",
                            DefaultVulnerability.class).getResultList().stream().findFirst().orElse(null);
                    Category cat = em.createQuery("from Category where name = 'Uncategorized'", Category.class)
                            .getResultList().stream().findFirst().orElse(null);
                    if (cat == null) {
                        cat = new Category();
                        cat.setName("Uncategorized");
                        HibHelper.getInstance().preJoin();
                        em.joinTransaction();
                        em.persist(cat);
                        em.persist(dv);
                        HibHelper.getInstance().commit();
                    }
                    if (dv == null) {
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

                if (name != null)
                    v.setName(name);

                if (v.getName() == null || v.getName() == "") {
                    return Response.status(400)
                            .entity(String.format(Support.ERROR, "Vulnerability Name Cannot Be Blank")).build();
                }

                if (severity != null)
                    v.setOverall(severity);
                // If impact is not explicitly provided, use severity
                if (impact == null) {
                    v.setImpact(v.getOverall());
                } else {
                    v.setImpact(impact);
                }
                // If likelihood is not explicitly provided, use severity
                if (likelihood == null) {
                    v.setLikelyhood(v.getOverall());
                } else {
                    v.setLikelyhood(likelihood);
                }
                v.setAssessmentId(a.getId());
                if (description != null)
                    v.setDescription(decodeAndSanitize(description));
                if (recommendation != null)
                    v.setRecommendation(decodeAndSanitize(recommendation));
                if (details != null)
                    v.setDetails(decodeAndSanitize(details));

                if (cvssScore != null && !cvssScore.trim().isEmpty()) {
                    v.setCvssScore(cvssScore);
                }

                if (cvssString != null && !cvssString.trim().isEmpty()) {
                    v.setCvssString(cvssString);
                }

                // Check that overall got set either manually or by the cvss score
                if (v.getOverall() == null) {
                    return Response.status(400)
                            .entity(String.format(Support.ERROR, "Severity Cannot Be Blank")).build();
                }
                if (section != null) {
                    if (ReportFeatures.allowSections()) {
                        if (VulnerabilityQueries.isValidSection(em, section))
                            v.setSection(section);
                        else {
                            return Response.status(400)
                                    .entity(String.format(Support.ERROR, "Not a Valid Section")).build();

                        }
                    }
                }

                // Parse user-provided custom field values if any
                Map<String, String> userProvidedValues = null;
                if (customFieldsJson != null && !customFieldsJson.trim().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        userProvidedValues = mapper.readValue(customFieldsJson,
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                                });
                    } catch (Exception e) {
                        // Log error but continue with empty user values
                        e.printStackTrace();
                    }
                }

                // Use helper to handle custom fields - create all fields with defaults
                handleVulnerabilityCustomFields(em, v, a.getType(), userProvidedValues, true);

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

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }

    }

    @POST
    @ApiOperation(value = "This API adds a new vulnerability to the assessment based on the internal database vulnerability templates.", notes = "Auto populates all non-required fields based on the vulnerability template database", response = Assessment.class, responseContainer = "List", position = 20)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Post Successful.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/addDefaultVuln/{aid}/{id}")
    public Response addTemplatedVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid,
            @ApiParam(value = "Vulnerability Name", required = true) @FormParam("name") String name,
            @ApiParam(value = "Exploit Details Information", required = false) @FormParam("details") String details,
            @ApiParam(value = "Severity ID 0-9", required = false) @FormParam("severity") Long severity,
            @ApiParam(value = "Impact (0-9)", required = false) @FormParam("impact") Long impact,
            @ApiParam(value = "Likelihood (0-9)", required = false) @FormParam("likelihood") Long likelihood,
            @ApiParam(value = "CVSS Score", required = false) @FormParam("cvssScore") String cvssScore,
            @ApiParam(value = "CVSS String", required = false) @FormParam("cvssString") String cvssString,
            @ApiParam(value = "Section (Enterprise Feature)", required = false) @FormParam("section") String section,
            @ApiParam(value = "Custom Fields (JSON object with key-value pairs)", required = false) @FormParam("customFields") String customFieldsJson,
            @ApiParam(value = "Vulnerability Template ID", required = true) @PathParam("id") Long defaultVulnId) {
        return this.addNewVuln(apiKey, aid, name, defaultVulnId, null, null, details, null, severity, impact, likelihood,
                cvssScore, cvssString, section, customFieldsJson);

    }

    /*
     * addVuln adds exploit steps to an existing vulnerability.
     */
    @POST
    @ApiOperation(value = "Add Exploit Details to an existing vulnerability.", notes = "Vulnerability must already exist.", response = Assessment.class, responseContainer = "List", position = 40)
    @Deprecated
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
    @ApiOperation(value = "Creates an Assessment Record and schedules it to an assessor or assessors.", notes = "It can auto create user's and teams if they are not in the system. If a User's email address is not in the system it will create the account and send an email notification to the user to register with Faction.", position = 70)
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

    @POST
    @ApiOperation(value = "Gets All Completed Assessments by Date", notes = "Gets all assessments by Date range", response = Assessment.class, responseContainer = "List", position = 50)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Returns Vulnerability and Exploit Step Information") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/completed")
    public Response getCompletedAssessments(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Start Date (DD/MM/YYYY)", required = true) @FormParam("start") String start,
            @ApiParam(value = "End Date (DD/MM/YYYY)", required = false) @FormParam("end") String end) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<AssessmentDTO> dtos = new ArrayList<>();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isManager())) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date startDate = null;
                Date endDate = null;

                try {
                    startDate = sdf.parse(start);
                    if (end != null) {
                        endDate = sdf.parse(end);
                    }
                } catch (Exception ex) {
                    return Response.status(500).entity(String.format(Support.ERROR, "Invalid Date Format")).build();
                }

                List<Assessment> completed = AssessmentQueries.getAllCompletedAssessmentsByDateRange(em, u, startDate,
                        endDate);
                for (Assessment a : completed) {
                    AssessmentQueries.updateImages(a);
                    AssessmentDTO dto = AssessmentDTO.fromEntity(a);

                    // Add custom fields if any
                    if (a.getCustomFields() != null) {
                        dto.setCustomFieldsFromEntity(a.getCustomFields());
                    }

                    dtos.add(dto);
                }

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(dtos);
                return Response.status(200).entity(json).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, e.getMessage())).build();
        } finally {
            em.close();
        }

    }

    /*
     * getCustomFieldTypes - Get allowed custom field types for an assessment
     */
    @GET
    @ApiOperation(value = "Gets the allowed custom field types for an assessment", notes = "Returns both Assessment and Vulnerability custom field types that are allowed based on the assessment type", response = String.class, position = 60)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Assessment Does not exist."),
            @ApiResponse(code = 200, message = "Returns custom field types") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/customfields/{aid}")
    public Response getCustomFieldTypes(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Assessment ID", required = true) @PathParam("aid") Long aid) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

                // Get the assessment
                Assessment assessment = em.find(Assessment.class, aid);
                if (assessment == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Assessment Does Not Exist."))
                            .build();
                }

                // Get assessment type
                AssessmentType assessmentType = assessment.getType();

                // Get assessment-specific custom types
                List<CustomType> assessmentCustomTypes = em.createQuery(
                        "from CustomType where type = :type and deleted = false", CustomType.class)
                        .setParameter("type", CustomType.ObjType.ASMT.getValue())
                        .getResultList();

                // Get vulnerability-specific custom types
                List<CustomType> vulnCustomTypes = em.createQuery(
                        "from CustomType where type = :type and deleted = false", CustomType.class)
                        .setParameter("type", CustomType.ObjType.VULN.getValue())
                        .getResultList();

                // Filter by assessment type if configured
                List<CustomTypeDTO> assessmentFields = new ArrayList<>();
                List<CustomTypeDTO> vulnerabilityFields = new ArrayList<>();

                for (CustomType ct : assessmentCustomTypes) {
                    if (ct.getAssessmentTypes().isEmpty() || ct.getAssessmentTypes().contains(assessmentType)) {
                        assessmentFields.add(CustomTypeDTO.fromEntity(ct));
                    }
                }

                for (CustomType ct : vulnCustomTypes) {
                    if (ct.getAssessmentTypes().isEmpty() || ct.getAssessmentTypes().contains(assessmentType)) {
                        vulnerabilityFields.add(CustomTypeDTO.fromEntity(ct));
                    }
                }

                // Build response
                Map<String, Object> response = new HashMap<>();
                response.put("assessmentFields", assessmentFields);
                response.put("vulnerabilityFields", vulnerabilityFields);

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(response);
                return Response.status(200).entity(json).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    /*
     * updateVulnCustomFields - Update custom fields for an existing vulnerability
     */
    @POST
    @ApiOperation(value = "Update custom fields for an existing vulnerability.", notes = "Updates only the custom fields for a vulnerability. Custom field keys must match allowed types for the assessment.", response = String.class, position = 125)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Update Successful.") })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/vuln/{vid}/customfields")
    public Response updateVulnCustomFields(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability ID", required = true) @PathParam("vid") Long vid,
            @ApiParam(value = "Custom Fields (JSON object with key-value pairs)", required = true) @FormParam("customFields") String customFieldsJson) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

                // Get the vulnerability
                Vulnerability vuln = em.find(Vulnerability.class, vid);
                if (vuln == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Vulnerability Does Not Exist."))
                            .build();
                }

                // Get the assessment to check permissions
                Assessment assessment = em.find(Assessment.class, vuln.getAssessmentId());
                if (assessment == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Assessment Not Found.")).build();
                }

                // Check if user is an assessor for this assessment
                boolean isAssessor = false;
                for (User assessor : assessment.getAssessor()) {
                    if (assessor.getId() == u.getId()) {
                        isAssessor = true;
                        break;
                    }
                }

                if (!isAssessor && !u.getPermissions().isManager()) {
                    return Response.status(401)
                            .entity(String.format(Support.ERROR, "Not Authorized for this Assessment.")).build();
                }

                // Check if in peer review
                PeerReview prTemp = null;
                try {
                    prTemp = (PeerReview) em
                            .createNativeQuery("{\"assessment_id\" : " + assessment.getId() + "}", PeerReview.class)
                            .getResultList().stream().findFirst().orElse(null);
                } catch (Exception ex) {
                    // ignore
                }

                Boolean prSubmitted = false;
                Boolean prComplete = false;
                if (prTemp != null) {
                    prSubmitted = true;
                    if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
                        prComplete = true;
                    } else {
                        prComplete = false;
                    }
                } else {
                    prSubmitted = false;
                }

                if (!(!prSubmitted || prComplete)) {
                    return Response.status(400)
                            .entity(String.format(Support.ERROR, "Assessment Locked for Peer Review")).build();
                }

                // Parse custom fields
                try {
                    HibHelper.getInstance().preJoin();
                    em.joinTransaction();

                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> customFieldsMap = mapper.readValue(customFieldsJson,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                            });

                    // Get allowed custom types for vulnerabilities for this assessment type
                    List<CustomType> vulnCustomTypes = em.createQuery(
                            "from CustomType where type = :type and deleted = false", CustomType.class)
                            .setParameter("type", CustomType.ObjType.VULN.getValue())
                            .getResultList();

                    // Remove existing custom fields
                    if (vuln.getCustomFields() != null) {
                        for (CustomField cf : vuln.getCustomFields()) {
                            em.remove(cf);
                        }
                    }

                    // Create new custom fields
                    List<CustomField> customFields = new ArrayList<>();
                    for (Map.Entry<String, String> entry : customFieldsMap.entrySet()) {
                        // Find the matching custom type
                        CustomType matchingType = null;
                        for (CustomType ct : vulnCustomTypes) {
                            if (ct.getKey().equals(entry.getKey()) &&
                                    (ct.getAssessmentTypes().isEmpty()
                                            || ct.getAssessmentTypes().contains(assessment.getType()))) {
                                matchingType = ct;
                                break;
                            }
                        }

                        if (matchingType != null) {
                            CustomField cf = new CustomField();
                            cf.setType(matchingType);
                            cf.setValue(entry.getValue());
                            customFields.add(cf);
                            em.persist(cf);
                        }
                    }

                    vuln.setCustomFields(customFields);
                    em.persist(vuln);
                    HibHelper.getInstance().commit();

                    return Response.status(200).entity(Support.SUCCESS).build();

                } catch (Exception e) {
                    return Response.status(400)
                            .entity(String.format(Support.ERROR, "Invalid custom fields format: " + e.getMessage()))
                            .build();
                }

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }
    }

    /*
     * updateVulnerability - Update a vulnerability including custom fields
     */
    @POST
    @ApiOperation(value = "Update vulnerability ", notes = "Updates a vulnerability including custom fields", response = String.class, position = 120)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request."),
            @ApiResponse(code = 200, message = "Update Successful.") })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/vuln/{vid}")
    public Response updateVulnerability(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability ID", required = true) @PathParam("vid") Long vid,
            @ApiParam(value = "Vulnerability Name", required = false) @FormParam("name") String name,
            @ApiParam(value = "Description (Base64 Encoded)", required = false) @FormParam("description") String description,
            @ApiParam(value = "Recommendation (Base64 Encoded)", required = false) @FormParam("recommendation") String recommendation,
            @ApiParam(value = "Details (Base64 Encoded)", required = false) @FormParam("details") String details,
            @ApiParam(value = "Severity (0-9)", required = false) @FormParam("severity") Long severity,
            @ApiParam(value = "Impact (0-9)", required = false) @FormParam("impact") Long impact,
            @ApiParam(value = "Likelihood (0-9)", required = false) @FormParam("likelihood") Long likelihood,
            @ApiParam(value = "CVSS Score", required = false) @FormParam("cvssScore") String cvssScore,
            @ApiParam(value = "CVSS String", required = false) @FormParam("cvssString") String cvssString,
            @ApiParam(value = "Vulnerability Category", required = false) @FormParam("categoryId") Long categoryId,
            @ApiParam(value = "Section (Enterprise Feature)", required = false) @FormParam("section") String section,
            @ApiParam(value = "Custom Fields (JSON object with key-value pairs)", required = false) @FormParam("customFields") String customFieldsJson) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager())) {

                // Get the vulnerability
                Vulnerability vuln = em.find(Vulnerability.class, vid);
                if (vuln == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Vulnerability Does Not Exist."))
                            .build();
                }

                // Get the assessment to check permissions
                Assessment assessment = em.find(Assessment.class, vuln.getAssessmentId());
                if (assessment == null) {
                    return Response.status(400).entity(String.format(Support.ERROR, "Assessment Not Found.")).build();
                }

                // Check if user is an assessor for this assessment or a manager
                boolean hasAccess = u.getPermissions().isManager();

                if (!hasAccess && assessment.getAssessor() != null) {
                    for (User assessor : assessment.getAssessor()) {
                        if (assessor.getId() == u.getId()) {
                            hasAccess = true;
                            break;
                        }
                    }
                }

                if (!hasAccess) {
                    return Response.status(401)
                            .entity(String.format(Support.ERROR, "Not Authorized for this Assessment.")).build();
                }

                // Check if in peer review
                PeerReview prTemp = null;
                try {
                    prTemp = (PeerReview) em
                            .createNativeQuery("{\"assessment_id\" : " + assessment.getId() + "}", PeerReview.class)
                            .getResultList().stream().findFirst().orElse(null);
                } catch (Exception ex) {
                    // ignore
                }

                Boolean prSubmitted = false;
                Boolean prComplete = false;
                if (prTemp != null) {
                    prSubmitted = true;
                    if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
                        prComplete = true;
                    } else {
                        prComplete = false;
                    }
                } else {
                    prSubmitted = false;
                }

                if (!(!prSubmitted || prComplete)) {
                    return Response.status(400)
                            .entity(String.format(Support.ERROR, "Assessment Locked for Peer Review")).build();
                }
                Category cat = null;
                if (categoryId != null) {
                    cat = em.find(Category.class, categoryId);
                    if (cat == null) {
                        return Response.status(400)
                                .entity(String.format(Support.ERROR, "Invalid Category Id")).build();
                    }
                }

                // Update fields
                HibHelper.getInstance().preJoin();
                em.joinTransaction();

                boolean updated = false;

                if (name != null && !name.trim().isEmpty()) {
                    vuln.setName(name);
                    updated = true;
                }

                if (description != null) {
                    vuln.setDescription(decodeAndSanitize(description));
                    updated = true;
                }

                if (recommendation != null) {
                    vuln.setRecommendation(decodeAndSanitize(recommendation));
                    updated = true;
                }

                if (details != null) {
                    vuln.setDetails(decodeAndSanitize(details));
                    updated = true;
                }
                if (section != null) {
                    if (ReportFeatures.allowSections()) {
                        if (VulnerabilityQueries.isValidSection(em, section))
                            vuln.setSection(section);
                        else {
                            return Response.status(400)
                                    .entity(String.format(Support.ERROR, "Not a Valid Section")).build();

                        }
                    }
                }

                // If severity is provided, use it as the base
                if (severity != null) {
                    vuln.setOverall(severity);
                    // If impact is not explicitly provided, use severity
                    if (impact == null) {
                        vuln.setImpact(severity);
                    } else {
                        vuln.setImpact(impact);
                    }
                    // If likelihood is not explicitly provided, use severity
                    if (likelihood == null) {
                        vuln.setLikelyhood(severity);
                    } else {
                        vuln.setLikelyhood(likelihood);
                    }
                } else {
                    // If severity is not provided but impact or likelihood are
                    if (impact != null) {
                        vuln.setImpact(impact);
                    }
                    if (likelihood != null) {
                        vuln.setLikelyhood(likelihood);
                    }
                }
                updated = true;

                if (cvssScore != null && !cvssScore.trim().isEmpty()) {
                    vuln.setCvssScore(cvssScore);
                    updated = true;
                }

                if (cvssString != null && !cvssString.trim().isEmpty()) {
                    vuln.setCvssString(cvssString);
                    updated = true;
                }

                // Parse and update custom fields if provided
                if (customFieldsJson != null && !customFieldsJson.trim().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, String> customFieldsMap = mapper.readValue(customFieldsJson,
                                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                                });

                        // Use helper to handle custom fields - partial update
                        handleVulnerabilityCustomFields(em, vuln, assessment.getType(), customFieldsMap, false);

                        updated = true;
                    } catch (Exception e) {
                        return Response.status(400)
                                .entity(String.format(Support.ERROR, "Invalid custom fields format: " + e.getMessage()))
                                .build();
                    }
                }

                if (updated) {
                    em.persist(vuln);
                    HibHelper.getInstance().commit();
                }

                return Response.status(200).entity(Support.SUCCESS).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
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

    /**
     * Helper function to handle custom fields for vulnerabilities
     * 
     * @param em                 EntityManager instance
     * @param vuln               The vulnerability to add custom fields to
     * @param assessmentType     The assessment type to filter custom fields by
     * @param userProvidedValues Map of user-provided custom field values (can be
     *                           null or empty)
     * @param createAllFields    If true, creates all available fields with
     *                           defaults; if false, only updates provided fields
     */
    private void handleVulnerabilityCustomFields(EntityManager em, Vulnerability vuln, AssessmentType assessmentType,
            Map<String, String> userProvidedValues, boolean createAllFields) {

        // Get allowed custom types for vulnerabilities
        List<CustomType> vulnCustomTypes = em.createQuery(
                "from CustomType where type = :type and deleted = false", CustomType.class)
                .setParameter("type", CustomType.ObjType.VULN.getValue())
                .getResultList();

        if (createAllFields) {
            // Create all custom fields with defaults (for new vulnerabilities)
            List<CustomField> customFields = new ArrayList<>();

            for (CustomType ct : vulnCustomTypes) {
                // Check if this custom type applies to the assessment type
                if (ct.getAssessmentTypes().isEmpty() || ct.getAssessmentTypes().contains(assessmentType)) {
                    CustomField cf = new CustomField();
                    cf.setType(ct);

                    // Check if user provided a value for this field
                    if (userProvidedValues != null && userProvidedValues.containsKey(ct.getKey())) {
                        cf.setValue(userProvidedValues.get(ct.getKey()));
                    } else {
                        // Set default value based on field type
                        if (ct.getFieldType() == CustomType.FieldType.BOOLEAN.getValue()) {
                            cf.setValue(ct.getDefaultValue() != null ? ct.getDefaultValue() : "false");
                        } else if (ct.getFieldType() == CustomType.FieldType.LIST.getValue()) {
                            // For list type, use the default value or first option if available
                            String defaultValue = ct.getDefaultValue();
                            if (defaultValue != null && !defaultValue.trim().isEmpty()) {
                                String[] optionArray = defaultValue.split(",");
                                if (optionArray.length > 0) {
                                    cf.setValue(optionArray[0].trim());
                                } else {
                                    cf.setValue("");
                                }
                            } else {
                                cf.setValue("");
                            }
                        } else {
                            // For String type, use default value or empty string
                            cf.setValue(ct.getDefaultValue() != null ? ct.getDefaultValue() : "");
                        }
                    }

                    customFields.add(cf);
                    em.persist(cf);
                }
            }

            vuln.setCustomFields(customFields);
        } else {
            // Partial update - only update provided fields (for existing vulnerabilities)
            List<CustomField> existingFields = vuln.getCustomFields();
            if (existingFields == null) {
                existingFields = new ArrayList<>();
                vuln.setCustomFields(existingFields);
            }

            // Create map for easier lookup
            Map<String, CustomField> existingFieldsMap = new HashMap<>();
            for (CustomField cf : existingFields) {
                if (cf.getType() != null) {
                    existingFieldsMap.put(cf.getType().getKey(), cf);
                }
            }

            // Update or add custom fields
            if (userProvidedValues != null) {
                for (Map.Entry<String, String> entry : userProvidedValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // Find the matching custom type
                    CustomType matchingType = null;
                    for (CustomType ct : vulnCustomTypes) {
                        if (ct.getKey().equals(key) &&
                                (ct.getAssessmentTypes().isEmpty()
                                        || ct.getAssessmentTypes().contains(assessmentType))) {
                            matchingType = ct;
                            break;
                        }
                    }

                    if (matchingType != null) {
                        // Check if field already exists
                        CustomField existingField = existingFieldsMap.get(key);

                        if (existingField != null) {
                            // Update existing field
                            existingField.setValue(value);
                            em.persist(existingField);
                        } else {
                            // Create new field
                            CustomField cf = new CustomField();
                            cf.setType(matchingType);
                            cf.setValue(value);
                            existingFields.add(cf);
                            em.persist(cf);
                        }
                    }
                }
            }
        }
    }

}
