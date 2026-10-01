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
	/** Request parameter; null when absent, which means "only mine" for users who have the toggle. */
	private Boolean onlyMine;
	private boolean showOnlyMineToggle;
	private boolean restrictToMine;


	@Action(value="AssessmentQueue")
	public String execute(){

		if(this.isAcassessor() || this.isAcmanager()){
			User u = this.getSessionUser();
			try{
				// Narrow the flag to what the user is actually allowed to see so the
				// page reflects the data it really loaded.
				this.showCompleted = this.includeCompleted(u);
				this.showOnlyMineToggle = canWidenQueue(u);
				this.restrictToMine = restrictToMine(u, this.onlyMine);
				assessments = AssessmentQueries.getAllAssessments(em, u,
						this.showCompleted ? AssessmentQueries.All : AssessmentQueries.OnlyNonCompleted,
						this.restrictToMine);
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

	/**
	 * Non-managers whose access level reaches past their own work (Team or All
	 * Assessments) get an "Only my assessments" checkbox in the queue. Managers
	 * already see the full queue; user-only accounts cannot widen it.
	 */
	public static boolean canWidenQueue(User u) {
		Permissions p = u.getPermissions();
		return !Boolean.TRUE.equals(p.isManager())
				&& !Permissions.AccessLevelUserOnly.equals(p.getAccessLevel());
	}

	/**
	 * Whether the queue should be limited to the user's own assessments. The
	 * checkbox is checked by default, so a missing parameter means "only mine" for
	 * users who have the toggle; it is ignored for everyone else.
	 */
	public static boolean restrictToMine(User u, Boolean onlyMineParam) {
		if (Permissions.AccessLevelUserOnly.equals(u.getPermissions().getAccessLevel())) {
			return true;
		}
		if (!canWidenQueue(u)) {
			return false;
		}
		return onlyMineParam == null || onlyMineParam.booleanValue();
	}

	public boolean getShowOnlyMineToggle() {
		return showOnlyMineToggle;
	}

	/** Effective state of the "Only my assessments" checkbox for the rendered page. */
	public boolean getOnlyMine() {
		return restrictToMine;
	}

	public void setOnlyMine(Boolean onlyMine) {
		this.onlyMine = onlyMine;
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
