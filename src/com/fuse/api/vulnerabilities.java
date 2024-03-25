package com.fuse.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fuse.dao.APIKeys;
import com.fuse.dao.Category;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;
import com.opencsv.CSVReader;
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


@Api(value="/vulnerabilities")
@Path("/vulnerabilities")
public class vulnerabilities {
	private String SUCCESS = "[{ \"result\" : \"SUCCESS\"}]";
	private String SUCCESSMSG = "[{ \"result\" : \"SUCCESS\": %s}]";
	private String ERROR = "[{ \"result\" : \"ERROR\", \"message\": \"%s\"}]";
	
	public static class GenericVulnerability {
		@JsonProperty("Id")
		public Long id;
		@JsonProperty("Name")
		public String name;
		@JsonProperty("CategoryId")
		public Long categoryId;
		@JsonProperty("CategoryName")
		public String categoryName;
		@JsonProperty("Description")
		public String description;
		@JsonProperty("Recommendation")
		public String recommendation;
		@JsonProperty("SeverityId")
		public Integer severityId;
		@JsonProperty("LikelihoodId")
		public Integer likelihoodId;
		@JsonProperty("ImpactId")
		public Integer impactId;
		@JsonProperty("Active")
		public Boolean active;
		@JsonProperty("Cvss31Score")
		public String cvss31Score="";
		@JsonProperty("Cvss31String")
		public String cvss31String="";
		@JsonProperty("Cvss40Score")
		public String cvss40Score="";
		@JsonProperty("Cvss40String")
		public String cvss40String="";
		
		public GenericVulnerability() {}
		
		public GenericVulnerability(DefaultVulnerability defaultVuln) {
			this.id = defaultVuln.getId();
			this.name = defaultVuln.getName();
			this.categoryId = defaultVuln.getCategory().getId();
			this.categoryName = defaultVuln.getCategory().getName();
			this.description = defaultVuln.getDescription();
			this.recommendation = defaultVuln.getRecommendation();
			this.severityId = defaultVuln.getOverall();
			this.likelihoodId = defaultVuln.getLikelyhood();
			this.impactId = defaultVuln.getImpact();
			this.active = defaultVuln.getActive();
			this.cvss31Score = defaultVuln.getCvss31Score() == null ? "" : defaultVuln.getCvss31Score();
			this.cvss31String = defaultVuln.getCvss31String() == null ? "" : defaultVuln.getCvss31String();
			this.cvss40Score = defaultVuln.getCvss40Score() == null? "" : defaultVuln.getCvss40Score();
			this.cvss40String = defaultVuln.getCvss40String() == null ? "" : defaultVuln.getCvss40String();
			
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(Long categoryId) {
			this.categoryId = categoryId;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getRecommendation() {
			return recommendation;
		}

		public void setRecommendation(String recommendation) {
			this.recommendation = recommendation;
		}

		public Integer getSeverityId() {
			return severityId;
		}

		public void setSeverityId(Integer severityId) {
			this.severityId = severityId;
		}

		public Integer getLikelihoodId() {
			return likelihoodId;
		}

		public void setLikelihoodId(Integer likelihoodId) {
			this.likelihoodId = likelihoodId;
		}

		public Integer getImpactId() {
			return impactId;
		}

		public void setImpactId(Integer impactId) {
			this.impactId = impactId;
		}

		public Boolean getActive() {
			return active;
		}

		public void setActive(Boolean active) {
			this.active = active;
		}
		
		public String getCvss31Score() {
			return cvss31Score;
		}

		public void setCvss31Score(String cvss31Score) {
			this.cvss31Score = cvss31Score;
		}
		public String getCvss31String() {
			return cvss31String;
		}

		public void setCvss31String(String cvss31String) {
			this.cvss31String = cvss31String;
		}
		public String getCvss40Score() {
			return cvss31Score;
		}

		public void setCvss40Score(String cvss40Score) {
			this.cvss40Score = cvss40Score;
		}
		public String getCvss40String() {
			return cvss40String;
		}

		public void setCvss40String(String cvss40String) {
			this.cvss40String = cvss40String;
		}
	}
	
	
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
							 dmethod.getReturnType().equals(Date.class) ||
							 dmethod.getReturnType().equals(Boolean.class)
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
		    value = "Gets All Default Vulnerabilities Stored in the System in a JSON format.", 
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
		List<Vulnerability> vulns = new ArrayList<>();
		try{
			User u = this.getUser(em, apiKey);
			if(u != null){
				
				try{
					List<DefaultVulnerability> defaults = (List<DefaultVulnerability>)em.createQuery("from DefaultVulnerability").getResultList();
					for(DefaultVulnerability v : defaults){
						GenericVulnerability vuln = new GenericVulnerability(v);
						jarray.add(this.dao2JSON(vuln, GenericVulnerability.class));
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
		    value = "Gets All Default Vulnerabilities Stored in the System in a CSV format.", 
		    notes = ""
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Default Vulnerabilites Returned")})
	@Produces("text/csv")
	@Path("/csv/default")
	public Response getalldefaulcsv(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey
			){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try{
			StringWriter stringWriter = new StringWriter();
			CSVWriter csvWriter = new CSVWriter(stringWriter);
			User u = this.getUser(em, apiKey);
			if(u != null){
				
				try{
					List<DefaultVulnerability> defaults = (List<DefaultVulnerability>)em.createQuery("from DefaultVulnerability").getResultList();
					for(DefaultVulnerability v : defaults){
						String [] line = { 
								""+v.getId(), 
								v.getName(), 
								""+v.getCategory().getId(), 
								v.getCategory().getName(),
								v.getDescription(),
								v.getRecommendation(),
								""+v.getOverall(),
								""+v.getImpact(),
								""+v.getLikelyhood(),
								""+v.getActive(),
								v.getCvss31Score(),
								v.getCvss31String(),
								v.getCvss40Score(),
								v.getCvss40String()
							};
						csvWriter.writeNext(line);
					}
					return Response.status(200).entity(stringWriter.toString()).build();
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
	}
	
	@POST
	@ApiOperation(
		    value = "Upload Default Vulnerabilties to Faction in CSV format", 
		    notes = "Below is an example CSV. Note that some parameters are optional.\n "
		    		+ "If the ID is empty then a new vulnerability will be created. If the ID is populated then it will overwrite the vulnerability with the same id.\n\n "
		    		+ "If the categoryId is missing then categoryName is required. If a category with the same name exists then the existing category will be used. "
		    		+ "If the categoryName does not match an existing category then a new category will be created. \n\n"
		    		+ "If the categoryId is populated then categoryName field is ignored."
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Default Vulnerabilites Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/csv/default")
	public Response uploadDefaultCSVVulns(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "CSV of Default Vulnerabilities", required = true) 
			@DefaultValue("id(optional),vulnName,categoryId(optional*),categoryName(optional*), description, recommendation, severityId, impactId, likelihoodId, active\nid(optional),vulnName,categoryId(optional*), categoryName(optional*), description, recommendation, severityId, impactId, likelihoodId, active, cvssScore, cvssString") String vulnList
			){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		try{
			User u = this.getUser(em, apiKey);
			if(u != null){
				
				try{
					vulnList = vulnList.replaceAll("\\\\n", "\n");
					vulnList = StringEscapeUtils.unescapeHtml4(vulnList);
					CSVReader csv = new CSVReader(new StringReader(vulnList));
					String[] line;
					int index = 0;
					while ((line = csv.readNext()) != null) {
						Long id = line[0].trim().equals("") ? null : Long.parseLong(line[0].trim());
						String name = line[1].trim().equals("")? null : line[1].trim();
						Long catId = line[2].trim().equals("")? null : Long.parseLong(line[2].trim()); 
						String catName = line[3].trim().equals("")? null : line[3].trim();
						String description = line[4].trim();
						description = FSUtils.convertFromMarkDown(description);
						description = FSUtils.sanitizeHTML(description);
						String recommendation = line[5].trim();
						recommendation = FSUtils.convertFromMarkDown(recommendation);
						recommendation = FSUtils.sanitizeHTML(recommendation);
						Integer sevId = line[6].trim().equals("")? null : Integer.parseInt(line[6].trim()); 
						Integer impactId = line[7].trim().equals("")? null : Integer.parseInt(line[7].trim()); 
						Integer likelihoodId = line[8].trim().equals("")? null : Integer.parseInt(line[8].trim()); 
						Boolean active = line[9].trim().equals("")? true: Boolean.parseBoolean(line[9].trim());
						String cvss31Score = line[10].trim().equals("")? null : line[10].trim();
						String cvss31String = line[11].trim().equals("")? null : line[11].trim();
						String cvss40Score = line[12].trim().equals("")? null : line[12].trim();
						String cvss40String = line[13].trim().equals("")? null : line[13].trim();
						if(name == null) {
							return Response.status(400).entity(String.format(this.ERROR,"Name on line " + index + " is invalid")).build();
						}
						if(catId == null && catName == null) {
							return Response.status(400).entity(String.format(this.ERROR,"Must Specifiy a Category Id or Category Name on line " + index)).build();
						}
						DefaultVulnerability dv = new DefaultVulnerability();
						if(id != null) {
							dv = em.find(DefaultVulnerability.class, id);
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
						if(catId == null) {
							Category cat = (Category) em.createQuery("from Category where name = :name ")
									.setParameter("name", catName)
									.getResultList()
									.stream()
									.findFirst()
									.orElse(null);
							if(cat == null) {
								cat = new Category();
								cat.setName(catName);
								HibHelper.getInstance().preJoin();
								em.joinTransaction();
								em.persist(cat);
								HibHelper.getInstance().commit();
							}
							dv.setCategory(cat);
						}else {
							Category cat = em.find(Category.class, catId);
							if(cat != null) {
								dv.setCategory(cat);
							}else {
								return Response.status(400).entity(String.format(this.ERROR,"Category ID does not exist on line " + index)).build();
							}
							
						}
						HibHelper.getInstance().preJoin();
						em.joinTransaction();
						em.persist(dv);
						HibHelper.getInstance().commit();
						index++;
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
		return Response.status(200).build();
	}
	@POST
	@ApiOperation(
		    value = "Upload Default Vulnerabilties to Faction in JSON format", 
		    notes = "Below is an example JSON. Note that some parameters are optional.\n "
		    		+ "If the ID is empty then a new vulnerability will be created. If the ID is populated then it will overwrite the vulnerability with the same id.\n\n "
		    		+ "If the categoryId is missing then categoryName is required. If a category with the same name exists then the existing category will be used. "
		    		+ "If the categoryName does not match an existing category then a new category will be created. \n\n"
		    		+ "If the categoryId is populated then categoryName field is ignored."
		    )
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Unknown Error"),
			@ApiResponse(code = 200, message = "All Default Vulnerabilites Returned")})
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/default")
	public Response uploadDefaultJSONVulns(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "JSON List of Default Vulnerabilities", required = true) 
			 List<GenericVulnerability> vulnList
			){
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		try{
			User u = this.getUser(em, apiKey);
			if(u != null){
				
				try{
					for(GenericVulnerability gv : vulnList) {
						if(gv.getName() == null || gv.getName().trim().equals("")) {
							return Response.status(400).entity(String.format(this.ERROR,"Name is invalid")).build();
						}
						if(gv.getCategoryId() == null && (gv.getCategoryName() == null || gv.getCategoryName().trim().equals("")) ) {
							return Response.status(400).entity(String.format(this.ERROR,"Must Specifiy a Category Id or Category Name")).build();
						}
						DefaultVulnerability dv = new DefaultVulnerability();
						if(gv.getId() != null) {
							dv = em.find(DefaultVulnerability.class, gv.getId());
						}
						dv.setName(gv.getName());
						String description = FSUtils.convertFromMarkDown(dv.getDescription());
						description = FSUtils.sanitizeHTML(description);
						dv.setDescription(description);
						String recommendation = FSUtils.convertFromMarkDown(dv.getRecommendation());
						recommendation = FSUtils.sanitizeHTML(recommendation);
						dv.setRecommendation(recommendation);
						dv.setOverall(gv.getSeverityId());
						dv.setLikelyhood(gv.getLikelihoodId());
						dv.setImpact(gv.getImpactId());
						dv.setActive(gv.getActive());
						dv.setCvss31Score(gv.getCvss31Score());
						dv.setCvss31String(gv.getCvss31String());
						dv.setCvss40Score(gv.getCvss40Score());
						dv.setCvss40String(gv.getCvss40String());
						if(gv.getCategoryId() == null) {
							Category cat = (Category) em.createQuery("from Category where name = :name ")
									.setParameter("name", gv.getCategoryName())
									.getResultList()
									.stream()
									.findFirst()
									.orElse(null);
							if(cat == null) {
								cat = new Category();
								cat.setName(gv.getCategoryName());
								HibHelper.getInstance().preJoin();
								em.joinTransaction();
								em.persist(cat);
								HibHelper.getInstance().commit();
							}
							dv.setCategory(cat);
						}else {
							Category cat = em.find(Category.class, gv.getCategoryId());
							if(cat != null) {
								dv.setCategory(cat);
							}else {
								return Response.status(400).entity(String.format(this.ERROR,"Category ID does not exist")).build();
							}
							
						}
						HibHelper.getInstance().preJoin();
						em.joinTransaction();
						em.persist(dv);
						HibHelper.getInstance().commit();
						
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
		return Response.status(200).build();
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