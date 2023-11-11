package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class Ratings {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "rateGen")
    @TableGenerator(
        name = "rateGen",
        table = "rateGenseq",
        pkColumnValue = "rate",
        valueColumnName = "nextRate",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String Name;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	

}
