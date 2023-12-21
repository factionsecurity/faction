package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class AppStore {
	
	@Id
	@GeneratedValue
	private Long id;
	private Long order;
	private String description;
	private String location;
	private Boolean approved;
	private Boolean enabled;
	

}
