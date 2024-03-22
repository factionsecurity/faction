package com.fuse.actions.admin;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Category;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.CustomType.ObjType;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.VulnMap;
import com.fuse.utils.AccessControl;
import com.fuse.utils.FSUtils;
import com.opencsv.CSVWriter;



@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/admin/DefaultVulns.jsp")
public class DefaultVulns  extends FSActionSupport{
	
	private String action="";
	private String name;
	private List<Category> categories;
	private String description;
	private String recommendation;
	private Long category;
	private Integer overall;
	private Integer impact;
	private Integer likelyhood;
	private String cvss31Score;
	private String cvss40Score;
	private String cvss31String;
	private String cvss40String;
	private List<DefaultVulnerability>vulnerabilities;
	private Long vulnId;
	private String catname;
	private String terms;
	private Long catId;
	private String message;
	private Integer c1,c2,h1,h2,m1,m2,l1,l2;
	private List<RiskLevel>levels = new ArrayList();
	private int riskId;
	private String riskName;
	private List<String>duedate;
	private List<String>warndate;
	private List<CustomType> vulntypes = new ArrayList();
	private List<CustomField> fields = new ArrayList();
	private String cf;
	private boolean active;
	private Long verOption;
	private InputStream stream;
	

	
	@Action(value="DefaultVulns", results={
			@Result(name="getvuln",location="/WEB-INF/jsp/admin/getVulnJSON.jsp"),
			@Result(name="vulnsearch",location="/WEB-INF/jsp/assessment/vulnsearchJSON.jsp")
		})
	public String execute() throws UnsupportedEncodingException, ParseException{
		
		if(!(this.isAcadmin() || this.isAcassessor() || this.isAcmanager() ))
			return AuditLog.notAuthorized(this, "User is not Assessor, Manager, or Admin", true);

		User user = this.getSessionUser();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		
		SystemSettings ss = (SystemSettings)em.createQuery("From SystemSettings")
				.getResultList().stream().findFirst().orElse(null);
		if(ss != null && ss.getVerificationOption() != null)
			this.verOption = ss.getVerificationOption();
		else
			this.verOption = 0l;
		
		vulntypes = em.createQuery("from CustomType where type = 1 and (deleted IS NULL or deleted = false)").getResultList();
		if(this.isAcassessor() && action != null && action.equals("json") && terms != null ){

			terms = FSUtils.sanitizeMongo(terms);
			String [] eachTerm =terms.split(" ");
			LinkedHashSet<DefaultVulnerability> dv = new LinkedHashSet();
			for(String term : eachTerm) {
				dv.addAll(	
					(List<DefaultVulnerability>)em
					.createNativeQuery("{ 'name' : {'$regex': '.*" + term + ".*', '$options': 'is'}, "
							+ "'$or' : ["
							+ " { 'active' : {'$exists': false}},"
							+ " { 'active' : true }"
							+ "] }", DefaultVulnerability.class)
					.getResultList()
					);
			}
			
			
			this.vulnerabilities = new ArrayList<>(dv);
			//session.close();
			return "vulnsearch";
		}else if(this.isAcassessor() && action != null && action.equals("getvuln") && vulnId != null ){
			
			//DefaultVulnerability dv = (DefaultVulnerability)session.createQuery("from DefaultVulnerability where id = :id")
			//		.setLong("id", vulnId).uniqueResult();
			DefaultVulnerability dv = em.find(DefaultVulnerability.class, vulnId);
			
			this.name = dv.getName();
			this.catname = dv.getCategory().getName();
			this.category = dv.getCategory().getId();
			this.impact = dv.getImpact();
			this.likelyhood = dv.getLikelyhood();
			this.overall = dv.getOverall();
			this.description = URLEncoder.encode(Base64.getEncoder().encodeToString(dv.getDescription().getBytes()),"UTF-8");
			this.recommendation = URLEncoder.encode(Base64.getEncoder().encodeToString(dv.getRecommendation().getBytes()),"UTF-8");
			this.cvss31Score = dv.getCvss31Score();
			this.cvss40Score = dv.getCvss40Score();
			this.cvss31String = dv.getCvss31String();
			this.cvss40String = dv.getCvss40String();
			this.fields = dv.getCustomFields();
			//session.close();
			return "getvuln";
			
			
		}else if(this.isAcassessor() && action != null && action.equals("importVDB")){
			if(!this.testToken(false))
				return this.ERRORJSON;
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			try {
				
				FSUtils.importVulnDB(null, 0, null, null, em);
				SystemSettings ems = (SystemSettings)em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
				ems.setImported(true);
				em.persist(ems);
				AuditLog.audit(this,"Vulnerabilies have been imported from vulnDB",AuditLog.UserAction, false);
				HibHelper.getInstance().commit();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				AuditLog.error(this,"IOException",AuditLog.UserAction, false);
				
			} catch (ParseException e) {
				AuditLog.error(this,"Parse Exception",AuditLog.UserAction, false);
				e.printStackTrace();
			}finally {
				HibHelper.getInstance().commit();
			}
			
		}else if(!(this.isAcadmin() || this.isAcmanager() || this.isAcassessor())){
			return AuditLog.notAuthorized(this, "User is not Accessor, Manager or Admin", true);
		}
		
		
		if(action != null && name != null && action.equals("addcat")){
			if(!this.testToken(false))
				return this.ERRORJSON;
			if(this.name ==  null || this.name.equals("")) {
				this._message = "Empty Category name";
				return this.ERRORJSON;
						
			}
			
			Category c = VulnerabilityQueries.getCategory(em, this.name);
			if(c != null){
				this._message = "Category name exists";
				return this.ERRORJSON;
			}else{
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				Category cat = new Category();
				cat.setName(this.name.trim());
				em.persist(cat);
				AuditLog.audit(this,"Category Added",AuditLog.UserAction, false);
				HibHelper.getInstance().commit();
				
				
			}
		}else if(action != null && name != null && action.equals("addvuln")){
			if(!this.testToken(false))
				return this.ERRORJSON;
			
			
			if(this.name == null || this.name.equals("")) {
				this._message = "Empty vulnerablility name.";
				return this.ERRORJSON;
			}
			if(this.overall == null || this.overall < 0 || this.overall > 9) {
				this._message = "Invalid Severity.";
				return this.ERRORJSON;
			}
			if(this.likelyhood == null || this.likelyhood < 0 || this.likelyhood > 9) {
				this._message = "Invalid Likelihood.";
				return this.ERRORJSON;
			}
			if(this.impact == null || this.impact < 0 || this.impact > 9) {
				this._message = "Invalid Impact.";
				return this.ERRORJSON;
			}
			if(this.category == null) {
				this._message = "Missing Category.";
				return this.ERRORJSON;
			}
			
			if(this.description == null) {
				this.description="";
			}
			if(this.recommendation == null) {
				this.recommendation="";
			}
			
			DefaultVulnerability ds = VulnerabilityQueries.getDefaultVulnerability(em, this.name);
			if(ds!= null) {
				this._message = "Vulnerability name exists.";
				return this.ERRORJSON;
			}else{
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				//Category cat = (Category) session.createQuery("from Category where id = :id").setLong("id", this.category).uniqueResult();
				Category cat = em.find(Category.class, this.category);
				DefaultVulnerability dv = new DefaultVulnerability();
				dv.setName(this.name.trim());
				dv.setCategory(cat);
				dv.setImpact(this.impact);
				dv.setRecommendation(this.recommendation);
				dv.setCvss31String(this.cvss31String.trim());
				dv.setCvss31Score(this.cvss31Score.trim());
				dv.setCvss40String(this.cvss40String.trim());
				dv.setCvss40Score(this.cvss40Score.trim());
				dv.setLikelyhood(this.likelyhood);
				dv.setOverall(this.overall);
				dv.setDescription(this.description);
				dv.setActive(true);
				em.persist(dv);
				AuditLog.audit(this,"Default Vulnerability Added",AuditLog.UserAction, AuditLog.CompDefaultVuln, dv.getId(),false);
				HibHelper.getInstance().commit();
			}
		}else if(action != null && name != null && action.equals("savevuln") && vulnId != null ){

			if(!this.testToken(false))
				return this.ERRORJSON;
			
			if(this.name == null || this.name.trim().equals("")) {
				this._message = "Empty vulnerability name";
				return this.ERRORJSON;
			}
			
			if(this.category == null) {
				this._message = "Missing Category.";
				return this.ERRORJSON;
			}
			if(this.description == null) {
				this.description="";
			}
			if(this.recommendation == null) {
				this.recommendation="";
			}
			
			DefaultVulnerability testDV = VulnerabilityQueries.getDefaultVulnerability(em, this.name);
			DefaultVulnerability dv = VulnerabilityQueries.getDefaultVulnerability(em, this.vulnId);
			if(testDV != null && testDV.getId() != dv.getId()) {
				this._message = "Vulnerability name exists.";
				return this.ERRORJSON;
			}
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Category cat = VulnerabilityQueries.getCategory(em,this.category);
			
			dv.setName(this.name.trim());
			dv.setCategory(cat);
			dv.setDescription(this.description.trim());
			dv.setRecommendation(this.recommendation.trim());
			dv.setCvss31String(this.cvss31String.trim());
			dv.setCvss31Score(this.cvss31Score.trim());
			dv.setCvss40String(this.cvss40String.trim());
			dv.setCvss40Score(this.cvss40Score.trim());
			dv.setImpact(this.impact);
			dv.setLikelyhood(this.likelyhood);
			dv.setOverall(this.overall);
			if(cf != null && !cf.equals("")){
				JSONParser parse = new JSONParser();
				JSONArray array = (JSONArray)parse.parse(cf);
				for(int i=0;i<array.size(); i++){
					JSONObject obj = (JSONObject) array.get(i);
					if(dv.getCustomFields() == null)
						dv.setCustomFields(new ArrayList());
					CustomField cf = null;
					// find the CF in the DV if it exits
					for(CustomField tmp : dv.getCustomFields()){
						if(tmp.getType().getId().equals(Long.parseLong(""+obj.get("typeid")))){
							cf = tmp;
							break;
						}
					}
					
					//CF does not exist so we need to create it
					if(cf == null){
						cf = new CustomField();
						CustomType ct = em.find(CustomType.class, Long.parseLong(""+obj.get("typeid")));
						cf.setType(ct);
					}
					cf.setValue(""+obj.get("value"));
					dv.getCustomFields().add(cf);
					
				}
			}
				
			em.persist(dv);
			AuditLog.audit(this,"Default Vulnerability Updated",AuditLog.UserAction, AuditLog.CompDefaultVuln, dv.getId(),false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
			
			
			
		}else if(action != null && action.equals("getvuln") && vulnId != null ){
			
			
			DefaultVulnerability dv = VulnerabilityQueries.getDefaultVulnerability(em,  vulnId);
			this.name = dv.getName();
			this.catname = dv.getCategory().getName();
			this.category = dv.getCategory().getId();
			this.impact = dv.getImpact();
			this.likelyhood = dv.getLikelyhood();
			this.overall = dv.getOverall();
			this.description = URLEncoder.encode(Base64.getEncoder().encodeToString(dv.getDescription().getBytes()),"UTF-8");
			this.recommendation = URLEncoder.encode(Base64.getEncoder().encodeToString(dv.getRecommendation().getBytes()),"UTF-8");
			
			return "getvuln";
			
			
		}else if(action != null && action.equals("delvuln") && vulnId != null ){
			if(!this.testToken(false))
				return this.ERRORJSON;
		
			Long count = VulnerabilityQueries.vulnCount(em, this.vulnId);
			if(count > 0l){
				this.message = "Cannot Delete Default Vulnerability once it has been assigned to an assessment. <br/> "
						+ "Would you like to make it inactive?";
				return this.ERRORJSON;
			}
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			DefaultVulnerability dv = VulnerabilityQueries.getDefaultVulnerability(em, vulnId);
			em.remove(dv);
			AuditLog.audit(this,"Default Vulnerability Deleted",AuditLog.UserAction, AuditLog.CompDefaultVuln, dv.getId(),false);
			HibHelper.getInstance().commit();
			
			return this.SUCCESSJSON;
			
		}else if(action != null && action.equals("delCat") && catId != null ){
			if(!this.testToken(false))
				return this.ERRORJSON;
			
			List<DefaultVulnerability> vulns = VulnerabilityQueries.getDefaultVulnerabilityFromCategory(em,this.catId);
			
			if(vulns!= null && vulns.size() >0){
				this.message="<b>Can't delete a Category with associated vulnerabilites.</b><br> Create a new Category and reassign Vulns to this new Category before deleting.";
				return this.ERRORJSON;
			}else{
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				Category cat = em.find(Category.class, catId);
				em.remove(cat);
				AuditLog.audit(this, "Vuln Category " + cat.getName() + " Deleted",AuditLog.UserAction,false);
				HibHelper.getInstance().commit();
				return SUCCESSJSON;
			}
		}else if(this.action != null && action.equals("updateDates")){
			if(!this.testToken(false))
				return this.ERRORJSON;
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
	
			
			for(int i=0; i<10;i++){
				Integer ddate = duedate.get(i).equals("") || !duedate.get(i).matches("[0-9]{1,10}")? null : Integer.parseInt(duedate.get(i));
				Integer wdate = warndate.get(i).equals("") || !warndate.get(i).matches("[0-9]{1,10}")? null : Integer.parseInt(warndate.get(i));
				
				levels.get(i).setDaysTillDue(ddate);
				levels.get(i).setDaysTillWarning(wdate);
				
				em.persist(levels.get(i));
			}
			AuditLog.audit(this, "Vuln Timelines Updated",AuditLog.UserAction,false);
			HibHelper.getInstance().commit();
			return SUCCESSJSON;
			
		}else if(action.equals("updateRisk")){
			if(!this.testToken(false))
				return this.ERRORJSON;
			
			for(RiskLevel level : levels){
				if(level.getRiskId() == riskId){
					level.setRisk(riskName);
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					em.persist(level);
					AuditLog.audit(this, "Updated Risk Levels",AuditLog.UserAction,false);
					HibHelper.getInstance().commit();
					return SUCCESSJSON;
				}
			}
			return SUCCESSJSON;
		}else{
		
			this.categories = (List<Category>) em.createQuery("from Category").getResultList();
			this.vulnerabilities = (List<DefaultVulnerability>) em.createQuery("from DefaultVulnerability").getResultList();
			SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
			if(ems == null){
				ems = new SystemSettings();
				c1=c2=m1=m2=h1=h2=l1=l2=100;
			}else{
				c1 = ems.getCritical();
				c2 = ems.getCritAlert();
				h1 = ems.getHigh();
				h2 = ems.getHighAlert();
				m1 =  ems.getMedium();
				m2 = ems.getMediumAlert();
				l1 = ems.getLow();
				l2 = ems.getLowAlert();
			}
			//session.close();
		}
		
		
		
		return SUCCESS;
	}
	

	@Action(value="editCat")
	public String editCat(){
		if(!this.isAcadmin())
			return LOGIN;
		
		if(this.name.equals("")) {
			this._message = "Empty category name";
			return this.ERRORJSON;
		}
		Category cat = em.find(Category.class, catId);
		if(cat == null)
			return this.ERRORJSON;
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		cat.setName(name);
		em.persist(cat);
		AuditLog.audit(this, "Edited Category " + name,AuditLog.UserAction,false);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
		
	}
	
	
	@Action(value="DeActivate")
	public String makeInactive(){
		if(!this.isAcadmin())
			return LOGIN;
		

		DefaultVulnerability dv = em.find(DefaultVulnerability.class, vulnId);
		if(dv == null)
			return this.ERRORJSON;
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		dv.setActive(false);
		AuditLog.audit(this, "Deactivated Vulnerability " + dv.getName() ,AuditLog.UserAction,false);
		em.persist(dv);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
		
	}
	@Action(value="ReActivate")
	public String makeActive(){
		if(!this.isAcadmin())
			return LOGIN;
		
		DefaultVulnerability dv = em.find(DefaultVulnerability.class, vulnId);
		if(dv == null)
			return this.ERRORJSON;
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		dv.setActive(true);
		AuditLog.audit(this, "Enabled Vulnerability " + dv.getName() ,AuditLog.UserAction,false);
		em.persist(dv);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
		
	}
	
	@Action(value="VerificationSetting")
	public String changeVerificationSettings(){
		if(!this.isAcadmin())
			return LOGIN;
		
		
		SystemSettings ss = (SystemSettings)em.createQuery("From SystemSettings")
				.getResultList().stream().findFirst().orElse(null);
		
		if(ss == null)
			return this.ERRORJSON;
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		ss.setVerificationOption(verOption);
		em.persist(ss);
		AuditLog.audit(this, "Updated Vulnerbility Settings",AuditLog.UserAction,false);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}
	
	@Action(value="GetVulnsCSV", results={
			@Result(name="vulnMap",type = "stream"
					, params = {
							"contentType", "text/csv", 
					        "inputName", "stream", 
							"contentDisposition", "attachment;filename=\"allvulns.csv\""})})
	public String getVulnsCSV() {
		if(!this.isAcadmin())
			return LOGIN;
		
		List<DefaultVulnerability> vulns = em.createQuery("from DefaultVulnerability").getResultList();
		
		StringWriter swriter = new StringWriter();
		CSVWriter write = new CSVWriter(swriter);
		
		List<String> titles = new ArrayList();
		String []tmp = new String[] {"id", "status", "name", "category id", "category name(not uploaded)", 
				"description", "recommendation", "Severity Id", "Impact Id", "Likelyhood Id"};
		for(String t : tmp) {
			titles.add(t);
		}
		List<CustomType> types = em.createQuery("from CustomType").getResultList();
		LinkedList<String> order = new LinkedList();
		for(CustomType field : types) {
			if(field.getType() == ObjType.VULN.getValue()) {
				titles.add(field.getVariable());
				order.add(field.getVariable());
			}
		}

		write.writeNext((String []) titles.toArray(new String[titles.size()]));

		
		for(DefaultVulnerability vuln : vulns) {
			String [] custom = new String[order.size()];
			for(CustomField cf : vuln.getCustomFields()) {
				int index = order.indexOf(cf.getType().getVariable());
				custom[index] = cf.getValue();
			}
			LinkedList<String> values = new LinkedList();
			values.add(""+vuln.getId()               );
			values.add(vuln.getActive() == null || vuln.getActive() == true? "Active": "InActive");
			values.add(vuln.getName()                );
			values.add(vuln.getCategory() == null ? "" : ""+vuln.getCategory().getId() );
			values.add(vuln.getCategory() == null ? "" : vuln.getCategory().getName()  );
			values.add(vuln.getDescription()  == null ? "" : vuln.getDescription()         );
			values.add(vuln.getRecommendation() == null ? "" : vuln.getRecommendation()      );
			values.add(""+vuln.getOverall()          );
			values.add(""+vuln.getImpact()           );
			values.add(""+vuln.getLikelyhood()       );
			values.add(vuln.getCvss31String());
			values.add(vuln.getCvss31Score());
			values.add(vuln.getCvss40String());
			values.add(vuln.getCvss40Score());
			for(String c : custom) {
				values.add(c);		
			}
			write.writeNext((String []) values.toArray(new String[values.size()]));

			
		}
		stream = new ByteArrayInputStream( swriter.toString().getBytes());
		AuditLog.audit(this, "Downloaded All Default Vulnerabilities",AuditLog.UserAction,true);
		return "vulnMap";
	}
	@Action(value="DefaultCategories", results={
			@Result(name="catsearch",location="/WEB-INF/jsp/admin/categoriesJSON.jsp")
		})
	public String catSearch() throws UnsupportedEncodingException, ParseException{
		if(terms == null)
			terms = "";
		categories = em.createNativeQuery("{ 'name' : {'$regex': '.*" + terms + ".*', '$options': 'is'}, "
				+ "'$or' : ["
				+ " { 'active' : {'$exists': false}},"
				+ " { 'active' : true }"
				+ "] }", Category.class).getResultList();
		return "catsearch";
	}
	
	
	public String getActiveVulns() {
		return "active";
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(name != null)
			name=name.trim();
		this.name = name;
	}
	public List<Category> getCategories() {
		return categories;
	}
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		if(description != null)
			description = description.trim();
		this.description = description;
	}
	public String getRecommendation() {
		return recommendation;
	}
	public void setRecommendation(String recommendation) {
		if(recommendation != null)
			recommendation = recommendation.trim();
		this.recommendation = recommendation;
	}
	public Long getCategory() {
		return category;
	}
	public void setCategory(Long category) {
		this.category = category;
	}
	public Integer getOverall() {
		return overall;
	}
	public void setOverall(Integer overall) {
		this.overall = overall;
	}
	public Integer getImpact() {
		return impact;
	}
	public void setImpact(Integer impact) {
		this.impact = impact;
	}
	public Integer getLikelyhood() {
		return likelyhood;
	}
	public void setLikelyhood(Integer likelyhood) {
		this.likelyhood = likelyhood;
	}
	public List<DefaultVulnerability> getVulnerabilities() {
		return vulnerabilities;
	}
	public void setVulnerabilities(List<DefaultVulnerability> vulnerabilities) {
		this.vulnerabilities = vulnerabilities;
	}
	public Long getVulnId() {
		return vulnId;
	}
	public void setVulnId(Long vulnId) {
		this.vulnId = vulnId;
	}
	public String getCatname() {
		return catname;
	}
	public void setCatname(String catname) {
		if(catname != null)
			catname = catname.trim();
		this.catname = catname;
	}
	public String getTerms() {
		return terms;
	}
	public void setTerms(String terms) {
		this.terms = terms;
	}
	public String getMessage() {
		return message;
	}
	public void setCatId(Long catId) {
		this.catId = catId;
	}
	public Integer getC1() {
		return c1;
	}
	public void setC1(Integer c1) {
		this.c1 = c1;
	}
	public Integer getC2() {
		return c2;
	}
	public void setC2(Integer c2) {
		this.c2 = c2;
	}
	public Integer getH1() {
		return h1;
	}
	public void setH1(Integer h1) {
		this.h1 = h1;
	}
	public Integer getH2() {
		return h2;
	}
	public void setH2(Integer h2) {
		this.h2 = h2;
	}
	public Integer getM1() {
		return m1;
	}
	public void setM1(Integer m1) {
		this.m1 = m1;
	}
	public Integer getM2() {
		return m2;
	}
	public void setM2(Integer m2) {
		this.m2 = m2;
	}
	public Integer getL1() {
		return l1;
	}
	public void setL1(Integer l1) {
		this.l1 = l1;
	}
	public Integer getL2() {
		return l2;
	}
	public void setL2(Integer l2) {
		this.l2 = l2;
	}
	public List<RiskLevel> getLevels() {
		return levels;
	}
	public void setRiskId(int riskId) {
		this.riskId = riskId;
	}
	public void setRiskName(String riskName) {
		if(riskName != null)
			riskName = riskName.trim();
		this.riskName = riskName;
	}
	public List<String> getDuedate() {
		return duedate;
	}
	public List<String> getWarndate() {
		return warndate;
	}
	public void setDuedate(List<String> duedate) {
		this.duedate = duedate;
	}
	public void setWarndate(List<String> warndate) {
		this.warndate = warndate;
	}
	public List<CustomType> getVulntypes() {
		return vulntypes;
	}
	public void setVulntypes(List<CustomType> vulntypes) {
		this.vulntypes = vulntypes;
	}
	public String getCf() {
		return cf;
	}
	public void setCf(String cf) {
		if(cf != null)
			cf = cf.trim();
		this.cf = cf;
	}
	public List<CustomField> getFields() {
		return fields;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}


	public Long getVerOption() {
		return verOption;
	}


	public void setVerOption(Long verOption) {
		this.verOption = verOption;
	}


	public InputStream getStream() {
		return stream;
	}
	public String getTier(){
		return FSUtils.getEnv("FACTION_TIER");
	}
	
	public String getCvss31Score() {
		return cvss31Score;
	}


	public void setCvss31Score(String cvss31Score) {
		this.cvss31Score = cvss31Score;
	}


	public String getCvss40Score() {
		return cvss40Score;
	}


	public void setCvss40Score(String cvss40Score) {
		this.cvss40Score = cvss40Score;
	}


	public String getCvss31String() {
		return cvss31String;
	}


	public void setCvss31String(String cvss31String) {
		this.cvss31String = cvss31String;
	}


	public String getCvss40String() {
		return cvss40String;
	}


	public void setCvss40String(String cvss40String) {
		this.cvss40String = cvss40String;
	}


	public String getLevelString(Integer level) {
		switch(level) {
		case 0: return "Informational"; 
		case 1: return "Recommended"; 
		case 2: return "Low"; 
		case 3: return "Medium"; 
		case 4: return "High"; 
		case 5: return "Critical"; 
		default: return "Custom";
		}
	}
	
}
