package com.fuse.actions.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.CheckList;
import com.fuse.dao.CheckListAnswers;
import com.fuse.dao.CheckListItem;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

 
@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/assessment/list.jsp")
public class CheckListView extends FSActionSupport{
	
	private Map<Long, List<CheckListAnswers>> checklists = new HashMap();
	private Map<Long,String> assignedLists = new HashMap();
	private List<CheckList> lists = new ArrayList();
	private CheckList checklist;
	private Long checklistid;
	private int answer;
	private String note;
	private Assessment assessment;
	private String id;
	private boolean prSubmitted;
	private boolean prComplete;
	private List<RiskLevel>levels=new ArrayList();
	private List<Vulnerability>avulns = new ArrayList<Vulnerability>();
	private HashMap<Integer, Integer>counts = new HashMap();
	private Boolean notowner=false;
	private Boolean finalized;
	
	
	@Action(value="CheckList", results={
			@Result(name="checklist",location="/WEB-INF/jsp/assessment/list.jsp")
		})
	public String ShowCheckList(){
		if(!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		Long lid = (Long)this.getSession("asmtid");
		
		User user = this.getSessionUser();
		
		if(this.isAcmanager()) {
			assessment = (Assessment) em
					.createNativeQuery("{ \"_id\" : " + lid +"}", Assessment.class)
					.getResultList().stream().findFirst().orElse(null);
			User mgrs = assessment.getAssessor().stream().filter(u -> u.getId() == user.getId()).findFirst().orElse(null);
			if(mgrs == null)
				this.notowner = true;
		
		}else {
			assessment = (Assessment) em
					.createNativeQuery("{\"assessor\" : "+user.getId() +", \"_id\" : " + lid +"}", Assessment.class)
					.getResultList().stream().findFirst().orElse(null);
		}
		
		if(assessment == null) {
			return this.ERROR;
		}
		
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		if(assessment.getType() == null)
			lists = new ArrayList();
		else {
			lists = em.createQuery("from CheckList c JOIN c.types t where t = :type")
					.setParameter("type", assessment.getType().getId().intValue())
					.getResultList();
		}
		
		if(assessment.getCompleted() != null )
			finalized = true;
		
		avulns = (List<Vulnerability>)assessment.getVulns();
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
		
		PeerReview prTemp = (PeerReview)em
				.createNativeQuery("{\"assessment_id\" : "+assessment.getId() +"}", PeerReview.class)
				.getResultList().stream().findFirst().orElse(null);
		
		// THis is used to disable the UI if we are in Peer Review
		if(prTemp == null) {
			this.prSubmitted = false;
			this.prComplete = false;
			
		}else {
			this.prSubmitted = true;
			if(prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0){
				prComplete = true;
			}else
				prComplete = false;
		}
		checklists = new HashMap();
		assignedLists = new HashMap();
		if(this.checklistid == null) {
			if(assessment.getAnswers() != null && assessment.getAnswers().size() !=0) {
				this.checklistid = assessment.getAnswers().get(0).getCheckId();
			}
		}
		for(CheckListAnswers a : assessment.getAnswers()){
			assignedLists.put(a.getCheckId(), a.getChecklist());
			if(a.getCheckId().equals(this.checklistid)){
				if(checklists.containsKey(a.getCheckId())){
					checklists.get(a.getCheckId()).add(a);
				}else{
					checklists.put(a.getCheckId(), new ArrayList());
					checklists.get(a.getCheckId()).add(a);
				}
			}
		}
		
		
		return SUCCESS;
	}
	 
	@Action(value="AddCheckListToAssessment")
	public String addCheckListToAssessment() {
		if(!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		
		if(!this.testToken())
			return this.ERRORJSON;
		
		User user = this.getSessionUser();
		
		Long asmtid = (Long)this.getSession("asmtid");
		assessment = em.find(Assessment.class, asmtid);
		
		CheckList list = em.find(CheckList.class, Long.parseLong(id));
		if(list == null) {
			this._message = "Incorrect Checklist";
			return this.ERRORJSON;
			
		}
		
		if(assessment.getAnswers() == null)
			assessment.setAnswers(new ArrayList());
		for(CheckListAnswers a : assessment.getAnswers()) {
			if(a.getCheckId().longValue() == Long.parseLong(id)) {
				this._message = "You already have this checklist";
				return this.ERRORJSON;
			}
		}
		
		for(CheckListItem item : list.getQuestions()){
			CheckListAnswers a = new CheckListAnswers();
			a.setAnswer(CheckListAnswers.Answer.Incomplete);
			a.setChecklist(list.getName());
			a.setQuestion(item.getQuestion());
			a.setCheckId(list.getId());
			assessment.getAnswers().add(a);
		}
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(assessment);
		AuditLog.audit(this, "Checklist " + list.getName() + " added to assessment", AuditLog.UserAction,
				AuditLog.CompAssessment, assessment.getId(), false);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
		
	}
	
	@Action(value="UpdateQuestion")
	public String UpdateQuestion(){
		if(!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		
		if(!this.testToken(false))
			return this.ERRORJSON;
		
		User user = this.getSessionUser();
		
			
	
		Long asmtid = (Long) this.getSession("asmtid");
		
		Assessment assessment = em.find(Assessment.class, asmtid);
		if(assessment == null) {
			this._message = "Assessment Does not exist";
			return this.ERRORJSON;
		}
		if(assessment.isFinalized()) {
			this._message = "Assessment has been finalized.";
			return this.ERRORJSON;
		}
		

		for(CheckListAnswers a : assessment.getAnswers()){
			if(a.getId().intValue() == checklistid)	{
				
				switch(answer){
					case 1: a.setAnswer(CheckListAnswers.Answer.NA);break;
					case 3: a.setAnswer(CheckListAnswers.Answer.Pass);break;
					case 2: a.setAnswer(CheckListAnswers.Answer.Fail);break;
					default: a.setAnswer(CheckListAnswers.Answer.Incomplete);break;
				}
				
				prSubmitted=false;
				// don't update the database if we are in PeerReview
				PeerReview prTemp = (PeerReview)em
						.createNativeQuery("{\"assessment_id\" : "+assessment.getId() +"}", PeerReview.class)
						.getResultList().stream().findFirst().orElse(null);
				if( prTemp != null) {
					prSubmitted = true;
					if(prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0){
						prComplete = true;
					}else
						prComplete = false;
				}
				
					
				if(!prSubmitted || (prSubmitted && prComplete)){	
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					em.persist(a);
					HibHelper.getInstance().commit();
				}
				return this.SUCCESSJSON;
			}
			
		}
		
		return this.ERRORJSON;
	}
	
	
	@Action(value="SetAll")
	public String SetAllNA(){
		if(!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		
		if(!this.testToken(false))
			return this.ERRORJSON;
		
		User user = this.getSessionUser();
		
			
	
		Long asmtid = (Long) this.getSession("asmtid");
		
		Assessment assessment = em.find(Assessment.class, asmtid);
		if(assessment == null) {
			this._message = "Assessment Does not exist";
			return this.ERRORJSON;
		}
		if(assessment.isFinalized()) {
			this._message = "Assessment has been finalized.";
			return this.ERRORJSON;
		}
		if(assessment.isInPr()) {
			this._message = "Assessment is in PeerReview.";
			return this.ERRORJSON;
		}
		if(assessment.isPrComplete()) {
			this._message = "You Must Review the PeerReview to make changes.";
			return this.ERRORJSON;
		}
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		for(CheckListAnswers a : assessment.getAnswers()){
			if(a.getCheckId().longValue() == checklistid)	{
				switch(answer){
					case 1: a.setAnswer(CheckListAnswers.Answer.NA);break;
					case 3: a.setAnswer(CheckListAnswers.Answer.Pass);break;
					case 2: a.setAnswer(CheckListAnswers.Answer.Fail);break;
					default: a.setAnswer(CheckListAnswers.Answer.Incomplete);break;
				}
				em.persist(a);
			}
			
		}
		
		HibHelper.getInstance().commit();
	
		return this.SUCCESSJSON;
		
	}
	@Action(value="UpdateNote")
	public String UpdateNote(){
		if(!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		
		if(!this.testToken(false))
			return this.ERRORJSON;
		
		User user = this.getSessionUser();
		
		Long asmtid = (Long) this.getSession("asmtid");
		Assessment assessment = em.find(Assessment.class, asmtid);
		if(assessment == null) {
			this._message = "Assessment Does not exist";
			return this.ERRORJSON;
		}
		if(assessment.getCompleted() != null) {
			this._message = "Assessment has been finalized.";
			return this.ERRORJSON;
		}
		
		for(CheckListAnswers a : assessment.getAnswers()){
			if(a.getId().intValue() == checklistid)	{
				a.setNotes(this.note);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(a);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			}
			
		}
		
		return this.ERRORJSON;
	}
	
	public String getActiveAQ() {
		return "active";
	}

	public Map<Long, List<CheckListAnswers>> getChecklists() {
		return checklists;
	}

	public List<CheckList> getLists() {
		return lists;
	}

	public CheckList getChecklist() {
		return checklist;
	}

	public Long getChecklistid() {
		return checklistid;
	}

	public int getAnswer() {
		return answer;
	}

	public String getNote() {
		return note;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public String getId() {
		return id;
	}

	public boolean isPrSubmitted() {
		return prSubmitted;
	}

	public void setChecklistid(Long checklistid) {
		this.checklistid = checklistid;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isPrComplete() {
		return prComplete;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

	public List<Vulnerability> getAvulns() {
		return avulns;
	}

	public HashMap<Integer, Integer> getCounts() {
		return counts;
	}

	public Map<Long, String> getAssignedLists() {
		return assignedLists;
	}

	public Boolean getNotowner() {
		return notowner;
	}

	public void setNotowner(Boolean notowner) {
		this.notowner = notowner;
	}

	public Boolean getFinalized() {
		return finalized;
	}
	
	
	
	
	

}
