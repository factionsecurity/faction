package com.fuse.actions.scheduling;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Query;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Campaign;
import com.fuse.dao.Comment;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.Files;
import com.fuse.dao.FinalReport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Note;
import com.fuse.dao.OOO;
import com.fuse.dao.PeerReview;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.faction.extender.AssessmentManager;
import com.fuse.extenderapi.Extensions;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.FSUtils;
import com.opensymphony.xwork2.ActionContext;


@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/engagement/Engagement.jsp")
public class Engagement  extends FSActionSupport{
	
	private List<CustomType> custom;
	private List<User> users;
	private List<User> eng_users;
	private List<User> asmt_users;
	private List<Teams>teams;
	private String action="";
	private Date sdate;
	private Date edate;
	private List<User>assessors = new ArrayList<User>();
	private String appid="-1";
	private String appName;
	private List<Integer> assessorId;
	private int remId;
	private int engId=-1;
	private List<AssessmentType> assessmentTypes;
	private Integer type;
	private String assessorName;
	private List<Assessment> assessments;
	private List<Campaign> campaigns;
	private Integer selectedTeam;
	private String notes;
	private String distro;
	private Long campId;
	private String message;
	private String cf;
	private Long unitTestId;
	///Used for unit testing
	public boolean forceDelete = false;
	public Integer length;
	public Integer start=0;
	public Integer count;
	public String search;
	private Boolean randId = true;
	private List<String> ratings;
	private String ratingName;
	private String statusName;
	private String defaultRating;
	private String back;
	private List<Object> order;
	

	
	@Action(value="Engagement", results={
			@Result(name="assessorJSON",location="/WEB-INF/jsp/engagement/dateSearchJson.jsp"),
			@Result(name="searchJSON",location="/WEB-INF/jsp/engagement/assessmentSearchJson.jsp"),
			@Result(name="messageJSON",location="/WEB-INF/jsp/engagement/messageJSON.jsp")
		})
	public String execute() throws ParseException{
		if(!(this.isAcengagement() || this.isAcmanager())){
			return AuditLog.notAuthorized(this, "User is not Engagment or Manager", true);
		}
		custom = em.createQuery("from CustomType where type = 0 and (deleted IS NULL or deleted = false)").getResultList();
		users = em.createQuery("from User").getResultList();
		teams = em.createQuery("from Teams").getResultList();
		assessmentTypes = em.createQuery("from AssessmentType").getResultList();
		campaigns = em.createQuery("from Campaign").getResultList();
		SystemSettings ss = (SystemSettings)em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
		if(ss.getEnableRandAppId() != null)
			this.randId = ss.getEnableRandAppId();
		else
			this.randId = true;
		this.ratings = ss.getRatings();
		this.defaultRating = ss.getDefaultRating();
		
		eng_users = new ArrayList<User>();
		asmt_users = new ArrayList<User>();
		for(User u : users){
			if(u.getPermissions() != null && u.getPermissions().isAssessor())
				asmt_users.add(u);
			if(u.getPermissions() != null && u.getPermissions().isEngagement())
				eng_users.add(u);
		}
		if(action.equals("")) {
			ServletActionContext.getRequest().getSession().setAttribute("Files",null);
		}
		
		if(action!= null && action.equals("dateSearch")){
			List<Assessment> asmts = em
					.createQuery("from Assessment as a where (a.start >= :start and a.start <= :end) or (a.end >= :start and a.end <= :end)")
					.setParameter("start", this.sdate)
					.setParameter("end", this.edate)
					.getResultList();
			List<OOO> ooos = em
					.createQuery("from OOO as a where (a.start >= :start and a.start <= :start) or (a.end >= :start and a.end <= :end)")
					.setParameter("start", this.sdate)
					.setParameter("end", this.edate)
					.getResultList();
			List<Verification> verifications = em
					.createQuery("from Verification as a where (a.start >= :start and a.start <= :start) or (a.end >= :start and a.end <= :end)")
					.setParameter("start", this.sdate)
					.setParameter("end", this.edate)
					.getResultList();
					
			for(User u : users){
				for(Assessment a : asmts){
					for(User hacker : a.getAssessor()){
						if(u.getId() == hacker.getId()){
							u.setAssessmentCount(u.getAssessmentCount()+1);
						}
					}
				}
				for(OOO o : ooos){
					if(u.getId() == o.getUser().getId()){
						u.setAssessmentCount(u.getAssessmentCount()+1);
					}
				}
				for(Verification v : verifications) {
					if(u.getId() == v.getAssessor().getId()) {
						u.setAssessmentCount(u.getAssessmentCount() +1);
					}
				}
				if(u.getTeam() != null && u.getTeam().getId().longValue() == this.selectedTeam.longValue() && u.getPermissions() != null && u.getPermissions().isAssessor())
					assessors.add(u);
			}
			
			
			return "assessorJSON";
		}else if(action!= null && action.equals("createAssessment")){

			if(!this.testToken(false)) {
				return this.ERRORJSON;
			}
			
			//Check if a current assessment is assigned
			List<Assessment> asmts = em.createQuery("from Assessment a where a.start = :start and a.end = :end")
					.setParameter("start", sdate)
					.setParameter("end", edate)
					.getResultList();
			
			List<User>hackers = new ArrayList<User>();
			if(assessorId != null) {
				
				for(Integer aid : assessorId){
					User assessor = em.find(User.class, (long)aid);
					hackers.add(assessor);
				}
			
			
				for(Assessment a : asmts){
					if(a.getAssessor().containsAll(hackers) && a.getName().equals(appName)){
						this._message = "Assessment has already been assigned. Choose different dates.";
						return this.ERRORJSON;
					}
					
				}
			}
			if(hackers.size() == 0) {
				this._message = "Assessment Must Have an Assessor";
				return this.ERRORJSON;
			}
			
			if(this.appid == null || this.appid.equals("")) {
				this._message = "Application Id is Missing";
				return this.ERRORJSON;
			}
			if(this.appName == null || this.appName.equals("")) {
				this._message = "Application Name is Missing";
				return this.ERRORJSON;
			}
			
			if(sdate == null || edate == null)
			 {
				this._message = "Start and End Dates Could Be Missing";
				return this.ERRORJSON;
			}
			if(ratingName != null && !ratingName.trim().equals("")) {
				
				if(!ss.getRatings().stream().anyMatch(s -> s.equals(this.ratingName.trim()))) {
					this._message = "Rating is not defined.";
					return this.ERRORJSON;
				}
			}
			User remediation = em.find(User.class, (long)remId);
			User engagement = em.find(User.class, (long)engId);
			Campaign camp = em.find(Campaign.class, (long)campId);
			AssessmentType Type = em.find(AssessmentType.class, (long)type);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			
			Assessment am = new Assessment();
			am.setWorkflow(0);
			am.setAppId(appid);
			am.setName(appName);
			am.setStart(sdate);
			am.setEnd(edate);
			am.setEngagement(engagement);
			am.setRemediation(remediation);
			am.setAssessor(hackers);
			am.setType(Type);
			am.setDistributionList(this.distro);
			am.setAccessNotes(this.notes);
			am.setCampaign(camp);
			Note defaultNote = new Note();
			defaultNote.setName("default");
			defaultNote.setCreated(new Date());
			defaultNote.setUpdated(new Date());
			am.addNoteToList(defaultNote);
			
			JSONArray cfstuff = new JSONArray();
			if(this.cf != null){
				JSONParser parse = new JSONParser();
				JSONArray array = (JSONArray) parse.parse(cf);
				am.setCustomFields(new ArrayList());
				
				for(int i=0; i < array.size(); i++){
					JSONObject json = (JSONObject) array.get(i);
					CustomField cf = new CustomField();
					
					Long cfid = Long.parseLong(""+json.get("id"));
					CustomType ct = em.find(CustomType.class, cfid);
					cf.setType(ct);
					cf.setValue(""+json.get("text"));
					am.getCustomFields().add(cf);
				}
				
			}
			Map<String, Files> files = null;
			try{
					HttpSession sess =  this.request.getSession();
					files = (Map<String, Files>) sess.getAttribute("Files");
					sess.setAttribute("Files",null);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			
			em.persist(am);
			AuditLog.audit(this, "Assessment Created" , AuditLog.UserAction,
					AuditLog.CompAssessment, am.getId(), false);
			
			
			
			
			if(files !=  null){
				for(Files f : files.values()){
					f.setCreator(this.getSessionUser());
					f.setEntityId(am.getId());
					f.setType(Files.ASSESSMENT);
					//session.save(f);
					em.persist(f);
				}
				
			}
			HibHelper.getInstance().commit();
			this.unitTestId = am.getId();
			//session.getTransaction().commit();
			String email = "<h2> New Assessment: " + am.getName() + "[ " + am.getAppId() + " ] </h2>";
			email += "<p> The above assessment has been added to your Assessment Queue. <br>"
					+ "The assessment begins " + am.getStart() + ".</p>";
				
			
			EmailThread emailThread = new EmailThread(am, "New Assessment Assigned to You", email);
			TaskQueueExecutor.getInstance().execute(emailThread);
			
			// Run All extensions
			Extensions amgr = new Extensions(Extensions.EventType.ASMT_MANAGER);
			amgr.execute(am, AssessmentManager.Operation.Create);
			
			return this.SUCCESSJSON;
			
			
		}else if(action != null && action.equals("search") ){
			String comma = "";
			boolean first=true;
			String mongoQuery = "{ ";
			if(appName != null && !appName.equals("") && !appName.equals("-1")){
				if (first )
					first = false;
				mongoQuery += " 'name' : { '$regex' : '.*" + FSUtils.sanitizeMongo(this.appName )+".*', '$options':'si'} ";
			}
			if(assessorId != null && assessorId.size() ==1 && assessorId.get(0) !=null){
				if( !first ){
					comma = ",";
				}else
					first = false;
				String array = " ";
				for(Integer aid : assessorId){
					array += "" + aid + " ";
				}
				array+="";
				mongoQuery += comma + " 'assessor' : " + array + " ";
				
			}
			if(engId != -1 ){

				if( !first ){
					comma = ",";
				}else
					first = false;
				mongoQuery += comma + " 'engagement_Id' : " + this.engId + " ";
				
			}
			if(appid != null && !appid.equals("")){
				if( !first ){
					comma = ",";
				}else
					first = false;
				mongoQuery += comma + " 'appId' : '" + this.appid + "' ";
			}
			if(statusName != null && !statusName.equals("")) {
				if( !first ){
					comma = ",";
				}else
					first = false;
				if(statusName.equals("Open")) {
					mongoQuery += comma + " 'completed' : { '$exists': false } ";
					
				}else if(statusName.equals("Completed")) {
					mongoQuery += comma + " 'completed' : { '$exists': true } ";
					
				}
				
			}
			
			String dir = request.getParameter("order[0][dir]");
			
			String colNum = request.getParameter("order[0][column]");
			mongoQuery += " }";
			//EntityManager em = HibHelper.getEM();
			String CountQuery = "db.Assessment.count(" + mongoQuery + ")";
			this.count = ((Long)em.createNativeQuery(CountQuery).getSingleResult()).intValue();
			String sortedQuery = "db.Assessment.find({ '$query' :" + mongoQuery + ", "
					+ " '$orderby': { '" + convertColNumToName(colNum) +"' : " + convertDir(dir) + " } })";
			
			
			this.assessments = (List<Assessment>)em.createNativeQuery(sortedQuery, Assessment.class)
					.setMaxResults(this.length)
					.setFirstResult(this.start)
					.getResultList();
			return "searchJSON";
			
		}else if(action != null && action.equals("delete") ){
			if(!this.testToken(false)) {
				return this.ERRORJSON;
			}
			Assessment a = em.find(Assessment.class, Long.parseLong(this.appid));
			if(!this.forceDelete){
				//Check if the assessment is finalized
				if(a.getCompleted() != null && a.getCompleted().getTime() != 0){
					this.message="Cannot Delete a Finalized Assessment";
					
					return "messageJSON";
				}
				//Check if the assessment has any already assigned vulns
				if(a.getVulns() != null && a.getVulns().size() != 0){
					this.message = "Cannot Delete an Assessment that Contains Vulnerabilities";
					return "messageJSON";
				}
			}
			//Check if the assessment is in PeerReview
			//EntityManager em = HibHelper.getEM();
			PeerReview pr = (PeerReview)em
					.createNativeQuery("{assessment_id : " + this.appid + " }", PeerReview.class)
					.getResultList().stream().findFirst().orElse(null);
			// Check if PR is completed
			if(!this.forceDelete){
				if(pr != null && pr.getCompleted() != null && pr.getCompleted().getTime() == 0){
					this.message = "Connot Delete an Assessment that is in Peer Review";
					return "messageJSON";
				}
			}
			// PR is completed so we can delete the PR too.
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			if(pr != null && pr.getCompleted() != null && pr.getCompleted().getTime() != 0){
				//em.getTransaction().begin();
				if(pr.getComments() != null && pr.getComments().size() != 0){
					List<Comment> cmts = pr.getComments();
					for(Comment cmt : cmts){
						em.remove(cmt);
					}
				}
				em.remove(pr);
				//em.getTransaction().commit();
			}
			
			//Delete the assessment
			
			FinalReport f= a.getFinalReport();
			if(f!= null){
				em.remove(f);
			}
			
			
			
			
			AuditLog.audit(this, "Assessment " + a.getAppId() + " " + a.getName() 
			+ " Deleted" , AuditLog.UserAction, AuditLog.CompAssessment, a.getId(), false);

			em.createNativeQuery("db.Assessment.remove({ '_id': " + a.getId() + " })").executeUpdate();
			HibHelper.getInstance().commit();
			
			Extensions amgr = new Extensions(Extensions.EventType.ASMT_MANAGER);
			amgr.execute(a, AssessmentManager.Operation.Delete);
			
			return SUCCESSJSON;
				
			
		}
		
		//session.close();
		return SUCCESS;
	}
	
	private String convertColNumToName(String number) {
		switch(number) {
			case "0": return "appId";
			case "1": return "name";
			case "2": return "status";
			case "3": return "assessor";
			case "4": return "type";
			case "5": return "campaign";
			case "6": return "start";
			case "7": return "end";
			case "8": return "completed";
			default: return "appId";
		}
	}
	private String convertDir(String dir) {
		switch(dir) {
			case "desc": return "-1";
			case "asc": return "1";
			default: return "-1";
		}
	}

	
	
	
	
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public String getActiveEngagement() {
		return "active";
	}
	
	
	public List<CustomType> getCustom(){
		//TODO fix this later
		return custom;
	}
	public List<User> getEngagement(){
		List<User> eng = new ArrayList<User>();
		for(User u : users){
			if(u.getPermissions().isEngagement()){
				eng.add(u);
			}
		}
		return eng;
	}
	
	public List<User> getRemediation(){
		List<User> rem = new ArrayList<User>();
		for(User u : users){
			if(u.getPermissions().isRemediation()){
				rem.add(u);
			}
		}
		return rem;
	}
	public List<Teams> getTeams() {
		return teams;
	}
	public void setTeams(List<Teams> teams) {
		this.teams = teams;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Date getSdate() {
		return sdate;
	}
	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getEdate() {
		return edate;
	}
	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public List<User> getAssessors() {
		return assessors;
	}
	public void setAssessors(List<User> assessors) {
		this.assessors = assessors;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public List<Integer>getAssessorId() {
		return assessorId;
	}
	public void setAssessorId(List<Integer> assessorId) {
		this.assessorId = assessorId;
	}
	public int getRemId() {
		return remId;
	}
	public void setRemId(int remId) {
		this.remId = remId;
	}
	public int getEngId() {
		return engId;
	}
	public void setEngId(int engId) {
		this.engId = engId;
	}
	public List<AssessmentType> getAssessmentTypes() {
		return assessmentTypes;
	}
	public void setAssessmentTypes(List<AssessmentType> assessmentTypes) {
		this.assessmentTypes = assessmentTypes;	
	}
	public int getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getAssessorName() {
		return assessorName;
	}
	public void setAssessorName(String assessorName) {
		this.assessorName = assessorName;
	}
	/*public void setAppid(Integer appid) {
		this.appid = appid;
	}*/
	public List<Assessment> getAssessments() {
		return assessments;
	}
	public void setAssessments(List<Assessment> assessments) {
		this.assessments = assessments;
	}
	public Integer getSelectedTeam() {
		return selectedTeam;
	}
	public void setSelectedTeam(Integer selectedTeam) {
		this.selectedTeam = selectedTeam;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getDistro() {
		return distro;
	}
	public void setDistro(String distro) {
		
		this.distro = distro;
	}
	public List<Campaign> getCampaigns() {
		return campaigns;
	}
	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}
	public Long getCampId() {
		return campId;
	}
	public void setCampId(Long campId) {
		this.campId = campId;
	}
	public String getMessage() {
		return message;
	}

	public void setCf(String cf) {
		this.cf = cf;
	}
	

	public List<User> getEng_users() {
		return eng_users;
	}

	public List<User> getAsmt_users() {
		return asmt_users;
	}

	public Long getUnitTestId() {
		return unitTestId;
	}



	public Integer getCount() {
		return count;
	}







	public void setCount(Integer count) {
		this.count = count;
	}





	public void setSearch(String search) {
		this.search = search;
	}
	
	public void setOrder(List<Object> order) {
		this.order = order;
	}





	public Boolean getRandId() {
		return randId;
	}
	
	public void validate() {
		if(this.distro == null || this.distro.trim().equals(""))
			return;
		
		//String emailRegex = "^['_a-z0-9-\\+]+(\\.['_a-z0-9-\\+]+)?@[a-z0-9-]+(\\.[a-z0-9-]+)?\\.[a-z]{2,6}$";
		String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		Pattern p = Pattern.compile(emailRegex);
		String [] emails = this.distro.split(";");
		for(String mail : emails) {
			Matcher m = p.matcher(mail.trim());
			if(m.matches())
				continue;
			else
				addActionError("Email Address is not Valid");
		}
		
	}

	public List<String> getRatings() {
		return ratings;
	}

	public void setRatingName(String ratingName) {
		this.ratingName = ratingName;
	}

	public String getDefaultRating() {
		return defaultRating;
	}
	
	public String getBack() {
		return back;
	}
	
	public void setBack(String back) {
		this.back = back;
	}
	
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

}
