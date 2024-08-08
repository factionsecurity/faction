package com.fuse.utils;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;

public class Item{
	private User assessor;
	private String appid;
	private String appname;
	private String desc;
	private String info;
	private Date due;
	private String type;
	private Date opened;
	private String severity;
	private Long vulnid;
	
	public Item() {
		
	}
	
	public User getAssessor() {
		return assessor;
	}
	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public Date getDue() {
		return due;
	}
	public void setDue(Date due) {
		this.due = due;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = "<b style=\"color:black\">" + type + "</b>";
		if(type.equals("Verification"))
			this.type = this.type.replace("black", "#00a65a");
		else if(type.equals("Vulnerability") )
			this.type = this.type.replace("black", "#00c0ef");
		
		
	}
	public Date getOpened() {
		return opened;
	}
	public void setOpened(Date opened) {
		this.opened = opened;
	}
	public String getSeverity() {
		return severity;
	}
	/*public void setSeverity(String severity) {
		this.severity = "<b style=\"color:black\">" + severity + "</b>";
		if(severity.equals("Critical"))
			this.severity = this.severity.replace("black", "red");
		else if(severity.equals("High"))
			this.severity = this.severity.replace("black", "orange");
		else if(severity.equals("Medium"))
			this.severity = this.severity.replace("black", "yellow");
		else if(severity.equals("Low"))
			this.severity = this.severity.replace("black", "blue");
	}*/
	public void setSeverity(EntityManager em, Long riskId, List<RiskLevel>levels) {
		this.severity = levels.get(riskId.intValue()).getRisk();
		//this.severity = "<b style=\"color:black\">" + levels.get(riskId.intValue()).getRisk() + "</b>";
		/*String[] colors = new String [] {"red", "orange", "yellow", "blue", "aqua","green"};
		int c=0;
		for(int i=9; i>=0; i--){
			String risk = levels.get(i).getRisk();
			if(risk == null || risk.equals("") || risk.toLowerCase().equals("unassigned"))
				continue;
			else if (riskId.intValue() == i && c < colors.length){
				this.severity = this.severity.replace("black", colors[c]);
			}else
				c++;
		}*/
	}
	public Long getVulnid() {
		return vulnid;
	}
	public void setVulnid(Long vulnid) {
		this.vulnid = vulnid;
	}
	
	
}