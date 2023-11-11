package com.fuse.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class CheckListItem {
	
	@Id
	@GeneratedValue
	private Long id;
	private String Question;
	@OneToOne
	private CheckList checklist;
	
	@OneToMany
	private List<DefaultVulnerability> mappingVulns = new ArrayList();
	
	public Long getId() {
		return id;
	}
	public String getQuestion() {
		return Question;
	}
	public CheckList getChecklist() {
		return checklist;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setQuestion(String question) {
		Question = question;
	}
	public void setChecklist(CheckList checklist) {
		this.checklist = checklist;
	}
	public List<DefaultVulnerability> getMappingVulns() {
		return mappingVulns;
	}
	public void setMappingVulns(List<DefaultVulnerability> mappingVulns) {
		this.mappingVulns = mappingVulns;
	}
	
	
	
	

}
