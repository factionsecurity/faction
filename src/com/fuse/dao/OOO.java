package com.fuse.dao;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity
public class OOO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "oooGen")
    @TableGenerator(
        name = "oooGen",
        table = "oooGenseq",
        pkColumnValue = "ooo",
        valueColumnName = "nextooo",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String title;
	private Date start;
	private Date end;
	@ManyToOne
	private User user;


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	
	
	
	
	
	

	
	
	

}
