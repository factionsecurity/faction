package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity
public class ReportTemplates {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "repTemp")
    @TableGenerator(
        name = "repTemp",
        table = "repTempseq",
        pkColumnValue = "repTemp",
        valueColumnName = "nextrepTemp",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String name;
	private String variable;
	private String filename;
	@ManyToOne
	private Teams team;
	@ManyToOne
	private AssessmentType type;
	private boolean retest = false;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Teams getTeam() {
		return team;
	}
	public void setTeam(Teams team) {
		this.team = team;
	}
	public AssessmentType getType() {
		return type;
	}
	public void setType(AssessmentType type) {
		this.type = type;
	}
	public boolean isRetest() {
		return retest;
	}
	public void setRetest(boolean retest) {
		this.retest = retest;
	}
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	
	
	

}
