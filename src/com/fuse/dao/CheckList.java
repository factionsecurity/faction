package com.fuse.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class CheckList {

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval=true)
	private List<CheckListItem> questions = new ArrayList();
	@ElementCollection
	private List<Integer> types = new ArrayList();
	
	
	
	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public List<CheckListItem> getQuestions() {
		return questions;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setQuestions(List<CheckListItem> questions) {
		this.questions = questions;
	}
	public List<Integer> getTypes() {
		return types;
	}
	public void setTypes(List<Integer> types) {
		this.types = types;
	}
	
	
	
}
