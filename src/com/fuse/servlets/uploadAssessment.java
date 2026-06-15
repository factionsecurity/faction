package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.Campaign;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.faction.elements.results.InventoryResult;
import com.fuse.extenderapi.Extensions;
import com.opencsv.CSVReader;




/**
 * Servlet implementation class fileUpload
 */
@MultipartConfig
public class uploadAssessment extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private boolean isMultipart;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public uploadAssessment() {
        super();
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/json");
		
		User user = (User)request.getSession().getAttribute("user");
		
		if(user == null || !(user.getPermissions().isEngagement() || user.getPermissions().isManager() || user.getPermissions().isAdmin())){
			return;
		}
		

		isMultipart = ServletFileUpload.isMultipartContent(request);
		if(isMultipart){   //Must be a file upload
			EntityManager em =HibHelper.getInstance().getEMF().createEntityManager();
			try{
				 
				
				Part filePart = request.getPart("file_data");
				
				byte [] bytes = new byte[(int) filePart.getSize()];
				filePart.getInputStream().read(bytes);
				
				String csv = new String(bytes);
				JSONObject result = createAssessment(csv, em);

	            PrintWriter out = response.getWriter();

	            // Keep the bootstrap-fileinput preview config so the widget renders the upload as complete.
	            JSONObject previewConfig = new JSONObject();
	            previewConfig.put("caption", "Success");
	            previewConfig.put("width", "100px");
	            previewConfig.put("key", 1);
	            JSONArray previewConfigList = new JSONArray();
	            previewConfigList.add(previewConfig);

	            JSONObject json = new JSONObject();
	            json.put("initialPreviewConfig", previewConfigList);
	            json.put("added", result.get("added"));
	            json.put("errors", result.get("errors"));
	            json.put("warnings", result.get("warnings"));

	            out.println(json.toJSONString());

		
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				em.close();
			}
		}
				
		
	}
	private String getFileName(Part p){
		String header = p.getHeader("Content-Disposition");
		String[] headers = header.split(";");
		for(String item : headers){
			if(item.trim().startsWith("filename="))
				return item.replace("filename=", "").replace("\"", "").trim();
		}
		return "";
	}
	
	protected  JSONObject createAssessment(String csv, EntityManager em) throws IOException {
		JSONArray added = new JSONArray();
		JSONArray errors = new JSONArray();
		JSONArray warnings = new JSONArray();

		CSVReader reader = new CSVReader(new StringReader(csv));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String[] line;
		HibHelper.getInstance().preJoin();
		//Skip first line (header). Data rows start at line 2.
		reader.readNext();
		int rowNum = 1;
        while ((line = reader.readNext()) != null) {
        	rowNum++;

        	// Ignore fully blank lines without reporting them as errors.
        	if(isBlankRow(line))
        		continue;

        	try {
        		if(line.length < 8)
        			throw new IllegalArgumentException("Row has " + line.length + " column(s); 8 are required "
        					+ "(App ID, App Name, Start Date, Days, Type, Assessors, Campaign, Custom Fields).");

	        	String appid = line[0];
	        	String appName = line[1];
	        	Date startDate;
	        	try {
	        		startDate = sdf.parse(line[2]);
	        	} catch (ParseException pe) {
	        		throw new IllegalArgumentException("Invalid start date '" + line[2] + "'. Expected format yyyyMMdd (e.g. 20240131).");
	        	}
	        	Integer days;
	        	try {
	        		days = Integer.parseInt(line[3].trim());
	        	} catch (NumberFormatException nfe) {
	        		throw new IllegalArgumentException("Invalid number of days '" + line[3] + "'. Expected a whole number.");
	        	}
	        	String type=line[4];
	        	String names=line[5];
	        	String camp=line[6];
	        	String custom=line[7];
	        	JSONParser parse = new JSONParser();

	        	JSONObject json;
	        	try {
	        		json = (JSONObject) parse.parse(custom.replaceAll("'", "\""));
	        	} catch (org.json.simple.parser.ParseException jpe) {
	        		throw new IllegalArgumentException("Invalid custom fields JSON '" + custom + "'. Expected an object such as {'field':'value'}.");
	        	}


	        	Assessment asmt = getOrCreateAssessmentByAppID(appid, appName, em);

	        	// Collect non-fatal issues for this row. These do NOT prevent the assessment from
	        	// being created; the row is still saved and reported as a warning the user can fix.
	        	List<String> rowWarnings = new ArrayList<String>();

	        	//Add assessors to the assessment. Unknown assessors are skipped, not fatal.
	    		List<User>team = new ArrayList();
	        	for(String name : names.split(";")){
	        		name = name.trim();
	        		if(name.isEmpty())
	        			continue;
	        		if(!name.contains(" ")){
	        			rowWarnings.add("Assessor '" + name + "' is not a full name (first and last) - skipped.");
	        			continue;
	        		}
		        	User a = (User)em.createQuery("from User where fname = :fname and lname= :lname")
		            			.setParameter("fname", name.split(" ")[0])
		            			.setParameter("lname", name.split(" ")[1])
		            			.getResultList().stream().findFirst().orElse(null);
		        	if(a == null){
		        		rowWarnings.add("Assessor '" + name + "' was not found as a user - skipped.");
		        		continue;
		        	}
		        	team.add(a);
	        	}
	    		asmt.setAssessor(team);

	    		//Check for a campaign and automatically create it if it does not exist.
	    		Campaign c = (Campaign) em.createQuery("from Campaign where name = :name")
		            		.setParameter("name", camp)
		            		.getResultList().stream().findFirst().orElse(null);
	        	if(c == null){
	        		c = new Campaign();
	        		c.setName(camp);
	        		em.persist(c);
	        	}

	        	asmt.setCampaign(c);

	        	AssessmentType at = (AssessmentType) em.createQuery("from AssessmentType where type = :name")
	        			.setParameter("name", type)
	        			.getResultList().stream().findFirst().orElse(null);
	        	if(at == null) {
	        		AssessmentType newType = new AssessmentType();
	        		newType.setType(type);
	        		em.persist(newType);
	        		asmt.setType(newType);

	        	}else {
	        		asmt.setType(at);
	        	}

	        	//Set assessment startdate and duration
	        	asmt.setStart(startDate);
	        	Calendar cal = Calendar.getInstance();
	        	cal.setTime(startDate);
	        	cal.add(Calendar.DATE, days);
	        	asmt.setEnd(cal.getTime());

	        	//Add Custom Fields
	        	if(asmt.getCustomFields() == null){
	        		asmt.setCustomFields(new ArrayList<CustomField>());
	        	}

	        	for(Object key : json.keySet()){
		        	CustomType ct = (CustomType)em.createQuery("from CustomType where variable = :value and (deleted IS NULL or deleted = false)")
								.setParameter("value", (String)key).getResultList().stream().findFirst().orElse(null);
		        	if(ct==null)
		        		continue;

		        	List<CustomField>fields = asmt.getCustomFields();
		        	boolean found=false;
	        		for(CustomField cf : fields){
	        			if(cf.getType().getVariable().equals(key)){
	        				cf.setValue((String)json.get((String)key));
	        				found=true;
	        				break;
	        			}

	        		}
	        		if(!found){
	    				CustomField cf2 = new CustomField();
			        	cf2.setType(ct);
			        	cf2.setValue((String)json.get((String)key));
			        	asmt.getCustomFields().add(cf2);
	    			}

	        	}

	        	em.persist(asmt);

	        	JSONObject addedRow = new JSONObject();
	        	addedRow.put("id", asmt.getId());
	        	addedRow.put("name", asmt.getName());
	        	addedRow.put("appId", asmt.getAppId());
	        	added.add(addedRow);

	        	// The assessment was saved, but surface any non-fatal issues so they can be fixed.
	        	for(String warning : rowWarnings){
	        		JSONObject warningRow = new JSONObject();
	        		warningRow.put("row", rowNum);
	        		warningRow.put("appId", appid);
	        		warningRow.put("name", asmt.getName());
	        		warningRow.put("message", warning);
	        		warnings.add(warningRow);
	        	}

        	} catch (Exception ex) {
        		ex.printStackTrace();
        		JSONObject errorRow = new JSONObject();
        		errorRow.put("row", rowNum);
        		errorRow.put("appId", line.length > 0 ? line[0] : "");
        		errorRow.put("name", line.length > 1 ? line[1] : "");
        		errorRow.put("message", ex.getMessage() == null ? ex.toString() : ex.getMessage());
        		errors.add(errorRow);
        	}
        }
        HibHelper.getInstance().commit();

        JSONObject result = new JSONObject();
        result.put("added", added);
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
	}

	private boolean isBlankRow(String[] line){
		if(line == null || line.length == 0)
			return true;
		for(String cell : line){
			if(cell != null && !cell.trim().isEmpty())
				return false;
		}
		return true;
	}
	
	private Assessment getOrCreateAssessmentByAppID(String appid, String appName, EntityManager em){
		
		
		//Find and existing assessment
		Assessment asmtHold = (Assessment) em.createQuery("from Assessment where appId = :appid")
				.setParameter("appid", appid)
				.getResultList().stream().findFirst().orElse(null);
		

		Assessment asmt = new Assessment();
		asmt.setVulns(new ArrayList<Vulnerability>());
		
		if(appid == null || appid.trim().equals("")) {
			String randId = "" + (int)((Math.random() * (1000000 - 1000)) + 1000);
			asmt.setAppId(randId);
		}else {
			asmt.setAppId(appid);
		}
		
		
		if(asmtHold != null){
			
			//Detach the object from hibernate
			asmt.setDistributionList(asmtHold.getDistributionList());
			//asmt.setCustomFields(asmtHold.getCustomFields());
			asmt.setEngagement(asmtHold.getEngagement());
			asmt.setRemediation(asmtHold.getRemediation());
			asmt.setName(asmtHold.getName());

			
		}else{
			asmt.setName(appName);
		
			List<CustomType> ctypes = (List<CustomType>) em.createQuery("from CustomType where type = :type")
				.setParameter("type", 0)
				.getResultList();
			//set its custom fields
			for(CustomType ct: ctypes){
				asmt.setCustomFields(new ArrayList());
				CustomField cf = new CustomField();
				cf.setType(ct);
				asmt.getCustomFields().add(cf);
			}
		}
			
				
		//Check the API Extensions for data		
		Extensions appInv = new Extensions(Extensions.EventType.ASMT_MANAGER);
		List<InventoryResult> results = appInv.execute(appid, "");
		
		if(results != null && results.size() == 1){
			InventoryResult ir = results.get(0);
			asmt.setDistributionList(ir.getDistributionList());
			asmt.setName(ir.getApplicationName());
			if(ir.getCustomFields()!= null){
				for(String  key : ir.getCustomFields().keySet()){
					CustomType ct = (CustomType)em.createQuery("from CustomType where variable = :value")
							.setParameter("value", key).getResultList().stream().findFirst().orElse(null);
					if(ct != null){
						boolean found=false;
						if(asmt.getCustomFields() == null)
							asmt.setCustomFields(new ArrayList<CustomField>());
						
						for(CustomField c : asmt.getCustomFields()){
							if(c.getType().getVariable().equals(key)){
								c.setValue(ir.getCustomFields().get(key));
								break;
							}
						}
						if(!found){
							CustomField cf = new CustomField();
							cf.setType(ct);
							cf.setValue(ir.getCustomFields().get(key));
							
							asmt.getCustomFields().add(cf);
						}
					}
				}		
			}
		}
		return asmt;
	}

}
