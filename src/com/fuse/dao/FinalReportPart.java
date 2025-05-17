package com.fuse.dao;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class FinalReportPart {
	@Id
	private String id = UUID.randomUUID().toString();
	private Integer index=0;
	private String base64Part;

}
