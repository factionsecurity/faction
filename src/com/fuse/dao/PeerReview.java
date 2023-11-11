package com.fuse.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.TableGenerator;

@Entity
public class PeerReview {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "prGen")
    @TableGenerator(
        name = "prGen",
        table = "prGenseq",
        pkColumnValue = "pr",
        valueColumnName = "nextpr",
        initialValue = 0,
        allocationSize = 1
    )
	private Long id;
	@OneToMany(fetch = FetchType.EAGER)
	private List<Comment>comments;
	@ManyToOne
	private Assessment assessment;
	private Date created;
	private Date completed;
	private String appsum_notes;
	private String risk_notes;
	private Date acceptedEdits;
	
	
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCompleted() {
		return completed;
	}
	public void setCompleted(Date completed) {
		this.completed = completed;
	}
	public Date getAcceptedEdits() {
		return acceptedEdits;
	}
	public void setAcceptedEdits(Date acceptedEdits) {
		this.acceptedEdits = acceptedEdits;
	}
	
	
	

}
