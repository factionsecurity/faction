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
				try {
					createAssessment(csv,em);
				} catch (org.json.simple.parser.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            	
	            PrintWriter out = response.getWriter();
	            String json = "{\"initialPreviewConfig\" : [{ \"caption\": \"Success\", \"width\" : \"100px\", \"key\" : 1}]}";
	            out.println(json);
	            
		
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
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
	
	protected void createAssessment(String csv, EntityManager em) throws NumberFormatException, IOException, ParseException, org.json.simple.parser.ParseException{
		CSVReader reader = null;
		reader = new CSVReader(new StringReader(csv));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String[] line;
		HibHelper.getInstance().preJoin();
		//Skip first line
		reader.readNext();
        while ((line = reader.readNext()) != null) {
        	String appid = line[0];
        	String appName = line[1];
        	Date startDate=sdf.parse(line[2]);
        	Integer days = Integer.parseInt(line[3]);
        	String type=line[4];
        	String names=line[5];
        	String camp=line[6];
        	String custom=line[7];
        	JSONParser parse = new JSONParser();
        	
        	JSONObject json = (JSONObject) parse.parse(custom.replaceAll("'", "\""));
        	
        	
        	Assessment asmt = getOrCreateAssessmentByAppID(appid, appName, em);
        	
        	//Merge data with existing assessment information
        	if(asmt != null){
        		//Add assessors to the assessment
        		List<User>team = new ArrayList();
            	for(String name : names.split(";")){
	            	User a = (User)em.createQuery("from User where fname = :fname and lname= :lname")
	            			.setParameter("fname", name.split(" ")[0])
	            			.setParameter("lname", name.split(" ")[1])
	            			.getResultList().stream().findFirst().orElse(null);
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
	            	CustomType ct = (CustomType)em.createQuery("from CustomType where variable = :value")
							.setParameter("value", (String)key).getResultList().stream().findFirst().orElse(null);
	            	
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
            	
            	try{
            		em.persist(asmt);
            	}catch(Exception ex){
            		ex.printStackTrace();
            	}
            	
            	
        	}
        	
        	
        }
        HibHelper.getInstance().commit();

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
