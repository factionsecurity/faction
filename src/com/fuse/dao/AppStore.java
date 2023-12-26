package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class AppStore {
	
	@Id
	@GeneratedValue
	private Long id;
	private Integer order;
	private String name;
	private String author;
	private String url;
	private String description;
	private String location;
	private Boolean approved;
	private Boolean enabled;
	private String base64Logo;
	private Boolean AssessmentEnabled;
	private Boolean VerificationEnabled;
	private Boolean VulnerabilityEnabled;
	private Boolean InventoryEnabled;
	

}
