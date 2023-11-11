package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity
public class VerificationItem {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "verItemGen")
    @TableGenerator(
        name = "verItemGen",
        table = "verItemGenseq",
        pkColumnValue = "verItem",
        valueColumnName = "nextVerItem",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	@ManyToOne
	private Vulnerability vulnerability;
	private boolean pass;
	private String notes;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean isPass() {
		return pass;
	}
	public void setPass(boolean pass) {
		this.pass = pass;
	}
	public Vulnerability getVulnerability() {
		return vulnerability;
	}
	public void setVulnerability(Vulnerability vulnerability) {
		this.vulnerability = vulnerability;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	
	
	

}
