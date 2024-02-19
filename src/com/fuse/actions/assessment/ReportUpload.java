package com.fuse.actions.assessment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.CustomType;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.MapItem;
import com.fuse.dao.Ratings;
import com.fuse.dao.ReportMap;
import com.fuse.dao.ReportMap.DataProperties;
import com.fuse.dao.User;
import com.fuse.dao.VulnMap;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;
import com.fuse.utils.ParseXML;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/report/Upload.jsp")
public class ReportUpload extends FSActionSupport{
	
	private File file_data;
	private String contentType;
	private String filename;
	private Long parseType;
	private Long id;
	private String reportType;
	private int paramid;
	private String attr;
	private boolean hasElements;
	private boolean isBase64;
	private String property;
	private String vulnName;
	private Long vulnId;
	private List<ReportMap> reports;
	private ReportMap currentMap;
	private DataProperties [] props;
	private String vulnJson;
	private Long mapid;
	private List<VulnMap> vulnMap;
	private Map<String,Long> sevMap;
	private InputStream stream;
	private String activeRConfig = "active";
	private Long defaultVuln;
	
	
	@Action(value="GetTemplate", results={
			@Result(name="reportTemplate",location="/WEB-INF/jsp/report/TemplateConfig.jsp")
	})
	public String getTemplate(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		currentMap = em.find(ReportMap.class, this.id);
		props = DataProperties.values();
		return "reportTemplate";
	}
	
	@Action(value="testReport", results={
			@Result(name="vulnData",location="/WEB-INF/jsp/report/vulndataJSON.jsp")
	})
	public String testReport(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		ParseXML parse = new ParseXML();
		ReportMap map = em.find(ReportMap.class, this.id);
		List<CustomType> cts = em.createQuery("from CustomType").getResultList();
		List<Vulnerability>vulns = parse.parseXML(file_data, 1l,
				map.getListname(), map.getMapping(), map.getMapRating(), map.getVulnMap(), 
				map.getDefaultVuln(), map.getCustomFields(), cts, false);
		
		JSONArray jarray = new JSONArray();
		for(Vulnerability v : vulns){
			JSONObject j = this.dao2JSON(v, Vulnerability.class);
			VulnMap dv = map.getVulnMap()
					.stream().filter(vm -> vm.getOriginTitle().equals(v.getName()))
					.findFirst().orElse(null);
			if(dv == null && v.getDefaultVuln() == null)
				j.put("mappedVuln", "Not Mapped");
			else if(dv == null && v.getDefaultVuln() != null)
				j.put("mappedVuln", v.getDefaultVuln().getCategory().getName() + " : " + v.getDefaultVuln().getName());
			else
				j.put("mappedVuln", dv.getTargetVuln().getCategory().getName() + " : " + dv.getTargetVuln().getName());
			jarray.add(j);
		}
		vulnJson = jarray.toJSONString();
		return "vulnData";
		
	}
	
	@Action(value="ReportConfig")
	public String reportConfig(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		reports = em.createQuery("From ReportMap").getResultList();
		if(reports == null || reports.size() == 0){
			//Create Burp Report
			ReportMap burp = new ReportMap();
			burp.setListname("issue");
			List<MapItem> map = new ArrayList();
			map.add(new MapItem("name",DataProperties.VulnName, false, false));
			map.add(new MapItem("severity", DataProperties.Severity, false, false));
			map.add(new MapItem("issueBackground", DataProperties.VulnDescription, false, false));
			map.add(new MapItem("remediationBackground", DataProperties.Recommendation, false, false));
			map.add(new MapItem("issueDetail", DataProperties.ExploitStep, false, false));
			map.add(new MapItem("requestresponse", DataProperties.ExploitStep, true, false));
			map.add(new MapItem("request", DataProperties.ExploitStep, false, true));
			map.add(new MapItem("response", DataProperties.ExploitStep, false, true));
			burp.setMapping(map);
			
			Map<String,Long>severities = new HashMap();
			severities.put("Low", 2l);
			severities.put("Information", 1l);
			severities.put("Medium", 3l);	
			severities.put("High", 4l);
			burp.setMapRating(severities);
			
			burp.setReportName("Burp XML Report Template");
			
			
			ReportMap zap = new ReportMap();
			List<MapItem> map2 = new ArrayList();
			map2.add(new MapItem("name", DataProperties.VulnName, false, false));
			map2.add(new MapItem("riskdesc",DataProperties.Severity, false, false));
			map2.add(new MapItem("desc", DataProperties.VulnDescription, false, false));
			map2.add(new MapItem("solution", DataProperties.Recommendation, false, false));
			map2.add(new MapItem("reference",DataProperties.Recommendation, false, false));
			map2.add(new MapItem("otherinfo",DataProperties.ExploitStep, false, false));
			map2.add(new MapItem("instances",DataProperties.ExploitStep, true, false));
			map2.add(new MapItem("instance", DataProperties.ExploitStep, true, false));
			map2.add(new MapItem("uri", DataProperties.ExploitStep, false, false));
			
			zap.setMapping(map2);
			zap.setListname("alertitem");
			
			Map<String,Long>severities2 = new HashMap();
			severities2.put("Low (Low)", 2l);
			severities2.put("Low (Medium)", 2l);
			severities2.put("Medium (Medium)", 3l);	
			severities2.put("High (High)", 4l);
			zap.setMapRating(severities2);
			
			zap.setReportName("ZAP XML Template");
			
		/* app scan template 	
			ReportMap appscan = new ReportMap();
			List<MapItem> map3 = new ArrayList();
			map3.add(new MapItem("advisory", DataProperties.None, true, false));
			map3.add(new MapItem("name", DataProperties.VulnName, false, false));
			map3.add(new MapItem("testTechnicalDescription",DataProperties.VulnDescription, true, false));
			map3.add(new MapItem("text",DataProperties.VulnDescription, false, false));
			map3.add(new MapItem("br",DataProperties.VulnDescription, false, false));
			map3.add(new MapItem("innerText",DataProperties.VulnDescription, false, false));
			
			
			map3.add(new MapItem("fixRecommendations",DataProperties.None, true, false));
			map3.add(new MapItem("fixRecommendation",DataProperties.Recommendation, true, false));
			map3.add(new MapItem("text",DataProperties.Recommendation, false, false));
			map3.add(new MapItem("br",DataProperties.Recommendation, false, false));
			map3.add(new MapItem("innerText",DataProperties.Recommendation, false, false));
			
			map3.add(new MapItem("Severity",DataProperties.Severity, false, false));
			
			
			appscan.setMapping(map3);
			appscan.setListname("IssueType");
			
			Map<String,Long>severities3 = new HashMap();
			severities3.put("Informational", 0l);
			severities3.put("Recommended", 1l);
			severities3.put("Low", 2l);
			severities3.put("Medium", 3l);	
			severities3.put("High", 4l);
			severities3.put("Critical", 4l);
			appscan.setMapRating(severities3);
			
			appscan.setReportName("AppScan Legacy XML Template");
			*/
			
			
			
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(burp);
			reports.add(burp);
			em.persist(zap);
			reports.add(zap);
			///em.persist(appscan);
			//reports.add(appscan);
			HibHelper.getInstance().commit();
				
		}
		
		return SUCCESS;
	}
	
	@Action(value="UploadReport")
	public String uploadReport(){
		
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		User user = this.getSessionUser();
		Assessment a = (Assessment) em
				.createNativeQuery("{ 'assessor_id' : " + user.getId() + ", '_id' : " + this.id + "}", Assessment.class)
				.getResultList().stream().findFirst().orElse(null);
		
		if(a == null)
			return SUCCESS;
		ReportMap map = em.find(ReportMap.class, parseType);
		
		if(map == null)
			return SUCCESS;
		
		ParseXML parse = new ParseXML();
		
		List<CustomType> types = (List<CustomType>)em.createQuery("from CustomType").getResultList();
		
		
		List<Vulnerability> vulns = parse.parseXML(file_data, -1l, map.getListname(), map.getMapping(), 
				map.getMapRating(), map.getVulnMap(), map.getDefaultVuln(), 
				map.getCustomFields(), types,false);
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		a.getVulns().addAll(vulns);
		em.persist(a);
		HibHelper.getInstance().commit();
		
		return SUCCESS;
		
	}
	
	@Action(value="AddReportType")
	public String setReportType(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		if(this.reportType == null || this.reportType.trim().equals("")) {
			this._message="Empty Report Name";
			return this.ERRORJSON;
		}
		
		List<ReportMap> maps = em.createQuery("from ReportMap where reportName = :name")
				.setParameter("name", this.reportType).getResultList();
		
		if(maps == null || maps.size() == 0){
			ReportMap map = new ReportMap();
			map.setReportName(this.reportType);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(map);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		}else{
			this._message = "Template Name Exists.";
			return this.ERRORJSON;
		}
		
	}
	
	
	private JSONObject dao2JSON(Object obj, Class cls){
		 JSONObject json = new JSONObject();
		 Method[] declaredMethods = cls.getDeclaredMethods();
		 for (Method dmethod : declaredMethods) {
			 //System.out.println(dmethod.getName());
			 //System.out.println( dmethod.getReturnType().toString());
			 
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
	@Action(value="DeleteReportMapItem")
	public String delateReportMapItem(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		ReportMap map = em.find(ReportMap.class, this.id);
		for(MapItem mi : map.getMapping()) {
			if(mi.getId().longValue() == this.mapid) {
				map.getMapping().remove(mi);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(map);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			}
		}
		return this.SUCCESSJSON;
	}
	@Action(value="DeleteReportMap")
	public String deleteReportMap() {
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		ReportMap map = em.find(ReportMap.class, this.id);
		if(map != null) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(map);
			HibHelper.getInstance().commit();
		}
		return this.SUCCESSJSON;
	}
	
	@Action(value="UpdateReportMap")
	public String setReportMap(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		if(this.id == -1) {
			if(this.property == null || this.property.trim().equals("")) {
				this._message="Empty Report Name";
				return this.ERRORJSON;
			}
			ReportMap map = new ReportMap();
			map.setReportName(this.property.trim());
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(map);
			HibHelper.getInstance().commit();
			
		}else {
			if(this.property == null || this.property.trim().equals("")) {
				this._message="Empty Report Name";
				return this.ERRORJSON;
			}
			ReportMap map = em.find(ReportMap.class, this.id);
			
			List<ReportMap> maps = em.createQuery("from ReportMap").getResultList();
			if(maps.stream().anyMatch(rm -> !rm.getReportName().equals(map.getReportName()) && rm.getReportName().equals(this.property.trim()))){
				this._message="Template Name Exists";
				return this.ERRORJSON;
			}
			
			if(this.mapid == null) {
				
				map.setListname(this.attr);
				map.setReportName(this.property);
				map.setDefaultVuln(em.find(DefaultVulnerability.class, this.defaultVuln));
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(map);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			}
			
			if(map.getMapping() == null){
				map.setMapping(new ArrayList());
			}
			
			DataProperties prop = DataProperties.getProp(Long.parseLong(this.property));
			if(this.mapid.longValue() == -1l) {
				map.getMapping().add(new MapItem(this.attr, prop, hasElements, isBase64)); 
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(map);
				HibHelper.getInstance().commit();
			}else {
				MapItem mi = map.getMapping().stream().filter(m -> m.getId().longValue() == this.mapid).findFirst().orElse(null);
				mi.setBase64(this.isBase64);
				mi.setRecursive(this.hasElements);
				mi.setParam(this.attr);
				mi.setProp(prop);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(mi);
				HibHelper.getInstance().commit();
				
			}
		}
		
		return this.SUCCESSJSON;
	}
	
	@Action(value="UploadVulnMap")
	public String setVulnMap() throws IOException{
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		ReportMap map = em.find(ReportMap.class, this.id);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		for(int i = map.getVulnMap().size()-1; i>=0; i--) {
			map.getVulnMap().remove(i);
		}
	
		CSVReader reader = null;
		reader = new CSVReader(new FileReader(this.file_data));
		String[] line;
		//Skip first line
		reader.readNext();
		
        while ((line = reader.readNext()) != null) {
        	String source = line[0];
        	String target = line[1];
        	if(source == "" || target == "")
        		continue;
        	
        	//TODO: input validation needs to happen here
        		
        	DefaultVulnerability dv = (DefaultVulnerability) em.createQuery("from DefaultVulnerability where name = :name" )
	        	.setParameter("name", target).getResultList()
	        	.stream().findFirst().orElse(null);
        	if(dv == null)
        		continue;
        	
			
			VulnMap vulnmap = new VulnMap();
			vulnmap.setOriginTitle(source);
			vulnmap.setTargetVuln(dv);
			//em.persist(vulnmap);
			map.getVulnMap().add(vulnmap);
			
		
			
			
			
			
        }
        em.persist(map);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
        //return this.SUCCESSJSON;
	}
	

	@Action(value="UploadSevMap")
	public String UploadSevMap() throws IOException{
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		ReportMap map = em.find(ReportMap.class, this.id);
		if(map == null)
			return this.ERRORJSON;
		
		if(map.getVulnMap() == null)
			map.setVulnMap(new ArrayList());
		
	
		CSVReader reader = null;
		reader = new CSVReader(new FileReader(this.file_data));
		String[] line;
		//Skip first line
		reader.readNext();
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
        while ((line = reader.readNext()) != null) {
        	String source = line[0];
        	String target = line[1];
        	if(source == "" || target == "")
        		continue;
        	
        	Long rate = map.getMapRating().get(source);
        	map.getMapRating().put(source, Long.parseLong(target));
        	
        }
        em.persist(map);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
        //return this.SUCCESSJSON;
	}
	
	
	@Action(value="VulnMap", results={
			@Result(name="vulnMap",type = "stream"
					, params = {
							"contentType", "text/csv", 
					        "inputName", "stream", 
							"contentDisposition", "attachment;filename=\"vulnmap.csv\""})})
	public String getVulnMapCSV() {
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		ReportMap map = em.find(ReportMap.class, this.id);
		if(map.getVulnMap() == null)
			vulnMap = new ArrayList();
		else
			vulnMap = map.getVulnMap();
		StringWriter swriter = new StringWriter();
		CSVWriter write = new CSVWriter(swriter);


		write.writeNext(new String [] {"Source Vuln Name", "Target Vuln Name"});
		for(VulnMap vulnmap : map.getVulnMap()) {
			write.writeNext(new String[] {vulnmap.getOriginTitle(), vulnmap.getTargetVuln().getName()});
			
		}
		stream = new ByteArrayInputStream( swriter.toString().getBytes());
		return "vulnMap";
		
	}
	
	
	@Action(value="SevMap", results={
			@Result(name="sevMap",type = "stream"
					, params = {
							"contentType", "text/csv", 
					        "inputName", "stream", 
							"contentDisposition", "attachment;filename=\"sevmap.csv\""})})
	public String getSevMapCSV() {
		if(!(this.isAcadmin() || this.isAcmanager()))
			return "login";
		
		ReportMap map = em.find(ReportMap.class, this.id);
		if(map == null)
			return this.ERRORJSON;
		
		if(map.getMapRating()== null)
			sevMap = new HashMap();
		else
			sevMap = map.getMapRating();
		
		
		StringWriter swriter = new StringWriter();
		CSVWriter write = new CSVWriter(swriter);


		write.writeNext(new String [] {"Source Rating Name", "Target Rating Number"});
		for(String key : map.getMapRating().keySet()) {
			write.writeNext(new String[] {key, ""+map.getMapRating().get(key).longValue()});
			
		}
		stream = new ByteArrayInputStream( swriter.toString().getBytes());
		
		
		return "sevMap";
		
	}
	

	public void setFile_data(File file_data) {
		this.file_data = file_data;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public void setParseType(Long parseType) {
		this.parseType = parseType;
	}
	public void setParamid(int paramid) {
		this.paramid = paramid;
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}
	public void setHasElements(boolean hasElements) {
		this.hasElements = hasElements;
	}
	public void setBase64(boolean isBase64) {
		this.isBase64 = isBase64;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public void setVulnName(String vulnName) {
		this.vulnName = vulnName;
	}
	public void setVulnId(Long vulnId) {
		this.vulnId = vulnId;
	}
	public List<ReportMap> getReports() {
		return reports;
	}

	public ReportMap getCurrentMap() {
		return currentMap;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DataProperties[] getProps() {
		return props;
	}

	public String getVulnJson() {
		return vulnJson;
	}

	public Long getId() {
		return id;
	}

	public void setMapid(Long mapid) {
		this.mapid = mapid;
	}

	public List<VulnMap> getVulnMap() {
		return vulnMap;
	}

	public Map<String, Long> getSevMap() {
		return sevMap;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public String getActiveRConfig() {
		return activeRConfig;
	}

	public void setDefaultVuln(Long defaultVuln) {
		this.defaultVuln = defaultVuln;
	}
	
	
	
	
	
	
	
	
	
	

	
}
