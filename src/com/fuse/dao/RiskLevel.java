package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class RiskLevel {
	
	@Id
	@GeneratedValue
	private Long id;
	private int riskId;
	private String risk;
	private Integer daysTillDue;
	private Integer daysTillWarning;
	
	public Long getId() {
		return id;
	}
	public int getRiskId() {
		return riskId;
	}
	public String getRisk() {
		return risk;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setRiskId(int riskId) {
		this.riskId = riskId;
	}
	public void setRisk(String risk) {
		this.risk = risk;
	}
	public Integer getDaysTillDue() {
		return daysTillDue;
	}
	public Integer getDaysTillWarning() {
		return daysTillWarning;
	}
	public void setDaysTillDue(Integer daysTillDue) {
		this.daysTillDue = daysTillDue;
	}
	public void setDaysTillWarning(Integer daysTillWarning) {
		this.daysTillWarning = daysTillWarning;
	}
	
	
	

}
