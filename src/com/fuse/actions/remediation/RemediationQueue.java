package com.fuse.actions.remediation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

import java.util.Calendar;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/remediation/RemediationQueue.jsp")
public class RemediationQueue extends FSActionSupport{
	private List<Item> items = new ArrayList<Item>();
	private List<Long> itemList = new ArrayList<Long>();
	private List<RiskLevel>levels = new ArrayList();
	private String checked="";
	private boolean all=false;
	
	private Date getDue(EntityManager em, Date start, int Level){
		RiskLevel level = (RiskLevel)em.createQuery("from RiskLevel where riskId = :id")
				.setParameter("id", Level).getResultList()
				.stream().findFirst().orElse(null);
		if(level.getDaysTillDue() == null)
			return null;
		Calendar dueDate =  Calendar.getInstance();
		dueDate.setTime(start);
		dueDate.add(Calendar.DAY_OF_YEAR, level.getDaysTillDue());
		return dueDate.getTime();
	}
	
	private Date getWarning(EntityManager em, Date start, int Level){
		RiskLevel level = (RiskLevel)em.createQuery("from RiskLevel where riskId = :id")
				.setParameter("id", Level).getResultList()
				.stream().findFirst().orElse(null);
		if(level.getDaysTillWarning() == null)
			return null;
		Calendar dueDate =  Calendar.getInstance();
		dueDate.setTime(start);
		dueDate.add(Calendar.DAY_OF_YEAR, level.getDaysTillWarning());
		return dueDate.getTime();
	}
	
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
			//Get completed assessments
			//as = (List<Assessment>)em.createNativeQuery("{\"remediation_Id\" : "+uid+", \"workflow\" : 4}", Assessment.class).getResultList();
		}
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings" ).getResultList().stream().findFirst().orElse(null);
		//List<RiskLevel> levels = (List<RiskLevel>)em.createQuery("from RiskLevel order by riskId").getResultList();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		//Check status of currently assigned verifications
		for(Verification v : vs){
			//remove completed Verifications from the list
			//System.out.println(v.getCompleted().getTime());
			//if(v.getCompleted().getTime() == 0l)
				//continue;
			Item i = new Item();
			i.setAppid(v.getAssessment().getAppId());
			i.setAppname(v.getAssessment().getName());
			i.setAssessor(v.getAssessor());
			i.setDesc(v.getVerificationItems().get(0).getVulnerability().getName());
			i.setDue(v.getEnd());
			i.setOpened(v.getStart());
			v.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
			//i.setSeverity(v.getVerificationItems().get(0).getVulnerability().getOverallStr());
			Long sev = v.getVerificationItems().get(0).getVulnerability().getOverall();
			i.setSeverity(em, sev, levels);
			i.setType("Verification");
			i.setVulnid(-1l * v.getId());
			if(v.getWorkflowStatus().equals(Verification.AssessorCancelled)) {
				i.setInfo("<span id='ver" + v.getId() + "'>"
						+"<i class='circle glyphicon glyphicon-remove bg-orange' title='Cancelled'></i> "
						+"</span>");
			}else {
				i.setInfo("<span id='ver" + v.getId() + "'>"
									+"<i class='circle glyphicon glyphicon-ok bg-gray' title='Pass/Fail'></i> "
									+"<i class='circle glyphicon glyphicon-calendar bg-green' title='Past Due Verification'></i> "
									+"<i class='circle fa fa-bug bg-gray' title='Past Due Vulnerability'></i>"
								+"</span>");
				if(i.getDue() == null){
					i.setInfo(i.getInfo().replace("green", "red"));
				}else if(i.getDue().getTime() < (new Date()).getTime())
					i.setInfo(i.getInfo().replace("green", "red"));
				if(v.getVerificationItems().get(0).isPass())
					i.setInfo(i.getInfo().replace("glyphicon-ok bg-gray", "glyphicon-ok bg-green"));
				else if(v.getCompleted() != null && v.getCompleted().getTime() > 0)
					i.setInfo(i.getInfo().replace("glyphicon-ok bg-gray", "glyphicon-ok bg-red"));
			}
			
			//HashMap<String, Date>status = this.VulnStatus(v.getVerificationItems().get(0).getVulnerability(), ems);
			Vulnerability tmpVuln = v.getVerificationItems().get(0).getVulnerability();
			Date DueDate = getDue(em, tmpVuln.getOpened(), tmpVuln.getOverall().intValue());
			Date WarnDate = getWarning(em, tmpVuln.getOpened(), tmpVuln.getOverall().intValue());
			if(DueDate != null && DueDate.getTime() <= (new Date()).getTime()){
				i.setInfo(i.getInfo().replace("circle fa fa-bug bg-gray", "circle fa fa-bug bg-red"));
				i.setInfo(i.getInfo().replace("title='Past Due Vulnerability", "title='Past Due Vulnerability - " + DueDate));
			}
			else if(WarnDate != null && WarnDate.getTime() <= (new Date()).getTime()){
				i.setInfo(i.getInfo().replace("circle fa fa-bug bg-gray", "circle fa fa-bug bg-yellow"));
				i.setInfo(i.getInfo().replace("title='Past Due Vulnerability", "title='Approaching Due Date - " + DueDate));
			}else{
				i.setInfo(i.getInfo().replace("title='Past Due Vulnerability", "title='Vulnerability OK' - " + DueDate));
			}
			
			
			items.add(i);
			itemList.add(v.getVerificationItems().get(0).getVulnerability().getId());
			
			
		}
		
		List<Vulnerability> vulns = VulnerabilityQueries.getOpenVulns(em);
		
		for(Vulnerability v : vulns){
	
			Date DueDate = getDue(em, v.getOpened(), v.getOverall().intValue());
			Date WarnDate = getWarning(em, v.getOpened(), v.getOverall().intValue());
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
					i.setInfo("<span id='vuln'" + v.getId() + "'><i class='circle fa fa-bug bg-red' title='Past Due'></i><span>");
					i.setType("Past Due Vulnerability");
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
				i.setInfo("<span id='vuln'" + v.getId() + "'><i class='circle fa fa-bug bg-yellow' title='Due Date Approaching'></i><span>");
				i.setType("Due Date Aproaching");
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


	private class Item{
		private User assessor;
		private String appid;
		private String appname;
		private String desc;
		private String info;
		private Date due;
		private String type;
		private Date opened;
		private String severity;
		private Long vulnid;
		
		public User getAssessor() {
			return assessor;
		}
		public void setAssessor(User assessor) {
			this.assessor = assessor;
		}
		public String getAppid() {
			return appid;
		}
		public void setAppid(String appid) {
			this.appid = appid;
		}
		public String getAppname() {
			return appname;
		}
		public void setAppname(String appname) {
			this.appname = appname;
		}
		public String getDesc() {
			return desc;
		}
		public void setDesc(String desc) {
			this.desc = desc;
		}
		public String getInfo() {
			return info;
		}
		public void setInfo(String info) {
			this.info = info;
		}
		public Date getDue() {
			return due;
		}
		public void setDue(Date due) {
			this.due = due;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = "<b style=\"color:black\">" + type + "</b>";
			if(type.equals("Verification"))
				this.type = this.type.replace("black", "#00a65a");
			else if(type.equals("Past Due Vulnerability"))
				this.type = this.type.replace("black", "#dd4b39");
			else if(type.equals("Due Date Aproaching"))
				this.type = this.type.replace("black", "#00c0ef");
			
			
		}
		public Date getOpened() {
			return opened;
		}
		public void setOpened(Date opened) {
			this.opened = opened;
		}
		public String getSeverity() {
			return severity;
		}
		/*public void setSeverity(String severity) {
			this.severity = "<b style=\"color:black\">" + severity + "</b>";
			if(severity.equals("Critical"))
				this.severity = this.severity.replace("black", "red");
			else if(severity.equals("High"))
				this.severity = this.severity.replace("black", "orange");
			else if(severity.equals("Medium"))
				this.severity = this.severity.replace("black", "yellow");
			else if(severity.equals("Low"))
				this.severity = this.severity.replace("black", "blue");
		}*/
		public void setSeverity(EntityManager em, Long riskId, List<RiskLevel>levels) {
			this.severity = levels.get(riskId.intValue()).getRisk();
			//this.severity = "<b style=\"color:black\">" + levels.get(riskId.intValue()).getRisk() + "</b>";
			/*String[] colors = new String [] {"red", "orange", "yellow", "blue", "aqua","green"};
			int c=0;
			for(int i=9; i>=0; i--){
				String risk = levels.get(i).getRisk();
				if(risk == null || risk.equals("") || risk.toLowerCase().equals("unassigned"))
					continue;
				else if (riskId.intValue() == i && c < colors.length){
					this.severity = this.severity.replace("black", colors[c]);
				}else
					c++;
			}*/
		}
		public Long getVulnid() {
			return vulnid;
		}
		public void setVulnid(Long vulnid) {
			this.vulnid = vulnid;
		}
		
		
		
		
		
		
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
