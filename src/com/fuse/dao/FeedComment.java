package com.fuse.dao;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity
public class FeedComment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "fcommentGen")
    @TableGenerator(
        name = "fcommentGen",
        table = "fcommentGenseq",
        pkColumnValue = "fcomment",
        valueColumnName = "nextfcomment",
        initialValue = 0,
        allocationSize = 1
    )
	private Long id;
	@ManyToOne
	private User commenter;
	private Date dateOfComment;
	private String comment;
	private String guid;
	
	public FeedComment(){
		UUID uuid = UUID.randomUUID();
		this.guid = uuid.toString();
	}
	public User getCommenter() {
		return commenter;
	}
	public void setCommenter(User commenter) {
		this.commenter = commenter;
	}
	public Date getDateOfComment() {
		return dateOfComment;
	}
	public void setDateOfComment(Date dateOfComment) {
		this.dateOfComment = dateOfComment;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	
	

}
