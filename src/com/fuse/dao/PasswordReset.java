package com.fuse.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

import lombok.Data;


@Entity
@Data
public class PasswordReset {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "resetGen")
    @TableGenerator(
        name = "resetGen",
        table = "resetGenseq",
        pkColumnValue = "reset",
        valueColumnName = "nextReset",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String key;
	private Date created;
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;
}
