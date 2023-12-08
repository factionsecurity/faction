package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

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
