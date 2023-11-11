package com.fuse.actions.assessment;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;


import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
//import com.fuse.dao.VTImage;
import com.fuse.dao.Vulnerability;

import com.fuse.utils.FSUtils;


@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/assessment/AddStepJSON.jsp")
public class AddStep extends FSActionSupport{

	private Long vulnid;
	private String action; //update,add,delete
	private String description;
	private File file_data;
	private String contentType;
	private String filename;
	private Long stepId;
	private List<ExploitStep> steps;
	private String editor6;
	private byte[] stepImage;
	private String direction;
	private String appId;

	
	@Action(value="AddStep", 
			results={
		@Result(name="getSteps",location="/WEB-INF/jsp/assessment/GetStepsJSON.jsp"),
		@Result(name="stepRedirect", type="redirectAction",
			params = {"actionName", "Assessment?id=${appId}",
					   "anchor", "tab_2"}
		)
	})
	public String execute() throws IOException{
		
		
		if(!(this.isAcassessor() || this.isAcmanager()))
				return AuditLog.notAuthorized(this, "User is not an assessor or manager", true);
		User user = this.getSessionUser();

		
		if(this.vulnid!= null && action != null && action.equals("add")){
			if(!this.testToken())
				return this.ERRORJSON;
			
			//Vulnerability v = (Vulnerability)em.createQuery("from Vulnerability where id = :id").setParameter("id", vulnid).getResultList().stream().findFirst().orElse(null);
			Vulnerability v = em.find(Vulnerability.class, vulnid);
			Assessment a = em.find(Assessment.class, v.getAssessmentId());
			
			//Prevent steps from being added if the assessment is already closed
			if(a != null && a.getCompleted() == null ){
				//session.getTransaction().begin();
				ExploitStep exs = new ExploitStep();
				editor6 = FSUtils.sanitizeHTML(editor6);
				exs.setDescription(editor6);
				
				exs.setStepNum(v.getSteps().size()+1);
				
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				this.appId="app"+ v.getAssessmentId();
				em.persist(exs);
				v.getSteps().add(exs);
				em.persist(v);
				AuditLog.audit(this, "Exploit Step Added to Assessent", AuditLog.UserAction, AuditLog.CompAssessment, v.getAssessmentId(), false);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			}else {
				this._message = "Assessment has been finalized.";
				return this.ERRORJSON;
			}
		
		}else if(this.vulnid!= null && action != null && action.equals("get")){
			Vulnerability v = (Vulnerability)em.find(Vulnerability.class, vulnid);
			Map<Long, ExploitStep> orderedSteps = this.orderSteps((List<ExploitStep>)v.getSteps());
			this.steps = new LinkedList<ExploitStep>();
			for(Long i=0l; i<orderedSteps.size(); i++){
				((List<ExploitStep>)this.steps).add(i.intValue(), orderedSteps.get(i+1));	
			}
			//session.close();
			
			return "getSteps";
		}else if(this.vulnid!= null && this.stepId!= null && action != null && action.equals("delete")){
			if(!this.testToken())
				return this.ERRORJSON;
			

			ExploitStep step = em.find(ExploitStep.class, stepId);
			Vulnerability v = em.find(Vulnerability.class, vulnid);
			Assessment a = em.find(Assessment.class, v.getAssessmentId());
			
			//Prevent steps from being edited if the assessment is already closed
			if(a != null && a.getCompleted() == null ){
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				Long stepNum = step.getStepNum();
				for(int i = 0; i < v.getSteps().size(); i++){
					ExploitStep testStep = ((List<ExploitStep>)v.getSteps()).get(i);
					if(testStep.getStepNum() > stepNum){
						testStep.setStepNum(testStep.getStepNum() -1l);
						//session.save(testStep);
						em.persist(testStep);
					}
				}
				v.getSteps().remove(step);
				em.remove(step);
				AuditLog.audit(this, "Exploit Step Deleted from Assessent", AuditLog.UserAction, AuditLog.CompAssessment, v.getAssessmentId(), false);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			}else {
				this._message = "Assessment has been finalized.";
				return this.ERRORJSON;
			}

			
			
			
		}else if(this.vulnid!= null && this.stepId!= null && action != null && action.equals("edit")){
			if(!this.testToken())
				return this.ERRORJSON;
			
			Vulnerability v = em.find(Vulnerability.class, vulnid);
			Assessment a = em.find(Assessment.class, v.getAssessmentId());
			
			//Preven steps from being edited if the assessment is already closed
			if(a != null && a.getCompleted() == null ){
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				//ExploitStep exs = (ExploitStep)session.createQuery("from ExploitStep where id = :id").setLong("id", stepId).uniqueResult();
				ExploitStep exs = em.find(ExploitStep.class, stepId);
				editor6 = FSUtils.sanitizeHTML(editor6);
				exs.setDescription(editor6);
				
				em.persist(exs);
			
				
				this.appId="app"+ v.getAssessmentId();
				AuditLog.audit(this, "Exploit Step Edited", AuditLog.UserAction, AuditLog.CompAssessment, v.getAssessmentId(), false);
				HibHelper.getInstance().commit();
			}else {
				this._message = "Assessment has been finalized.";
				return this.ERRORJSON;
			}
			return this.SUCCESSJSON;
		}
		return SUCCESS;
		
		
	}
	
	@Action(value="OrderUp")
	public String orderUp() {
		if(!(this.isAcassessor() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "User is not an assessor or manager", true);
		
		if(!this.testToken())
			return this.ERRORJSON;
		Vulnerability v =em.find(Vulnerability.class, vulnid);
		Assessment a = em.find(Assessment.class, v.getAssessmentId());
		
		//Prevent steps from being edited if the assessment is already closed
		if(a != null && a.getCompleted() == null ){
			
			// reorder steps move up
			//Vulnerability v = (Vulnerability)session.createQuery("from Vulnerability where id = :id").setLong("id", vulnid).uniqueResult();
			
			Map<Long, ExploitStep> orderedSteps = this.orderSteps((List<ExploitStep>)v.getSteps());
			
			ExploitStep test = orderedSteps.get(1l);
			//em.persist(test);
			if(test.getId().equals(stepId)){
	
				return this.SUCCESSJSON;
			}
			
			//session.getTransaction().begin();
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			for(Long i=0l; i<orderedSteps.size(); i++){
				ExploitStep exs = orderedSteps.get(i+1);
				if(exs.getId().equals(stepId)){
					exs.setStepNum(exs.getStepNum() -1);
					//session.save(exs);
					em.persist(exs);
					ExploitStep prevStep = orderedSteps.get(i);
					prevStep.setStepNum(prevStep.getStepNum()+1);
					//orderedSteps.get(i).setStepNum(orderedSteps.get(i+1).getStepNum()+1);
					//session.save(prevStep);
					em.persist(prevStep);
					//session.getTransaction().commit();
					break;
				}
				
			}
			AuditLog.audit(this, "Exploit Steps Reordered", AuditLog.UserAction, AuditLog.CompAssessment, v.getAssessmentId(), false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		}else {
			this._message = "Assessment has been finalized.";
			return this.ERRORJSON;
		}
		
	}
	@Action(value="OrderDn")
	public String orderDn() {
		// reorder steps move down
		if(!(this.isAcassessor() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "User is not an assessor or manager", true);
		
		if(!this.testToken())
			return this.ERRORJSON;
		
		Vulnerability v =em.find(Vulnerability.class, vulnid);
		Assessment a = em.find(Assessment.class, v.getAssessmentId());
		
		//Prevent steps from being edited if the assessment is already closed
		if(a != null && a.getCompleted() == null ){
			Map<Long, ExploitStep> orderedSteps = this.orderSteps((List<ExploitStep>)v.getSteps());
			ExploitStep test = orderedSteps.get(Long.parseLong(""+orderedSteps.size()));
			if(test.getId().equals(stepId)){
				return this.SUCCESSJSON;
			}
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			for(Long i=0l; i<orderedSteps.size(); i++){
				ExploitStep exs = orderedSteps.get(i+1);
				if(exs.getId().equals(stepId)){
					
					exs.setStepNum(exs.getStepNum() +1);
					//session.save(exs);
					em.persist(exs);
					ExploitStep nextStep = orderedSteps.get(i+2);
					nextStep.setStepNum(nextStep.getStepNum()-1);
					//orderedSteps.get(i).setStepNum(orderedSteps.get(i+1).getStepNum()+1);
					//session.save(nextStep);
					em.persist(nextStep);
					HibHelper.getInstance().commit();
					//session.getTransaction().commit();
					break;
				}
				
			}
			AuditLog.audit(this, "Exploit Step Reordered", AuditLog.UserAction, AuditLog.CompAssessment, v.getAssessmentId(), false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		}else {
			this._message = "Assessment has been finalized";
			return this.ERRORJSON;
		}
	}
	
	
	public String submit() {
	    return SUCCESS;
	  }
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getVulnid() {
		return vulnid;
	}
	public void setVulnid(Long vulnid) {
		this.vulnid = vulnid;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public File getFile_data() {
		return file_data;
	}
	public void setFile_data(File file_data) {
		this.file_data = file_data;
	}
	public Long getStepId() {
		return stepId;
	}
	public void setStepId(Long stepId) {
		this.stepId = stepId;
	}
	public List<ExploitStep> getSteps() {
		return steps;
	}
	public void setSteps(List<ExploitStep> steps) {
		this.steps = steps;
	}
	public String getEditor6() {
		return editor6;
	}
	public void setEditor6(String editor6) {
		this.editor6 = editor6;
	}
	public byte[] getStepImage() {
		return stepImage;
	}
	public void setStepImage(byte[]  stepImage) {
		this.stepImage = stepImage;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	private Map<Long,ExploitStep> orderSteps(List<ExploitStep>steps){
		Map<Long, ExploitStep> orderedSteps = new HashMap<Long, ExploitStep>();
		for(ExploitStep exs : steps){
			orderedSteps.put(exs.getStepNum(), exs);
		}
		return orderedSteps;
		
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
