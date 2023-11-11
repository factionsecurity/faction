package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "catGen")
    @TableGenerator(
        name = "catGen",
        table = "catGenseq",
        pkColumnValue = "cat",
        valueColumnName = "nextCat",
        initialValue = 1,
        allocationSize = 1
    )
	private long id;
	private String name;
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
	
	

}
