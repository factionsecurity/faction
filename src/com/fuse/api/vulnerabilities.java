package com.fuse.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.docx4j.dml.spreadsheetdrawing.CTAbsoluteAnchor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.api.dto.CategoryDTO;
import com.fuse.api.dto.CustomFieldDTO;
import com.fuse.api.dto.DefaultVulnerabilityDTO;
import com.fuse.api.dto.VulnerabilityCondensedDTO;
import com.fuse.api.dto.VulnerabilityDTO;
import com.fuse.dao.APIKeys;
import com.fuse.dao.Assessment;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.api.util.Support;
import com.fuse.utils.FSUtils;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Api(value = "/vulnerabilities")
@Path("/vulnerabilities")
public class vulnerabilities {
    private String SUCCESS = "[{ \"result\" : \"SUCCESS\"}]";
    private String SUCCESSMSG = "[{ \"result\" : \"SUCCESS\": %s}]";
    private String ERROR = "[{ \"result\" : \"ERROR\", \"message\": \"%s\"}]";


    @GET 
    @ApiOperation(value="Gets All Default Vulnerabilities Stored in the System in a JSON format.",notes="Returns vulnerability templates with custom fields support",response=DefaultVulnerabilityDTO.class,responseContainer="List",position=50)@ApiResponses(value={

    @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Default Vulnerabilites Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/default")
	public Response getalldefault(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey
			){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		List<DefaultVulnerabilityDTO> dtos = new ArrayList<>();
		
		try{
			User u = Support.getUser(em, apiKey);
			if(u != null){
				
				try{
					// Get all vulnerability custom types (no assessment type filter for templates)
					List<CustomType> vulnCustomTypes = em.createQuery(
							"from CustomType where type = :type and deleted = false", CustomType.class)
							.setParameter("type", CustomType.ObjType.VULN.getValue())
							.getResultList();
					
					List<DefaultVulnerability> defaults = (List<DefaultVulnerability>)em.createQuery("from DefaultVulnerability").getResultList();
					for(DefaultVulnerability v : defaults){
						v.updateRiskLevels(em);
						DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(v, vulnCustomTypes);
						dtos.add(dto);
					}
					
					// Serialize using Jackson
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(dtos);
					return Response.status(200).entity(json).build();
					
				}catch(JsonProcessingException e) {
					return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
				}catch(Exception ex){
					ex.printStackTrace();
					return Response.status(400).entity(String.format(Support.ERROR,"Unknown Error. Check logs.")).build();
				}
				
			}else{
				return Response.status(401).entity(String.format(Support.ERROR,"Not Authorized")).build();
			}
		}finally{
			em.close();
		}
	}

    @GET
    @ApiOperation(value = "Gets All Default Vulnerabilities Stored in the System in a CSV format.", notes = "", position = 40)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "All Default Vulnerabilites Returned") })
    @Produces("text/csv")
    @Path("/csv/default")
    public Response getalldefaulcsv(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);
            User u = Support.getUser(em, apiKey);
            if (u != null) {
                String[] titleLine = {
                        "Id",
                        "Name",
                        "CategoryId",
                        "CategoryName",
                        "Description",
                        "Recommendation",
                        "SeverityId",
                        "ImpactId",
                        "LikelihoodId",
                        "isActive",
                        "CVSS31Score",
                        "CVSS31String",
                        "CVSS40Score",
                        "CVSS40String",
                        "CustomFields"
                };
                csvWriter.writeNext(titleLine);

                try {
                    List<CustomType> vulnCustomTypes = em.createQuery(
                            "from CustomType where type = :type and deleted = false", CustomType.class)
                            .setParameter("type", CustomType.ObjType.VULN.getValue())
                            .getResultList();
                    List<DefaultVulnerability> defaults = (List<DefaultVulnerability>) em
                            .createQuery("from DefaultVulnerability").getResultList();
                    for (DefaultVulnerability v : defaults) {
                        DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(v, vulnCustomTypes);
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonFields = mapper.writeValueAsString(dto.getCustomFields());
                        String[] line = {
                                "" + v.getId(),
                                v.getName(),
                                "" + v.getCategory().getId(),
                                v.getCategory().getName(),
                                v.getDescription().replace("\r", "\\r").replace("\n", "\\n"),
                                v.getRecommendation().replace("\r", "\\r").replace("\n", "\\n"),
                                "" + v.getOverall(),
                                "" + v.getImpact(),
                                "" + v.getLikelyhood(),
                                v.getActive() == null ? "true" : "" + v.getActive(),
                                v.getCvss31Score(),
                                v.getCvss31String(),
                                v.getCvss40Score(),
                                v.getCvss40String(),
                                jsonFields
                        };
                        csvWriter.writeNext(line);
                    }
                    return Response.status(200).entity(stringWriter.toString()).build();
                } catch (Exception ex) {

                    ex.printStackTrace();
                    return Response.status(400).entity(String.format(this.ERROR, "Unknown Error. Check logs.")).build();

                }

            } else {
                return Response.status(401).entity(String.format(this.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }
    }

    @POST
    @ApiOperation(value = "Upload Default Vulnerabilties to Faction in CSV format", notes = "Upload the same CSV produced by GET /csv/default. The first row must be a header; "
            + "columns are matched by header name (case-insensitive), so column order may vary and unknown columns are ignored.\n\n"
            + "Recognized headers: Id, Name, CategoryId, CategoryName, Description, Recommendation, "
            + "SeverityId, ImpactId, LikelihoodId, isActive, CVSS31Score, CVSS31String, CVSS40Score, CVSS40String, CustomFields.\n\n"
            + "If Id is empty a new vulnerability is created; otherwise the matching record is updated.\n"
            + "If CategoryId is missing, CategoryName is required (and an existing category with that name is used, or a new one created).\n"
            + "CustomFields, when present, must be a JSON array matching the download format; unknown keys are skipped.", position = 45)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "All Default Vulnerabilites Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/csv/default")
    public Response uploadDefaultCSVVulns(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "CSV of Default Vulnerabilities", required = true) @DefaultValue("Id,Name,CategoryId,CategoryName,Description,Recommendation,SeverityId,ImpactId,LikelihoodId,isActive,CVSS31Score,CVSS31String,CVSS40Score,CVSS40String,CustomFields") String vulnList) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();

        try {
            User u = Support.getUser(em, apiKey);
            if (u != null) {

                try {
                    // Parse with no escape character so backslashes inside descriptions
                    // and JSON custom fields survive intact. The download stores literal
                    // \r and \n in description/recommendation; we restore them per-field
                    // below rather than mangling the whole document up front.
                    CSVReader csv = buildCsvReader(vulnList);

                    String[] header = csv.readNext();
                    if (header == null) {
                        return Response.status(400)
                                .entity(String.format(this.ERROR, "CSV is empty")).build();
                    }
                    Map<String, Integer> col = buildHeaderMap(header);

                    // If the first row is data (no recognizable header), treat positionally
                    // using the legacy column order so older clients keep working.
                    boolean firstRowIsData = !col.containsKey("name") && !col.containsKey("vulnname");
                    if (firstRowIsData) {
                        col = legacyPositionalMap();
                    }

                    List<CustomType> vulnCustomTypes = em.createQuery(
                            "from CustomType where type = :type and deleted = false", CustomType.class)
                            .setParameter("type", CustomType.ObjType.VULN.getValue())
                            .getResultList();
                    ObjectMapper mapper = new ObjectMapper();

                    String[] line = firstRowIsData ? header : csv.readNext();
                    int index = firstRowIsData ? 0 : 1;
                    while (line != null) {
                        String idStr = getCol(line, col, "id");
                        String name = getCol(line, col, "name", "vulnname");
                        String catIdStr = getCol(line, col, "categoryid");
                        String catName = getCol(line, col, "categoryname");
                        String descriptionRaw = getCol(line, col, "description");
                        String recommendationRaw = getCol(line, col, "recommendation");
                        String sevIdStr = getCol(line, col, "severityid");
                        String impactIdStr = getCol(line, col, "impactid");
                        String likelihoodIdStr = getCol(line, col, "likelihoodid");
                        String activeStr = getCol(line, col, "isactive", "active");
                        String cvss31Score = nullIfBlank(getCol(line, col, "cvss31score", "cvssscore"));
                        String cvss31String = nullIfBlank(getCol(line, col, "cvss31string", "cvssstring"));
                        String cvss40Score = nullIfBlank(getCol(line, col, "cvss40score"));
                        String cvss40String = nullIfBlank(getCol(line, col, "cvss40string"));
                        String customFieldsJson = getCol(line, col, "customfields");

                        Long id = isBlank(idStr) ? null : Long.parseLong(idStr.trim());
                        Long catId = isBlank(catIdStr) ? null : Long.parseLong(catIdStr.trim());
                        Integer sevId = isBlank(sevIdStr) ? null : Integer.parseInt(sevIdStr.trim());
                        Integer impactId = isBlank(impactIdStr) ? null : Integer.parseInt(impactIdStr.trim());
                        Integer likelihoodId = isBlank(likelihoodIdStr) ? null : Integer.parseInt(likelihoodIdStr.trim());
                        Boolean active = isBlank(activeStr) ? Boolean.TRUE : Boolean.parseBoolean(activeStr.trim());
                        name = isBlank(name) ? null : name.trim();
                        catName = isBlank(catName) ? null : catName.trim();

                        // Undo the download's literal-\r/\n substitution before markdown conversion.
                        String description = unescapeNewlines(descriptionRaw);
                        description = FSUtils.convertFromMarkDown(description);
                        description = FSUtils.sanitizeHTML(description);
                        String recommendation = unescapeNewlines(recommendationRaw);
                        recommendation = FSUtils.convertFromMarkDown(recommendation);
                        recommendation = FSUtils.sanitizeHTML(recommendation);

                        if (name == null) {
                            return Response.status(400)
                                    .entity(String.format(this.ERROR, "Name on line " + index + " is invalid")).build();
                        }
                        if (catId == null && catName == null) {
                            return Response.status(400).entity(String.format(this.ERROR,
                                    "Must Specifiy a Category Id or Category Name on line " + index)).build();
                        }
                        DefaultVulnerability dv = new DefaultVulnerability();
                        if (id != null) {
                            DefaultVulnerability existing = em.find(DefaultVulnerability.class, id);
                            if (existing != null) {
                                dv = existing;
                            }
                        }
                        dv.setName(name);
                        dv.setDescription(description);
                        dv.setRecommendation(recommendation);
                        dv.setOverall(sevId);
                        dv.setLikelyhood(likelihoodId);
                        dv.setImpact(impactId);
                        dv.setActive(active);
                        dv.setCvss31Score(cvss31Score);
                        dv.setCvss31String(cvss31String);
                        dv.setCvss40Score(cvss40Score);
                        dv.setCvss40String(cvss40String);
                        if (catId == null) {
                            Category cat = (Category) em.createQuery("from Category where name = :name ")
                                    .setParameter("name", catName)
                                    .getResultList()
                                    .stream()
                                    .findFirst()
                                    .orElse(null);
                            if (cat == null) {
                                cat = new Category();
                                cat.setName(catName);
                                HibHelper.getInstance().preJoin();
                                em.joinTransaction();
                                em.persist(cat);
                                HibHelper.getInstance().commit();
                            }
                            dv.setCategory(cat);
                        } else {
                            Category cat = em.find(Category.class, catId);
                            if (cat != null) {
                                dv.setCategory(cat);
                            } else {
                                return Response.status(400).entity(
                                        String.format(this.ERROR, "Category ID does not exist on line " + index))
                                        .build();
                            }

                        }

                        HibHelper.getInstance().preJoin();
                        em.joinTransaction();
                        em.persist(dv);
                        HibHelper.getInstance().commit();

                        if (!isBlank(customFieldsJson)) {
                            List<CustomFieldDTO> incoming;
                            try {
                                incoming = mapper.readValue(customFieldsJson,
                                        new TypeReference<List<CustomFieldDTO>>() {});
                            } catch (Exception jsonEx) {
                                return Response.status(400).entity(String.format(this.ERROR,
                                        "CustomFields JSON on line " + index + " is invalid")).build();
                            }
                            mergeCustomFields(em, dv, incoming, vulnCustomTypes);
                        }

                        line = csv.readNext();
                        index++;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Response.status(400).entity(String.format(this.ERROR, "Unknown Error. Check logs.")).build();
                }

            } else {
                return Response.status(401).entity(String.format(this.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }
        return Response.status(200).build();
    }

    static Map<String, Integer> buildHeaderMap(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            if (header[i] == null) continue;
            String key = header[i].trim().toLowerCase();
            if (!key.isEmpty() && !map.containsKey(key)) {
                map.put(key, i);
            }
        }
        return map;
    }

    static Map<String, Integer> legacyPositionalMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("id", 0);
        map.put("name", 1);
        map.put("vulnname", 1);
        map.put("categoryid", 2);
        map.put("categoryname", 3);
        map.put("description", 4);
        map.put("recommendation", 5);
        map.put("severityid", 6);
        map.put("impactid", 7);
        map.put("likelihoodid", 8);
        map.put("isactive", 9);
        map.put("active", 9);
        map.put("cvss31score", 10);
        map.put("cvssscore", 10);
        map.put("cvss31string", 11);
        map.put("cvssstring", 11);
        map.put("cvss40score", 12);
        map.put("cvss40string", 13);
        map.put("customfields", 14);
        return map;
    }

    static String getCol(String[] line, Map<String, Integer> col, String... names) {
        for (String n : names) {
            Integer idx = col.get(n);
            if (idx != null && idx < line.length) {
                return line[idx];
            }
        }
        return null;
    }

    static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    static String nullIfBlank(String s) {
        return isBlank(s) ? null : s.trim();
    }

    static String unescapeNewlines(String s) {
        if (s == null) return "";
        return s.replace("\\r\\n", "\r\n").replace("\\n", "\n").replace("\\r", "\r");
    }

    static CSVReader buildCsvReader(String csv) {
        return new CSVReaderBuilder(new StringReader(csv))
                .withCSVParser(new CSVParserBuilder().withEscapeChar('\0').build())
                .build();
    }

    private void mergeCustomFields(EntityManager em, DefaultVulnerability vuln,
                                   List<CustomFieldDTO> incoming, List<CustomType> vulnCustomTypes) {
        if (incoming == null || incoming.isEmpty()) return;

        List<CustomField> existingFields = vuln.getCustomFields();
        if (existingFields == null) {
            existingFields = new ArrayList<>();
            vuln.setCustomFields(existingFields);
        }
        Map<String, CustomField> existingByKey = new HashMap<>();
        for (CustomField cf : existingFields) {
            if (cf.getType() != null) {
                existingByKey.put(cf.getType().getKey(), cf);
            }
        }
        Map<String, CustomType> typesByKey = new HashMap<>();
        for (CustomType ct : vulnCustomTypes) {
            typesByKey.put(ct.getKey(), ct);
        }

        for (CustomFieldDTO entry : incoming) {
            String key = entry.getKey();
            if (key == null) continue;
            CustomType matchingType = typesByKey.get(key);
            if (matchingType == null) continue;

            HibHelper.getInstance().preJoin();
            em.joinTransaction();

            CustomField existingField = existingByKey.get(key);
            if (existingField != null) {
                existingField.setValue(entry.getValue());
                em.persist(existingField);
            } else {
                CustomField cf = new CustomField();
                cf.setType(matchingType);
                cf.setValue(entry.getValue());
                existingFields.add(cf);
                em.persist(cf);
            }
            HibHelper.getInstance().commit();
        }
    }

    @POST
    @ApiOperation(value = "Upload Default Vulnerabilties to Faction in JSON format", notes = "Accepts a JSON list of vulnerabilities. All entity fields and custom fields are supported.\n\n"
            + "If Id is empty a new vulnerability is created. If Id is populated the matching record is updated; "
            + "fields omitted (or sent as null) on an existing record are left unchanged, except for description, "
            + "recommendation, name, category, and cvss values, which are always replaced with what was sent.\n\n"
            + "If CategoryId is missing then CategoryName is required. If a category with the same name exists "
            + "the existing category is used; otherwise a new one is created. If CategoryId is populated then "
            + "CategoryName is ignored.\n\n"
            + "Custom fields are merged by key — existing custom fields not present in the request are preserved.", position = 55)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Default Vulnerabilites Created/Updated") })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/default")
    public Response uploadDefaultJSONVulns(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "JSON List of Default Vulnerabilities", required = true) List<DefaultVulnerabilityDTO> vulnList) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();

        try {
            User u = Support.getUser(em, apiKey);
            List<String> createdIds = new ArrayList<>();
            if (u != null) {

                try {
                    List<CustomType> vulnCustomTypes = em.createQuery(
                            "from CustomType where type = :type and deleted = false", CustomType.class)
                            .setParameter("type", CustomType.ObjType.VULN.getValue())
                            .getResultList();

                    for (DefaultVulnerabilityDTO dto : vulnList) {
                        DefaultVulnerability target = null;
                        if (dto.getId() != null) {
                            target = em.find(DefaultVulnerability.class, dto.getId());
                        }
                        if (target == null) {
                            target = new DefaultVulnerability();
                        }
                        DtoApplyResult result = applyDtoToVulnerability(em, target, dto, vulnCustomTypes);
                        if (result.errorResponse != null) {
                            return result.errorResponse;
                        }
                        createdIds.add("" + result.vuln.getId());
                    }

                    String returnMsg = String.format(Support.SUCCESSMSG, "\"vids\": [" +
                            String.join(",", createdIds) + "]");
                    return Response.status(200).entity(returnMsg).build();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Response.status(400).entity(String.format(Support.ERROR, "Unknown Error. Check logs."))
                            .build();
                }

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }
    }

    @POST
    @ApiOperation(value = "Update a single default vulnerability by id (JSON)", notes = "Updates the default vulnerability with the path id from a JSON body. "
            + "All entity fields and custom fields are supported. Fields omitted in the body are left unchanged, "
            + "except for description, recommendation, name, category, and cvss values which are always replaced. "
            + "Returns the updated vulnerability as JSON.", position = 56,
            response = DefaultVulnerabilityDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Vulnerability Not Found"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Default Vulnerability Updated") })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/default/{id : [0-9]+}")
    public Response updateDefaultVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Default Vulnerability Id", required = true) @PathParam("id") Long defaultVulnId,
            @ApiParam(value = "Default Vulnerability JSON", required = true) DefaultVulnerabilityDTO dto) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
            try {
                DefaultVulnerability target = em.find(DefaultVulnerability.class, defaultVulnId);
                if (target == null) {
                    return Response.status(404)
                            .entity(String.format(Support.ERROR, "Default Vulnerability not found")).build();
                }
                List<CustomType> vulnCustomTypes = em.createQuery(
                        "from CustomType where type = :type and deleted = false", CustomType.class)
                        .setParameter("type", CustomType.ObjType.VULN.getValue())
                        .getResultList();

                if (dto == null) {
                    dto = new DefaultVulnerabilityDTO();
                }
                // Path id wins over body id
                dto.setId(defaultVulnId);

                DtoApplyResult result = applyDtoToVulnerability(em, target, dto, vulnCustomTypes);
                if (result.errorResponse != null) {
                    return result.errorResponse;
                }

                DefaultVulnerabilityDTO out = DefaultVulnerabilityDTO.fromEntity(result.vuln, vulnCustomTypes);
                return Response.status(200).entity(out).build();
            } catch (Exception ex) {
                ex.printStackTrace();
                return Response.status(400).entity(String.format(Support.ERROR, "Unknown Error. Check logs.")).build();
            }
        } finally {
            em.close();
        }
    }

    @DELETE
    @ApiOperation(value = "Delete a default vulnerability by id", notes = "Deletes the default vulnerability template (and its custom field values) with the given id. "
            + "Existing assessment vulnerabilities created from this template are not affected.", position = 57)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Vulnerability Not Found"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Default Vulnerability Deleted") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/default/{id : [0-9]+}")
    public Response deleteDefaultVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Default Vulnerability Id", required = true) @PathParam("id") Long defaultVulnId) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
            try {
                DefaultVulnerability target = em.find(DefaultVulnerability.class, defaultVulnId);
                if (target == null) {
                    return Response.status(404)
                            .entity(String.format(Support.ERROR, "Default Vulnerability not found")).build();
                }
                HibHelper.getInstance().preJoin();
                em.joinTransaction();
                em.remove(target);
                HibHelper.getInstance().commit();
                return Response.status(200).entity(this.SUCCESS).build();
            } catch (Exception ex) {
                ex.printStackTrace();
                return Response.status(400).entity(String.format(Support.ERROR, "Unknown Error. Check logs.")).build();
            }
        } finally {
            em.close();
        }
    }

    private static class DtoApplyResult {
        DefaultVulnerability vuln;
        Response errorResponse;
    }

    private DtoApplyResult applyDtoToVulnerability(EntityManager em, DefaultVulnerability target,
            DefaultVulnerabilityDTO dto, List<CustomType> vulnCustomTypes) {
        DtoApplyResult result = new DtoApplyResult();
        result.vuln = target;

        if (dto.getName() == null || dto.getName().trim().equals("")) {
            result.errorResponse = Response.status(400)
                    .entity(String.format(Support.ERROR, "Name is invalid")).build();
            return result;
        }
        if (dto.getCategoryId() == null
                && (dto.getCategoryName() == null || dto.getCategoryName().trim().equals(""))) {
            result.errorResponse = Response.status(400)
                    .entity(String.format(Support.ERROR, "Must Specifiy a Category Id or Category Name")).build();
            return result;
        }

        target.setName(dto.getName());
        String description = FSUtils.convertFromMarkDown(dto.getDescription() == null ? "" : dto.getDescription());
        description = FSUtils.sanitizeHTML(description);
        target.setDescription(description);
        String recommendation = FSUtils.convertFromMarkDown(dto.getRecommendation() == null ? "" : dto.getRecommendation());
        recommendation = FSUtils.sanitizeHTML(recommendation);
        target.setRecommendation(recommendation);
        if (dto.getSeverityId() != null) target.setOverall(dto.getSeverityId());
        if (dto.getLikelihoodId() != null) target.setLikelyhood(dto.getLikelihoodId());
        if (dto.getImpactId() != null) target.setImpact(dto.getImpactId());
        if (dto.getActive() != null) target.setActive(dto.getActive());
        target.setCvss31Score(dto.getCvss31Score());
        target.setCvss31String(dto.getCvss31String());
        target.setCvss40Score(dto.getCvss40Score());
        target.setCvss40String(dto.getCvss40String());

        if (dto.getCategoryId() == null) {
            Category cat = (Category) em.createQuery("from Category where name = :name ")
                    .setParameter("name", dto.getCategoryName())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (cat == null) {
                cat = new Category();
                cat.setName(dto.getCategoryName());
                HibHelper.getInstance().preJoin();
                em.joinTransaction();
                em.persist(cat);
                HibHelper.getInstance().commit();
            }
            target.setCategory(cat);
        } else {
            Category cat = em.find(Category.class, dto.getCategoryId());
            if (cat == null) {
                result.errorResponse = Response.status(400)
                        .entity(String.format(Support.ERROR, "Category ID does not exist")).build();
                return result;
            }
            target.setCategory(cat);
        }

        HibHelper.getInstance().preJoin();
        em.joinTransaction();
        em.persist(target);
        HibHelper.getInstance().commit();

        if (dto.getCustomFields() != null && !dto.getCustomFields().isEmpty()) {
            mergeCustomFields(em, target, dto.getCustomFields(), vulnCustomTypes);
        }

        return result;
    }

    @GET
    @ApiOperation(value = "Get a single default vulnerability template by id.", notes = "Returns the vulnerability template with the given numeric id, including custom fields. Returns 404 if no template matches.",
            response = DefaultVulnerabilityDTO.class, position = 58)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Vulnerability Not Found"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Vulnerability Template Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/default/{id : [0-9]+}")
    public Response getDefaultVulnByIdPath(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Default Vulnerability Id", required = true) @PathParam("id") Long defaultVulnId) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
            try {
                DefaultVulnerability dv = em.find(DefaultVulnerability.class, defaultVulnId);
                if (dv == null) {
                    return Response.status(404)
                            .entity(String.format(Support.ERROR, "Default Vulnerability not found")).build();
                }
                dv.updateRiskLevels(em);
                List<CustomType> vulnCustomTypes = em.createQuery(
                        "from CustomType where type = :type and deleted = false", CustomType.class)
                        .setParameter("type", CustomType.ObjType.VULN.getValue())
                        .getResultList();
                DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(dv, vulnCustomTypes);
                return Response.status(200).entity(dto).build();
            } catch (Exception ex) {
                ex.printStackTrace();
                return Response.status(400).entity(String.format(Support.ERROR, "Unknown Error. Check logs.")).build();
            }
        } finally {
            em.close();
        }
    }

    @GET
    @ApiOperation(value = "Search for default vulnerbilities based on vulnerability name. ", notes = "Performs partial (case-insensitive substring) matching on the name parameter. Returns vulnerability templates with custom fields support. "
            + "Use GET /default/search?name=... if your search term contains a forward slash (\"/\"); path-param URL encoding does not preserve '/'.\n\n"
            + "Note: numeric paths are routed to GET /default/{id} (single template by id) instead of name search.", response = DefaultVulnerabilityDTO.class, responseContainer = "List", position = 60)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "All Matching Vulnerabilites Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/default/{name}")
    public Response searchdefault(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability name to search", required = true) @PathParam("name") String name) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
            return runDefaultVulnSearch(em, name);
        } finally {
            em.close();
        }
    }

    @GET
    @ApiOperation(value = "Search for default vulnerabilities by name (query parameter form).", notes = "Same partial, case-insensitive substring search as GET /default/{name}, but takes the search term as a query parameter so values containing '/' (e.g. \"LLMNR/NBT-NS\") round-trip cleanly.",
            response = DefaultVulnerabilityDTO.class, responseContainer = "List", position = 61)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "All Matching Vulnerabilites Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/default/search")
    public Response searchDefaultByQuery(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability name to search (substring, case-insensitive)") @javax.ws.rs.QueryParam("name") @DefaultValue("") String name) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
            return runDefaultVulnSearch(em, name);
        } finally {
            em.close();
        }
    }

    private Response runDefaultVulnSearch(EntityManager em, String name) {
        try {
            String safeName = name == null ? "" : name;
            String pattern = jsonStringEscape(escapeMongoRegex(safeName));
            String query = "{ \"name\" : { \"$regex\": \"" + pattern + "\", \"$options\": \"i\" } }";
            @SuppressWarnings("unchecked")
            List<DefaultVulnerability> vulns = (List<DefaultVulnerability>) em
                    .createNativeQuery(query, DefaultVulnerability.class)
                    .getResultList();

            List<CustomType> vulnCustomTypes = em.createQuery(
                    "from CustomType where type = :type and deleted = false", CustomType.class)
                    .setParameter("type", CustomType.ObjType.VULN.getValue())
                    .getResultList();

            List<DefaultVulnerabilityDTO> dtos = new ArrayList<>();
            for (DefaultVulnerability v : vulns) {
                v.updateRiskLevels(em);
                dtos.add(DefaultVulnerabilityDTO.fromEntity(v, vulnCustomTypes));
            }

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dtos);
            return Response.status(200).entity(json).build();
        } catch (JsonProcessingException e) {
            return Response.status(500)
                    .entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(400)
                    .entity(String.format(Support.ERROR, "Unknown Error. Check logs.")).build();
        }
    }

    /** Escape PCRE/JS regex metacharacters so a user-supplied search term matches literally. */
    static String escapeMongoRegex(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ("\\.[]{}()*+?^$|/-".indexOf(c) >= 0) {
                out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }

    /** Escape a string so it can safely appear inside a JSON double-quoted string literal. */
    static String jsonStringEscape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"':  out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    @GET
    @ApiOperation(value = "Search for default vulnerability based on default vulnerability id. ", response = DefaultVulnerabilityDTO.class, position = 65)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Matching Vulnerability Template Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/default/getvuln/{id}")
    public Response getDefaultVulnById(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Default Vulnerability Id", required = true) @PathParam("id") Long defaultVulnId) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        User u = Support.getUser(em, apiKey);
        try {
            if (u != null) {

                try {
                    DefaultVulnerability dv = em.find(DefaultVulnerability.class, defaultVulnId);
                    if (dv == null) {
                        return Response.status(400)
                                .entity(String.format(Support.ERROR, "Invalid Vulnerability Template")).build();
                    }
                    dv.updateRiskLevels(em);
                    List<CustomType> vulnCustomTypes = em.createQuery(
                            "from CustomType where type = :type and deleted = false", CustomType.class)
                            .setParameter("type", CustomType.ObjType.VULN.getValue())
                            .getResultList();
                    DefaultVulnerabilityDTO dto = DefaultVulnerabilityDTO.fromEntity(dv, vulnCustomTypes);

                    // Serialize using Jackson
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(dto);
                    return Response.status(200).entity(json).build();

                } catch (JsonProcessingException e) {
                    return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response"))
                            .build();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Response.status(400).entity(String.format(Support.ERROR, "Unknown Error. Check logs."))
                            .build();
                }

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } finally {
            em.close();
        }
    }

    @GET
    @ApiOperation(value = "Get a vulnerability based on its vuln id. ", notes = "Returns vulnerability with custom fields support", response = VulnerabilityDTO.class, position = 80)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Matching Vulnerability Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getvuln/{id}")
    public Response getVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability ID", required = true) @PathParam("id") Long vulnid) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            
            Vulnerability vuln = em.find(Vulnerability.class, vulnid);
            if (vuln == null)
                return Response.status(400).entity(String.format(Support.ERROR, "No Matching Result for vulnid.")).build();

            vuln.updateRiskLevels(em);
            AssessmentQueries.updateImages(em, vuln);
            VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);
            
            // Add custom fields if any
            if (vuln.getCustomFields() != null) {
                dto.setCustomFieldsFromEntity(vuln.getCustomFields());
            }
            
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dto);
            return Response.status(200).entity(json).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    @GET
    @ApiOperation(value = "Get Customized Risk Rankings ", notes = "", response = RiskLevel.class, responseContainer = "List", position = 75)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "All Risk Levels Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getrisklevels")
    public Response getRiskLevels(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null)
                return Response.status(401).entity(String.format(this.ERROR, "Not Authorized")).build();

            List<RiskLevel> levels = em.createQuery("from RiskLevel order by riskId desc").getResultList();
            JSONArray array = new JSONArray();
            for (RiskLevel level : levels) {
                JSONObject obj = new JSONObject();
                obj.put("name", level.getRisk());
                obj.put("id", level.getRiskId());
                array.add(obj);
            }
            return Response.status(200).entity(array.toJSONString()).build();
        } finally {
            em.close();
        }
    }

    @GET
    @ApiOperation(value = "Get Vulnerability Info based on it's assigned tracking id. ", notes = "Returns vulnerability with custom fields support", response = VulnerabilityDTO.class, position = 70)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Matching Vulnerability Returned") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("gettracking/{track}")
    public Response getVuln(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability Tracking ID", required = true) @PathParam("track") String tracking) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();

            Vulnerability vuln = (Vulnerability) em.createQuery("from Vulnerability where tracking = :track")
                    .setParameter("track", tracking)
                    .getResultList().stream().findFirst().orElse(null);
            if (vuln == null)
                return Response.status(400).entity(String.format(Support.ERROR, "No Matching Result for tracking id.")).build();

            vuln.updateRiskLevels(em);
            AssessmentQueries.updateImages(em, vuln);
            VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(vuln);
            
            // Add custom fields if any
            if (vuln.getCustomFields() != null) {
                dto.setCustomFieldsFromEntity(vuln.getCustomFields());
            }
            
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dto);
            return Response.status(200).entity(json).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    @POST
    @ApiOperation(value = "Assign a tracking number to a vulnerability.", notes = "", position = 90)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Vulnerability Successfully Assigned a Tracking ID.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("settracking")
    public Response setTracking(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability ID", required = true) @FormParam("vulnId") Long vulnid,
            @ApiParam(value = "Tracking ID", required = true) @FormParam("trackingId") String trackingid) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
                return Response.status(401).entity(String.format(this.ERROR, "Not Authorized")).build();
            Vulnerability vuln = em.find(Vulnerability.class, vulnid);
            if (vuln == null) {
                return Response.status(400).entity(String.format(this.ERROR, "No Matching Result for vulnid.")).build();
            }
            vuln.setTracking(trackingid);
            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(vuln);
            HibHelper.getInstance().commit();
            return Response.status(200).entity(this.SUCCESS).build();
        } finally {
            em.close();
        }

    }

    @POST
    @ApiOperation(value = "Set the status (open/closed) of a vulnerability in production or development.", notes = "Set status to open or closed and set remediation dates for the vulnerability. ", position = 85)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "Vulnerability Successfully Updated.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setstatus")
    public Response setStatus(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Vulnerability ID", required = true) @FormParam("vulnId") Long vulnid,
            @ApiParam(value = "Tracking ID", required = true) @FormParam("trackingId") String trackingid,
            @ApiParam(value = "Set Status to Fixed in the development environment", required = false) @FormParam("isClosedDev") boolean statusDev,
            @ApiParam(value = "Set Status to Fixed in the production environment", required = false) @FormParam("isClosedProd") boolean statusProd,
            @ApiParam(value = "Set Date of remediation for development", required = false) @FormParam("devClosedDate") Date devClosed,
            @ApiParam(value = "Set Date of remediation for production", required = false) @FormParam("prodClosedDate") Date prodClosed) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null || !(u.getPermissions().isRemediation() || u.getPermissions().isManager()))
                return Response.status(401).entity(String.format(this.ERROR, "Not Authorized")).build();

            if (vulnid != null && !vulnid.equals("")) {

                Vulnerability vuln = em.find(Vulnerability.class, vulnid);
                if (vuln == null) {
                    return Response.status(400).entity(String.format(this.ERROR, "No Matching Result for vulnid."))
                            .build();
                }

                if (statusDev) {
                    vuln.setDevClosed(devClosed);
                }
                if (statusDev) {
                    vuln.setClosed(prodClosed);
                }
                HibHelper.getInstance().preJoin();
                em.joinTransaction();
                em.persist(vuln);
                HibHelper.getInstance().commit();
                return Response.status(200).entity(this.SUCCESS).build();

            } else if (trackingid != null && !trackingid.equals("")) {
                Vulnerability vuln = (Vulnerability) em.createQuery("from Vulnerability where tracking = :track")
                        .setParameter("track", trackingid)
                        .getResultList().stream().findFirst().orElse(null);
                if (vuln == null) {
                    return Response.status(400).entity(String.format(this.ERROR, "No Matching Result for trackingid."))
                            .build();
                }
                if (statusDev) {
                    vuln.setDevClosed(devClosed);
                }
                if (statusDev) {
                    vuln.setClosed(prodClosed);
                }
                HibHelper.getInstance().preJoin();
                em.joinTransaction();
                em.persist(vuln);
                HibHelper.getInstance().commit();
                return Response.status(200).entity(this.SUCCESS).build();
            } else {
                return Response.status(400).entity(String.format(this.ERROR, "Vulnid or tracking id not valid."))
                        .build();

            }
        } finally {
            em.close();
        }
    }

    @POST
    @ApiOperation(value = "Get All Vulnerabilities for the specified timeframe.", notes = "Returns vulnerabilities with custom fields support", position = 10)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "List of matching vulnerabilities.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public Response getall(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Start Date of Search (MM/DD/YYYY)", required = true) @FormParam("start") String start,
            @ApiParam(value = "End Date of Search (MM/DD/YYYY)", required = false) @FormParam("end") String end) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<VulnerabilityDTO> dtos = new ArrayList<>();
        try {

            User u = Support.getUser(em, apiKey);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager()
                    || u.getPermissions().isRemediation())) {

                List<Vulnerability> vulns = null;
                if (end != null) {
                    vulns = em.createQuery("from Vulnerability as v where v.opened >= :start and v.opened <= :end")
                            .setParameter("start", sdf.parse(start))
                            .setParameter("end", sdf.parse(end))
                            .getResultList();
                } else {
                    vulns = em.createQuery("from Vulnerability as v where v.opened >= :start")
                            .setParameter("start", sdf.parse(start))
                            .getResultList();
                }

                for (Vulnerability v : vulns) {
                    Assessment parent = AssessmentQueries.getAssessmentById(em, v.getAssessmentId());
                    if (!AssessmentQueries.canAccessAssessment(u, parent)) {
                        continue;
                    }
                    v.updateRiskLevels(em);
                    AssessmentQueries.updateImages(parent, v);
                    VulnerabilityDTO dto = VulnerabilityDTO.fromEntity(v);

                    // Add custom fields if any
                    if (v.getCustomFields() != null) {
                        dto.setCustomFieldsFromEntity(v.getCustomFields());
                    }

                    dtos.add(dto);
                }

                // Serialize using Jackson
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(dtos);
                return Response.status(200).entity(json).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return Response.status(400).entity(String.format(Support.ERROR, "Invalid date format")).build();
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    @POST
    @ApiOperation(value = "Get all vulnerabilities in condensed format", notes = "Returns simplified vulnerability data without large text blocks (description, recommendation, details). Designed for MCP endpoints.", position = 12)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Unknown Error"),
            @ApiResponse(code = 200, message = "List of matching vulnerabilities.") })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("all/condensed")
    public Response getAllCondensed(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Start Date of Search (MM/DD/YYYY)", required = true) @FormParam("start") String start,
            @ApiParam(value = "End Date of Search (MM/DD/YYYY)", required = false) @FormParam("end") String end) {

        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        List<VulnerabilityCondensedDTO> dtos = new ArrayList<>();
        try {

            User u = Support.getUser(em, apiKey);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            if (u != null && (u.getPermissions().isAssessor() || u.getPermissions().isManager()
                    || u.getPermissions().isRemediation())) {

                List<Vulnerability> vulns = null;
                if (end != null) {
                    vulns = em.createQuery("from Vulnerability as v where v.opened >= :start and v.opened <= :end")
                            .setParameter("start", sdf.parse(start))
                            .setParameter("end", sdf.parse(end))
                            .getResultList();
                } else {
                    vulns = em.createQuery("from Vulnerability as v where v.opened >= :start")
                            .setParameter("start", sdf.parse(start))
                            .getResultList();
                }

                for (Vulnerability v : vulns) {
                    Assessment parent = AssessmentQueries.getAssessmentById(em, v.getAssessmentId());
                    if (!AssessmentQueries.canAccessAssessment(u, parent)) {
                        continue;
                    }
                    v.updateRiskLevels(em);
                    AssessmentQueries.updateImages(parent, v);
                    VulnerabilityCondensedDTO dto = VulnerabilityCondensedDTO.fromEntity(v);

                    // Add custom fields if any
                    if (v.getCustomFields() != null) {
                        dto.setCustomFieldsFromEntity(v.getCustomFields());
                    }

                    dtos.add(dto);
                }

                // Serialize using Jackson
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(dtos);
                return Response.status(200).entity(json).build();

            } else {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return Response.status(400).entity(String.format(Support.ERROR, "Invalid date format")).build();
        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    /**
     * Get all categories
     */
    @GET
    @ApiOperation(value = "Get all categories", notes = "Returns a list of all vulnerability categories", response = CategoryDTO.class, responseContainer = "List", position = 20)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 200, message = "Categories returned successfully")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/categories")
    public Response getAllCategories(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }

            List<Category> categories = em.createQuery("from Category order by name", Category.class).getResultList();
            List<CategoryDTO> dtos = new ArrayList<>();

            for (Category category : categories) {
                dtos.add(CategoryDTO.fromEntity(category));
            }

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dtos);
            return Response.status(200).entity(json).build();

        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    /**
     * Get a single category by ID
     */
    @GET
    @ApiOperation(value = "Get a single category by ID", notes = "Returns a specific vulnerability category", response = CategoryDTO.class, position = 25)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 404, message = "Category not found"),
            @ApiResponse(code = 200, message = "Category returned successfully")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/category/{id}")
    public Response getCategory(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Category ID", required = true) @PathParam("id") Long categoryId) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null) {
                return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
            }

            Category category = em.find(Category.class, categoryId);
            if (category == null) {
                return Response.status(404).entity(String.format(Support.ERROR, "Category not found")).build();
            }

            CategoryDTO dto = CategoryDTO.fromEntity(category);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dto);
            return Response.status(200).entity(json).build();

        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } finally {
            em.close();
        }
    }

    /**
     * Create a new category
     */
    @POST
    @ApiOperation(value = "Create a new vulnerability category", notes = "Creates a new category for organizing vulnerabilities", response = CategoryDTO.class, position = 30)
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Not Authorized"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 200, message = "Category created successfully")
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/category")
    public Response createCategory(
            @ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
            @ApiParam(value = "Category to create", required = true) CategoryDTO categoryDto) {
        EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
        try {
            User u = Support.getUser(em, apiKey);
            if (u == null || !u.getPermissions().isManager()) {
                return Response.status(401)
                        .entity(String.format(Support.ERROR, "Not Authorized - Manager permission required")).build();
            }

            if (categoryDto.getName() == null || categoryDto.getName().trim().isEmpty()) {
                return Response.status(400).entity(String.format(Support.ERROR, "Category name is required")).build();
            }

            // Check if category with same name already exists
            List<Category> existing = em.createQuery("from Category where name = :name", Category.class)
                    .setParameter("name", categoryDto.getName())
                    .getResultList();

            if (!existing.isEmpty()) {
                return Response.status(400)
                        .entity(String.format(Support.ERROR, "Category with this name already exists")).build();
            }

            // Create new category
            Category category = new Category();
            category.setName(categoryDto.getName());

            HibHelper.getInstance().preJoin();
            em.joinTransaction();
            em.persist(category);
            HibHelper.getInstance().commit();

            // Return the created category
            CategoryDTO resultDto = CategoryDTO.fromEntity(category);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(resultDto);
            return Response.status(200).entity(json).build();

        } catch (JsonProcessingException e) {
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to serialize response")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(String.format(Support.ERROR, "Failed to create category")).build();
        } finally {
            em.close();
        }
    }

}