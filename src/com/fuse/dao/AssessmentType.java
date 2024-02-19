package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class AssessmentType{
	
	
	@Id
	@GeneratedValue
	private Long id;
	private String type;
	private Boolean cvss31;
	private Boolean cvss40;

	public AssessmentType(){
	}
	public AssessmentType(Long id, String type){
		this.id = id;
		this.type=type;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Transient
	public void setRatingSystem(String RatingSystem) {
		switch(RatingSystem) {
			case "CVSS 3.1": 
				this.cvss31 = true;
				this.cvss40 = false;
				break;
			case "CVSS 4.0":
				this.cvss40 = true;
				this.cvss31 = false;
				break;
			default: 
				this.cvss31 = false;
				this.cvss40 = false;
		}
	}
	
	@Transient
	public Boolean isNativeRiskRanking() {
		return !( this.isCvss40() || this.isCvss31() );
	}
	@Transient
	public Boolean isCvss31() {
		return this.cvss31 != null && this.cvss31;
	}
	@Transient
	public Boolean isCvss40() {
		return this.cvss40 != null && this.cvss40;
	}
	@Transient
	public String getRatingSystemName() {
		if(this.isCvss31()) {
			return "CVSS 3.1";
		}else if(this.isCvss40()) {
			return "CVSS 4.0";
		}else {
			return "Native (default)";
		}
	}
	
	
	

}
