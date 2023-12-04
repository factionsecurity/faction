package com.fuse.actions.admin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONObject;

import com.fuse.dao.CheckListItem;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.opencsv.CSVReader;
import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.AuditLog;
import com.fuse.dao.CheckList;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/admin/Checklists.jsp")
public class CheckLists extends FSActionSupport{
	
	private List<CheckLists> lists = new ArrayList();
	private Long listitem;
	private Long checklist;
	private String name;
	private String question;
	private List<Long> vulns;
	private File file_data;
    private String contentType;
    private String filename;
    private CheckList check;
    private List<AssessmentType> types = new ArrayList();
    private Integer type;
    private String activeChecklist="active";
    
	
	
	@Action(value="Checklists")
	public String showCheckLists(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true);
		
		User user = this.getSessionUser();
		
		lists = em.createQuery("From CheckList").getResultList();
		types = em.createQuery("from AssessmentType").getResultList();
		return this.SUCCESS;
		
	}
	
	@Action(value="GetCheckList", results={
	     @Result(name="listJSON",location="/WEB-INF/jsp/admin/listJSON.jsp")})
	public String FindChecklist(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true);
		User user = this.getSessionUser();
		
		check = em.find(CheckList.class, checklist);
		return "listJSON";
		
	}
	
	@Action(value="GetTypes", results={
		     @Result(name="typesJSON",location="/WEB-INF/jsp/admin/typesJSON.jsp")})
	public String FindTypes(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		User user = this.getSessionUser();
		
		check = em.find(CheckList.class, checklist);
		return "typesJSON";
		
	}
	
	@Action(value="DeleteChecklist")
	public String deleteCheckList(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true);
		
		User user = this.getSessionUser();

		CheckList cl = (CheckList) em.find(CheckList.class, checklist);
		if(cl == null) {
			this._message = "Checklist does not exist";
			return this.ERRORJSON;
		}
		
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.remove(cl);
		AuditLog.audit(this, "User Deleted a checklist " + cl.getName(), AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
		HibHelper.getInstance().commit();
		
		return this .SUCCESSJSON;

			
		
	}
	
	@Action(value="CreateChecklist")
	public String createCheckList(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager" , true);
		
		if(!this.testToken(false)) {
			return this.ERRORJSON;
		}
		
		if(this.name == null || this.name.trim().equals("")) {
			this._message = "Name cannot be empty";
			return this.ERRORJSON;
		}
		List<CheckList> lists = em.createQuery("from CheckList").getResultList();
		if(lists.stream().anyMatch(l -> l.getName().equals(this.name.trim()))) {
			this._message = "Checklist Exists";
			return this.ERRORJSON;
		}
			
		User user = this.getSessionUser();
		
		CheckList cl = new CheckList();
		cl.setName(this.name.trim());
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(cl);
		AuditLog.audit(this, "User Created a checklist " + cl.getName(), AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
		HibHelper.getInstance().commit();
		
		return this.SUCCESSJSON;
	}
	
	@Action(value="UpdateChecklist")
	public String updateCheckList(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager" , true);
		
		User user = this.getSessionUser();
		if(!this.testToken(false)) {
			return this.ERRORJSON;
		}
		
		if(this.name == null || this.name.trim().equals("")) {
			this._message = "Name cannot be empty";
			return this.ERRORJSON;
		}
		
		
		CheckList cl = (CheckList) em.find(CheckList.class, checklist);
		if(cl == null) {
			this._message = "Checklist does not exist";
			return this.ERRORJSON;
		}
		
		List<CheckList> lists = em.createQuery("from CheckList").getResultList();
		if(lists.stream().anyMatch(l -> l.getName().equals(this.name.trim()))) {
			this._message = "Checklist Exists";
			return this.ERRORJSON;
		}
		
		cl.setName(name.trim());
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(cl);
		AuditLog.audit(this, "User Updated a checklist " + cl.getName(), AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
		HibHelper.getInstance().commit();
		
		return this .SUCCESSJSON;

			
		
	}
	
	@Action(value="AddChecklistQuestion")
	public String addCheckListQuestion(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		if(!this.testToken(false)) {
			return this.ERRORJSON;
		}
		
		User user = this.getSessionUser();
		
		if(checklist == null) {
			this._message = "Checklist does not exist.";
			return this.ERRORJSON;
		}
		
		if(question == null || question.trim().equals("")) {
			this._message = "Question was empty.";
			return this.ERRORJSON;
		}

		CheckList cl = em.find(CheckList.class, checklist);
		if(cl == null) {
			this._message = "Checklist does not exist.";
			return this.ERRORJSON;
		}
		
		
		CheckListItem item = new CheckListItem();
		item.setQuestion(this.question.trim());
		item.setChecklist(cl);
		cl.getQuestions().add(item);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(cl);
		AuditLog.audit(this, "User Added a checklist question to " + cl.getName(), AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
		HibHelper.getInstance().commit();
		JSONObject json = new JSONObject();
		json.put("id", item.getId());
		json.put("token", this.get_token());
		return this.jsonOutput(json.toJSONString());
	}
	
	@Action(value="RemoveChecklistQuestion")
	public String removeCheckListQuestion(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		if(!this.testToken(false)) {
			return this.ERRORJSON;
		}
		
		User user = this.getSessionUser();
		
		CheckList cl = em.find(CheckList.class, checklist);
		if(cl == null)
			return this.ERRORJSON;
		
		for(CheckListItem item : cl.getQuestions()){
			if(item.getId().longValue() == listitem){
				cl.getQuestions().remove(item);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(cl);
				AuditLog.audit(this, "User Removed a checklist question in  " + cl.getName(), AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
				HibHelper.getInstance().commit();
				
				return this.SUCCESSJSON;
			}
		}
		return this.ERRORJSON;
		
	}
	
	@Action(value="UpdateChecklistQuestion")
	public String updateCheckListQuestion(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		if(!this.testToken(false)) {
			return this.ERRORJSON;
		}
		if(question == null || question.trim().equals("")) {
			this._message = "Question was empty.";
			return this.ERRORJSON;
		}
		User user = this.getSessionUser();
		
		CheckListItem item = em.find(CheckListItem.class, listitem);
		if(item == null)
			return this.ERRORJSON;
		
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		item.setQuestion(question.trim());
		em.persist(item);
		AuditLog.audit(this, "User Updated a checklist question for " + item.getChecklist().getName() , 
				AuditLog.UserAction,AuditLog.CompChecklist, item.getChecklist().getId(), true);
		HibHelper.getInstance().commit();
		
		return this.SUCCESSJSON;
			

		
	}
	
	@Action(value="MapVulns")
	public String mapVulns(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager" , true);
		
		User user = this.getSessionUser();
		
		CheckList cl = em.find(CheckList.class, checklist);
		if(cl == null)
			return this.ERRORJSON;
		
		for(CheckListItem item : cl.getQuestions()){
			if(item.getId().longValue() == listitem){
				for(Long vid : this.vulns){
					DefaultVulnerability dv = em.find(DefaultVulnerability.class, vid);
					if(dv == null)
						return this.ERRORJSON;
					
					item.getMappingVulns().add(dv);
				}
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(cl);
				AuditLog.audit(this, "User mapped vulns in  " + cl.getName() , AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
				HibHelper.getInstance().commit();
				
				return this.SUCCESSJSON;
			}
		}
		return this.ERRORJSON;
		
	}
	@Action(value="ExportChecklist",
			results={
				     @Result(name="listCSV",location="/WEB-INF/jsp/admin/listCSV.jsp")})
	public String exportCheckList(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		User user = this.getSessionUser();
		CheckList cl = em.find(CheckList.class, this.checklist);
		if(cl == null)
			return this.ERRORJSON;
		
		this.check = cl;
		AuditLog.audit(this, "User exported checklist  " + cl.getName() , AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), true);
		return "listCSV";
	}
	
	@Action(value="UploadChecklist")
	public String uploadCheckList() throws IOException{
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		User user = this.getSessionUser();
		
		CSVReader reader = null;
		reader = new CSVReader(new FileReader(this.file_data));
		
		String[] line;
		
		//Skip first line
		reader.readNext();
		CheckList cl = em.find(CheckList.class, this.checklist);
		if(cl == null)
			return this.ERRORJSON;
		//List<CheckListItem> items = new ArrayList();
        while ((line = reader.readNext()) != null) {
			if(line[0].equals("")){
				continue;
			}
        	Long id = Long.parseLong(line[0]);
        	if(id == 0l){
        		CheckListItem item = new CheckListItem();
        		item.setQuestion(line[1]);
        		cl.getQuestions().add(item);
        	}else{
        		boolean found=false;
        		for(CheckListItem item : cl.getQuestions()){
        			if(item.getId() != null && item.getId().longValue() == Long.parseLong(line[0])){
        				item.setQuestion(line[1]);
        				found=true;
        				break;
        			}
        		}
        		if(!found){
        			CheckListItem item = new CheckListItem();
            		item.setQuestion(line[1]);
            		cl.getQuestions().add(item);
        		}
        		
        	}
        }
        HibHelper.getInstance().preJoin();
        em.joinTransaction();
        em.persist(cl);
        AuditLog.audit(this, "User uploaded a checklist  for " + cl.getName() , AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
        HibHelper.getInstance().commit();
       
        return this.SUCCESSJSON;
	}
	
	@Action(value="ToggleType")
	public String addType(){
		if(!(this.isAcadmin() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "Not an Admin or Manager", true );
		
		if(!this.testToken(false)) {
			return this.ERRORJSON;
		}
		
		User user = this.getSessionUser();
		
		CheckList cl = em.find(CheckList.class, checklist);
		if(cl == null){
		
			return this.ERRORJSON;
		}
		
		
		if(cl.getTypes() == null)
			cl.setTypes(new ArrayList());
		
		if(cl.getTypes().contains(this.type)){
				cl.getTypes().remove(this.type);
		}else{
			cl.getTypes().add(this.type);
		}
		HibHelper.getInstance().preJoin();
        em.joinTransaction();
        em.persist(cl);
        AuditLog.audit(this, "User changed checklist type in  " + cl.getName() , AuditLog.UserAction, AuditLog.CompChecklist, cl.getId(), false);
        HibHelper.getInstance().commit();
        
		return this.SUCCESSJSON;
		
	}
	
	
	
	public boolean isActiveCL(){
		return true;
	}

	public List<CheckLists> getLists() {
		return lists;
	}

	public Long getListitem() {
		return listitem;
	}

	public Long getChecklist() {
		return checklist;
	}

	public String getName() {
		return name;
	}

	public String getQuestion() {
		return question;
	}

	public List<Long> getVulns() {
		return vulns;
	}

	public void setLists(List<CheckLists> lists) {
		this.lists = lists;
	}

	public void setListitem(Long listitem) {
		this.listitem = listitem;
	}

	public void setChecklist(Long checklist) {
		this.checklist = checklist;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setVulns(List<Long> vulns) {
		this.vulns = vulns;
	}

	public File getFile_data() {
		return file_data;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFilename() {
		return filename;
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

	public CheckList getCheck() {
		return check;
	}

	public List<AssessmentType> getTypes() {
		return types;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getActiveChecklist() {
		return "active";
	}
	
	
	
	
	

}
