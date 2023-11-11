package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class ReportPage {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "repGen")
    @TableGenerator(
        name = "repGen",
        table = "repGenseq",
        pkColumnValue = "rep",
        valueColumnName = "nextRep",
        initialValue = 1,
        allocationSize = 1
    )
	private long id;
	private String name;
	private long order;
	private boolean front;
	private String content;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getOrder() {
		return order;
	}
	public void setOrder(long order) {
		this.order = order;
	}
	public boolean isFront() {
		return front;
	}
	public void setFront(boolean front) {
		this.front = front;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
	

}
