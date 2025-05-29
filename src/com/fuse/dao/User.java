package com.fuse.dao;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "userGen")
	@TableGenerator(name = "userGen", table = "userGenseq", pkColumnValue = "user", valueColumnName = "nextUser", initialValue = 1, allocationSize = 1)
	private Long Id;
	private String fname;
	private String lname;
	private String email;
	private String username;
	private String passhash;
	private boolean inActive = false;
	private String avatarGuid;
	@ManyToOne
	private Teams team;
	@ManyToOne(cascade = CascadeType.ALL)
	private Permissions permissions;
	@Transient
	private int count;
	@Transient
	private int vcount;
	@Transient
	private int ocount;
	private Date lastLogin;
	private Date loginTime;
	private Integer failedAuth;
	private String authMethod;
	private String ldapUserDn = "";

	public User(Long id, String first, String last) {
		this.fname = first;
		this.lname = last;
		this.Id = id;
	}

	public User() {
	}

	public long getId() {
		return Id;
	}

	public void setId(long id) {
		Id = id;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasshash() {
		return passhash;
	}

	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

	public Teams getTeam() {
		return team;
	}

	public void setTeam(Teams team) {
		this.team = team;
	}

	public void setId(Long id) {
		Id = id;
	}

	public boolean isInActive() {
		return inActive;
	}

	public void setInActive(boolean inActive) {
		this.inActive = inActive;
	}

	@Transient
	public int getAssessmentCount() {
		return count;
	}

	@Transient
	public void setAssessmentCount(int count) {
		this.count = count;
	}

	@Transient
	public int getVerificationCount() {
		return vcount;
	}

	@Transient
	public void setVerificationCount(int count) {
		this.vcount = count;
	}

	@Transient
	public int getOOOCount() {
		return ocount;
	}

	@Transient
	public void setOOOCount(int count) {
		this.ocount = count;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public String getAvatarGuid() {
		return avatarGuid;
	}

	public void setAvatarGuid(String avatarGuid) {
		this.avatarGuid = avatarGuid;
	}

	public Integer getFailedAuth() {
		return failedAuth;
	}

	public void setFailedAuth(Integer failedAuth) {
		this.failedAuth = failedAuth;
	}

	public String getAuthMethod() {
		if (this.authMethod == null)
			return "Native";
		else
			return authMethod;
	}

	public void setAuthMethod(String authMethod) {
		if (authMethod.equals("Native") || authMethod.equals("LDAP") || authMethod.equals("OAUTH2.0") || authMethod.equals("SAML2"))
			this.authMethod = authMethod;
		else
			this.authMethod = "Native";
	}

	public String getLdapUserDn() {
		return ldapUserDn;
	}

	public void setLdapUserDn(String ldapUserDn) {
		this.ldapUserDn = ldapUserDn;
	}
	
	

}
