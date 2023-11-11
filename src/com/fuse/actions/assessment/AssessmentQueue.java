package com.fuse.actions.assessment;

import java.util.ArrayList;
import java.util.List;


import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Query;
import org.hibernate.Session;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.utils.AccessControl;
import com.opensymphony.xwork2.ActionContext;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/assessment/AssessmentQueue.jsp")
public class AssessmentQueue extends FSActionSupport{
	
	private  List<Assessment> assessments;
	private List<RiskLevel>levels=new ArrayList();
	

	@Action(value="AssessmentQueue")
	public String execute(){
		
		if(this.isAcassessor() || this.isAcmanager()){
			User u = this.getSessionUser();
			//{ assessor_Id : 158, $and : [{ completed : {$exists:false}}, {start : ISODate("2015-12-14T06:00:00Z")}]}
			
			try{
				assessments = AssessmentQueries.getAllAssessments(em, u, AssessmentQueries.OnlyNonCompleted);
				levels = em.createQuery("from RiskLevel order by riskId desc").getResultList();
			}catch(Exception ex){}
			//em.close();
			return SUCCESS;
		}else{
			AuditLog.notAuthorized(this, "User is not an Assessor or Manager", true);
			return LOGIN;
		}
	}

	
	

	public List<Assessment> getAssessments() {
		return assessments;
	}

	public void setAssessments(List<Assessment> assessments) {
		this.assessments = assessments;
	}
	
	
	public String getActiveAQ() {
		return "active";
	}


	public List<RiskLevel> getLevels() {
		return levels;
	}





	






	
	

}
