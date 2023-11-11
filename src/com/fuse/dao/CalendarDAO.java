package com.fuse.dao;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class CalendarDAO {
	
	
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private User assessor;
	@ManyToOne
	private Assessment assessment;
	private Date start;
	private Date end;
	@ManyToOne
	private CalendarType calType;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getAssessor() {
		return assessor;
	}
	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public CalendarType getCalType() {
		return calType;
	}
	public void setCalType(CalendarType calType) {
		this.calType = calType;
	}
	
	
	

}
