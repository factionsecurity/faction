package com.fuse.actions.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.CustomType;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.dao.Vulnerability;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/assessment/AuditView.jsp")
public class AssessmentAuditLog extends FSActionSupport{
	
	private List<AuditLog> logs;
	private Assessment assessment;
	private List<RiskLevel> levels=new ArrayList();
	private HashMap<Integer, Integer> counts = new HashMap();
	
	@Action(value="AuditLog")
	public String getLog() {
		if(!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();

		List<CustomType> vulntypes = em.createQuery("from CustomType where type = 1").getResultList();
		
		Long lid = (Long)this.getSession("asmtid");
		assessment = AssessmentQueries.getAssessment(em, this.getSessionUser(), lid);
		logs = AssessmentQueries.getLogs(em, assessment);

		List<Vulnerability> avulns = (List<Vulnerability>)assessment.getVulns();

		for(int i=0; i<10; i++){
			counts.put(i, 0);
		}
		for(Vulnerability v : avulns){
			v.updateRiskLevels(em);
			if(v.getOverall() == null || v.getOverall() == -1l)
				continue;
			else
				counts.put(v.getOverall().intValue(), counts.get(v.getOverall().intValue())+1);

		}
		return SUCCESS;
	}
	
	public String getActiveAQ() {
		return "active";
	}

	public List<AuditLog> getLogs() {
		return logs;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

	public HashMap<Integer, Integer> getCounts() {
		return counts;
	}
	
	

}
