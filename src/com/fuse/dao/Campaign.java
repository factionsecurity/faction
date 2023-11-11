package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class Campaign {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "campGen")
    @TableGenerator(
        name = "campGen",
        table = "campGenseq",
        pkColumnValue = "camp",
        valueColumnName = "nextCamp",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String name;
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
	

}
