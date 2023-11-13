package com.fuse.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class BoilerPlate {
	
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private User user;
	private String title;
	private String text;
	private Boolean exploit;
	private String type;
	private Boolean global;
	private Boolean active;
	private Date created;
	
	public Long getId() {
		return id;
	}
	public User getUser() {
		return user;
	}
	public String getTitle() {
		return title;
	}
	public String getText() {
		return text;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Boolean getExploit() {
		return exploit;
	}
	public void setExploit(Boolean exploit) {
		this.exploit = exploit;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getGlobal() {
		return global;
	}
	public void setGlobal(Boolean global) {
		this.global = global;
	}
	public Boolean getActive() {
		if(active == null)
			return true;
		else
			return active;
	}
	public void setActive(Boolean active) {
		if(active == null)
			this.active = true;
		else
			this.active = active;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	

}
