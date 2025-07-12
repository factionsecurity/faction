package com.fuse.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class CustomField {
	
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private CustomType type;
	private String value;
	public Long getId() {
		return id;
	}
	public CustomType getType() {
		return type;
	}
	public String getValue() {
		return value;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setType(CustomType type) {
		this.type = type;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
