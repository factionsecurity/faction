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
import com.fuse.dao.Permissions;
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
	private boolean showCompleted;


	@Action(value="AssessmentQueue")
	public String execute(){

		if(this.isAcassessor() || this.isAcmanager()){
			User u = this.getSessionUser();
			try{
				// Narrow the flag to what the user is actually allowed to see so the
				// page reflects the data it really loaded.
				this.showCompleted = this.includeCompleted(u);
				assessments = AssessmentQueries.getAllAssessments(em, u,
						this.showCompleted ? AssessmentQueries.All : AssessmentQueries.OnlyNonCompleted);
				levels = em.createQuery("from RiskLevel order by riskId desc").getResultList();
			}catch(Exception ex){}
			//em.close();
			return SUCCESS;
		}else{
			AuditLog.notAuthorized(this, "User is not an Assessor or Manager", true);
			return LOGIN;
		}
	}

	
	

	/**
	 * The queue is scoped to active work by default and only pulls in completed
	 * assessments when the status filter asks for them. Users restricted to their
	 * own assessments have no access to completed ones at all
	 * ({@link AssessmentQueries#canAccessAssessment}), so the flag is ignored for
	 * them rather than listing rows they cannot open.
	 */
	private boolean includeCompleted(User u) {
		return this.showCompleted
				&& u.getPermissions().getAccessLevel() != Permissions.AccessLevelUserOnly;
	}

	public boolean getShowCompleted() {
		return showCompleted;
	}

	public void setShowCompleted(boolean showCompleted) {
		this.showCompleted = showCompleted;
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
