package com.fuse.actions.remediation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.utils.FSUtils;
import com.fuse.utils.Item;

import java.util.Calendar;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/remediation/RemediationQueue.jsp")
public class RemediationQueue extends FSActionSupport{
	private List<Item> items = new ArrayList<Item>();
	private List<Long> itemList = new ArrayList<Long>();
	private List<RiskLevel>levels = new ArrayList();
	private String checked="";
	private boolean all=false;
	
	
	@Action(value="RemediationQueue")
	public String execute()
	{
		if(!this.isAcremediation())
			return LOGIN;
		Long uid = this.getSessionUser().getId();
		List<Verification>vs;
		List<Assessment>as;
		if(this.all){
			vs = (List<Verification>)em
					.createQuery("from Verification "
							+ "where workflowStatus = :wf1 or workflowStatus = :wf2 "
							+ "or workflowStatus = :wf3")
					.setParameter("wf1", Verification.InAssessorQueue)
					.setParameter("wf2", Verification.AssessorCompleted)
					.setParameter("wf3", Verification.AssessorCancelled)
					.getResultList();
		}else{
			vs = (List<Verification>)em
					.createQuery("from Verification v "
							+ "where v.assignedRemediation = :id "
							+ "and (v.workflowStatus = :wf1 or "
							+ "v.workflowStatus = :wf2 or "
							+ "workflowStatus = :wf3)")
					.setParameter("id", this.getSessionUser())
					.setParameter("wf1", Verification.InAssessorQueue)
					.setParameter("wf2", Verification.AssessorCompleted)
					.setParameter("wf3", Verification.AssessorCancelled)
					.getResultList();
		}
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings" ).getResultList().stream().findFirst().orElse(null);
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		//Check status of currently assigned verifications
		for(Verification verification : vs){
			Item i = new Item();
			i.setAppid(verification.getAssessment().getAppId());
			i.setAppname(verification.getAssessment().getName());
			i.setAssessor(verification.getAssessor());
			i.setDesc(verification.getVerificationItems().get(0).getVulnerability().getName());
			i.setDue(verification.getEnd());
			i.setOpened(verification.getStart());
			verification.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
			//i.setSeverity(v.getVerificationItems().get(0).getVulnerability().getOverallStr());
			Long sev = verification.getVerificationItems().get(0).getVulnerability().getOverall();
			i.setSeverity(em, sev, levels);
			i.setType("Verification");
			i.setVulnid(-1l * verification.getId());
			String badges = "";
			if(verification.getWorkflowStatus().equals(Verification.AssessorCancelled)) {
				badges += FSUtils.addBadge("Verification Cancelled", "yellow", "fa-times");
			}else {
				
				if(verification.getEnd().getTime() < (new Date().getTime())) {
					badges += FSUtils.addBadge("Verification Past Due", "red", "fa-calendar");
				}
				else if(verification.getEnd().getTime() < (FSUtils.getWarn(new Date(), 2)).getTime() ) {
					badges += FSUtils.addBadge("Verification Almost Due", "yellow", "fa-calendar");
				}
				
				if(verification.getWorkflowStatus().equals(Verification.AssessorCompleted)) {
					if(verification.getVerificationItems().get(0).isPass()) {
						badges += FSUtils.addBadge("Verification Passed", "green", "fa-check");
					}else if(verification.getCompleted() != null && verification.getCompleted().getTime() > 0) {
						badges += FSUtils.addBadge("Verification Failed", "red", "fa-times");
					}else{
						badges += FSUtils.addBadge("In Retest", "green", "fa-calendar");
					}
				}
			}
			
			Vulnerability tmpVuln = verification.getVerificationItems().get(0).getVulnerability();
			Date DueDate = FSUtils.getDue(em, tmpVuln.getOpened(), tmpVuln.getOverall().intValue());
			Date WarnDate = FSUtils.getWarning(em, tmpVuln.getOpened(), tmpVuln.getOverall().intValue());
			String pattern = "MM/dd/yyyy";
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			if(DueDate != null ) {
				String dueDateString = format.format(DueDate);
				
				if(DueDate != null && DueDate.getTime() <= (new Date()).getTime()){
					badges += FSUtils.addBadge("Vulnerability Past Due (" + dueDateString +")", "red", "fa-bug");
				}
				else if(WarnDate != null && WarnDate.getTime() <= (new Date()).getTime()){
					badges += FSUtils.addBadge("Vulnerability Approaching Due Date (" + dueDateString +")", "yellow", "fa-bug");
				}
			}
			i.setInfo(badges);
			
			
			items.add(i);
			itemList.add(verification.getVerificationItems().get(0).getVulnerability().getId());
			
			
		}
		
		List<Vulnerability> vulns = VulnerabilityQueries.getOpenVulns(em);
		
		for(Vulnerability v : vulns){
	
			Date DueDate = FSUtils.getDue(em, v.getOpened(), v.getOverall().intValue());
			Date WarnDate = FSUtils.getWarning(em, v.getOpened(), v.getOverall().intValue());
			if( DueDate!= null && DueDate.getTime() <= (new Date()).getTime()){
					if(itemList.contains(v.getId()))
						continue;
					Assessment a = em.find(Assessment.class, v.getAssessmentId());
					if(a == null)
						continue;
					Item i = new Item();	
					i.setAppid(a.getAppId());
					i.setAppname(a.getName());
					i.setDesc(v.getName());
					i.setAssessor(a.getAssessor().get(0)); 
					i.setDue(DueDate); 
					v.updateRiskLevels(em);
					//i.setSeverity(v.getOverallStr());
					Long sev = v.getOverall();
					i.setSeverity(em, sev, levels);
					i.setVulnid(v.getId());
					i.setInfo(FSUtils.addBadge("Vulnerability Past Due", "red", "fa-bug"));
					i.setType("Vulnerability");
					i.setOpened(v.getOpened());
					items.add(i);	
						
			// issues are appraching the due date	
			}else if( WarnDate!= null && WarnDate.getTime() <= (new Date()).getTime()){
				if(v.getClosed() != null && v.getClosed().getTime() != 0)
					continue;
				if(itemList.contains(v.getId()))
					continue;
				Item i = new Item();
				Assessment a = em.find(Assessment.class, v.getAssessmentId());
				if(a == null)
					continue;
				i.setAppid(a.getAppId());
				i.setAppname(a.getName());
				i.setDesc(v.getName());
				i.setAssessor(a.getAssessor().get(0)); //TODO: this might also be fucked up
				i.setDue(DueDate); 
				v.updateRiskLevels(em);
				//i.setSeverity(v.getOverallStr());
				Long sev = v.getOverall();
				i.setSeverity(em, sev, levels);
				i.setOpened(v.getOpened());
				i.setVulnid(v.getId());
				i.setInfo(FSUtils.addBadge("Vulnerability Approaching Due Date", "yellow", "fa-bug"));
				i.setType("Vulnerability");
				items.add(i);
			}
			
		}
			
		
		
		
		return SUCCESS;
	}
	public String getActiveRem(){
		return "active";
	}
	
	public List<Item> getItems() {
		return items;
	}
	

	/*private HashMap<String,Date> VulnStatus(Vulnerability v, SystemSettings ems){
		Calendar now = Calendar.getInstance();
		Calendar cA = Calendar.getInstance();
		Calendar cD = Calendar.getInstance();
		Calendar hA = Calendar.getInstance();
		Calendar hD = Calendar.getInstance();
		Calendar mA = Calendar.getInstance();
		Calendar mD = Calendar.getInstance();
		Calendar lA = Calendar.getInstance();
		Calendar lD = Calendar.getInstance();
		cA.setTimeInMillis(v.getOpened().getTime());
		cD.setTimeInMillis(v.getOpened().getTime());
		hA.setTimeInMillis(v.getOpened().getTime());
		hD.setTimeInMillis(v.getOpened().getTime());
		mA.setTimeInMillis(v.getOpened().getTime());
		mD.setTimeInMillis(v.getOpened().getTime());
		lA.setTimeInMillis(v.getOpened().getTime());
		lD.setTimeInMillis(v.getOpened().getTime());
		
		//Just alert that due date is approaching
		cA.add(Calendar.DATE, ems.getCritAlert());
		hA.add(Calendar.DATE, ems.getHighAlert());
		mA.add(Calendar.DATE, ems.getMediumAlert());
		lA.add(Calendar.DATE, ems.getLowAlert());
		
		// Vulns are due and late at this time
		cD.add(Calendar.DATE, ems.getCritical());
		hD.add(Calendar.DATE, ems.getHigh());
		mD.add(Calendar.DATE, ems.getMedium());
		lD.add(Calendar.DATE, ems.getLow());
		
		
		//Vulns are past due
		if( (v.getOverall() == 5l && cD.getTimeInMillis() < now.getTimeInMillis())
				|| (v.getOverall() == 4l && hD.getTimeInMillis() < now.getTimeInMillis())
				|| (v.getOverall() == 3l && mD.getTimeInMillis() < now.getTimeInMillis())
				|| (v.getOverall() == 2l && lD.getTimeInMillis() < now.getTimeInMillis())
		){
			HashMap hm = new HashMap<String, Date>();
			
			switch(v.getOverall().intValue()){
				case 5: 
					hm.put("late", cD.getTime());
					return hm;
				case 4: 
					hm.put("late", hD.getTime());
					return hm;
				case 3: 
					hm.put("late", mD.getTime());
					return hm;
				case 2: 
					hm.put("late", lD.getTime());
					return hm;
			}
			// Due dates are approaching	
		}else if( (v.getOverall() == 5l && cA.getTimeInMillis() < now.getTimeInMillis())
			|| (v.getOverall() == 4l && hA.getTimeInMillis() < now.getTimeInMillis())
			|| (v.getOverall() == 3l && mA.getTimeInMillis() < now.getTimeInMillis())
			|| (v.getOverall() == 2l && lA.getTimeInMillis() < now.getTimeInMillis())
			){
			HashMap hm = new HashMap<String, Date>();
			
			switch(v.getOverall().intValue()){
			case 5: 
				hm.put("approaching", cD.getTime());
				return hm;
			case 4: 
				hm.put("approaching", hD.getTime());
				return hm;
			case 3: 
				hm.put("approaching", mD.getTime());
				return hm;
			case 2: 
				hm.put("approaching", lD.getTime());
				return hm;
		}
		}
		return null;
		
	}*/

	public List<RiskLevel> getLevels() {
		return levels;
	}




	public String getChecked() {
		if(all==true)
			return "checked='checked'";
		else
			return "";
	}

	public void setAll(boolean all) {
		this.all = all;
	}
	
	

}
