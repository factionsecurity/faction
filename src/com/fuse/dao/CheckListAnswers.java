package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
public class CheckListAnswers {

	
	@Id
	@GeneratedValue
	private Long id;
	private String notes;
	private Integer answer = Answer.Incomplete.getValue();
	private String question;
	@OneToOne
	private User assessor;
	@OneToOne
	private Assessment assessment;
	private String checklist;
	private Long checkId;
	

	public static enum Answer { 
		Pass(3), Fail(2), NA(1), Incomplete(0);
		
		private final int value;
	    private Answer(int value) {
	        this.value = value;
	    }

	    public  int getValue() {
	        return value;
	    }
	    public static  Answer getAnswer(int value){
	    	switch(value){
	    	case 0 : return Incomplete;
	    	case 1 : return NA;
	    	case 2 : return Fail;
	    	case 3 : return Pass;
	    	default:
	    		return Incomplete;
	    	}
	    }
		
	}
	
	public Long getId() {
		return id;
	}
	public String getNotes() {
		return notes;
	}
	public Answer getAnswer() {
		return Answer.getAnswer(this.answer);
	}
	public String getQuestion() {
		return question;
	}
	public User getAssessor() {
		return assessor;
	}
	public Assessment getAssessment() {
		return assessment;
	}
	public String getChecklist() {
		return checklist;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public void setAnswer(int answer) {
		this.answer = answer;
	}
	public void setAnswer(Answer answer) {
		this.answer = answer.getValue();
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	public void setChecklist(String checklist) {
		this.checklist = checklist;
	}
	public Long getCheckId() {
		return checkId;
	}
	public void setAnswer(Integer answer) {
		this.answer = answer;
	}
	public void setCheckId(Long checkId) {
		this.checkId = checkId;
	}
	
	
	
	
	
	
	
}
