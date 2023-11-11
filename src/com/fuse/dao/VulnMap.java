package com.fuse.dao;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class VulnMap {

	@Id
	@GeneratedValue
	private Long id;
	private String originTitle;
	@OneToOne
	private DefaultVulnerability targetVuln;
	public Long getId() {
		return id;
	}
	public String getOriginTitle() {
		return originTitle;
	}
	public DefaultVulnerability getTargetVuln() {
		return targetVuln;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setOriginTitle(String originTitle) {
		this.originTitle = originTitle;
	}
	public void setTargetVuln(DefaultVulnerability targetVuln) {
		this.targetVuln = targetVuln;
	}
	
	
	
}
