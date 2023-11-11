package com.fuse.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fuse.actions.FSActionSupport;

@Entity
public class AuditLog {
	
	//these are related to other DAO objects
	@Transient
	 public static final String CompAssessment = "Assessment";
	@Transient
	 public static final String CompVulnerability = "Vulnerability";
	@Transient
	 public static final String CompDefaultVuln = "DefaultVulnerability";
	@Transient
	 public static final String CompVerification = "Verification";
	@Transient
	 public static final String CompPeerReview = "PeerReview";
	@Transient
	 public static final String CompChecklist = "CheckList";
	@Transient
	 public static final String CompUser = "User";
	
	//These are the type of log
	@Transient
	 public static final String Login = "Login";
	@Transient
	 public static final String UserAction = "UserAction";
	@Transient
	 public static final String APIEvent = "APIEvent";
	@Transient
	 public static final String RestEvent = "RestEvent";

	
	 
	 
	@Id
	@GeneratedValue
	private Long id;
	@OneToOne(fetch=FetchType.LAZY)
	@NotFound(action = NotFoundAction.IGNORE)
	private User user;
	private String type;
	private String description;
	private Date timestamp;
	private Long compid;
	private String alertname;
	private String username;
	private String url;
	private String compname;
	
	private String ip;
	public Long getId() {
		return id;
	}
	public User getUser() {
		return user;
	}
	public String getType() {
		return type;
	}
	public String getDescription() {
		return description;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public Long getCompid() {
		return compid;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public void setCompid(Long compid) {
		this.compid = compid;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	
	
	public String getAlertname() {
		return alertname;
	}
	public String getUsername() {
		return username;
	}
	public String getUrl() {
		return url;
	}
	public void setAlertname(String alertname) {
		this.alertname = alertname;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getCompname() {
		return compname;
	}
	public void setCompname(String compname) {
		this.compname = compname;
	}
	
	@Transient
	public static void saveLog(EntityManager em, User user, HttpServletRequest  servlet, String type, String alert, 
			String description, String compname, Long compid, boolean newTransaction) {
		
		if(newTransaction) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
		}
		AuditLog al = new AuditLog();
		al.setDescription(description);
		al.setCompid(compid);
		al.setUser(user);
		al.setIp(servlet.getRemoteAddr());
		al.setUrl( servlet.getRequestURI());
		al.setAlertname(alert);
		al.setCompname(compname);
		if(user == null)
			al.setUsername("None");
		else
			al.setUsername(user.getUsername());
		
		al.setType(type);
		al.setTimestamp(new Date());
		em.persist(al);
		if(newTransaction) 
			HibHelper.getInstance().commit();
		
	}
	@Transient
	public static void saveLog(FSActionSupport action, String type, String alert, 
			String description, String compname, Long compid, boolean newTransaction) { 
		User u = action.getSessionUser();
		EntityManager em = action.em;
		if(newTransaction) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
		}
		AuditLog al = new AuditLog();
		al.setDescription(description);
		al.setCompid(compid);
		al.setUser(action.getSessionUser());
		al.setIp(  action.request.getRemoteAddr());
		al.setUrl( action.request.getRequestURI());
		al.setAlertname(alert);
		al.setCompname(compname);
		if(u == null)
			al.setUsername("None");
		else
			al.setUsername(u.getUsername());
		
		al.setType(type);
		al.setTimestamp(new Date());
		em.persist(al);
		if(newTransaction) 
			HibHelper.getInstance().commit();
		
	}
	@Transient
	public static void saveLog(String username, FSActionSupport action, String type, String alert, 
			String description, String compname, Long compid, boolean newTransaction) {
		User u = action.getSessionUser();
		EntityManager em = action.em;
		if(newTransaction) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
		}
		AuditLog al = new AuditLog();
		al.setDescription(description);
		al.setCompid(compid);
		al.setUser(action.getSessionUser());
		al.setIp(  action.request.getRemoteAddr());
		al.setUrl( action.request.getRequestURI());
		al.setAlertname(alert);
		al.setCompname(compname);
		al.setUsername(username);
		al.setType(type);
		al.setTimestamp(new Date());
		em.persist(al);
		if(newTransaction) 
			HibHelper.getInstance().commit();
		
	}
	
	@Transient
	public static String notAuthorized(FSActionSupport action, String description, boolean trans) {
		saveLog(action,Login, "Not Authorized", description, null, null, trans);
		return action.LOGIN;
	}
	@Transient
	public static String notAuthorized(String username, FSActionSupport action, String description, boolean trans) {
		saveLog(username, action,Login, "Not Authorized", description, null, null, trans);
		return action.LOGIN;
	}
	
	@Transient
	public static void error(FSActionSupport action,  String description, String type,boolean trans) {
		saveLog(action,type, "Error", description, null, null, trans);
	}
	@Transient
	public static void error(String username, FSActionSupport action,  String description, String type,boolean trans) {
		saveLog(username, action,type, "Error", description, null, null, trans);
	}
	@Transient
	public static void security(FSActionSupport action,  String description, String type,boolean trans) {
		saveLog(action,type, "Security Event", description,null, null, trans);
	}
	
	@Transient
	public static void audit(FSActionSupport action,  String description, String type,boolean trans) {
		saveLog(action,type, "Audit", description, null, null, trans);
	}
	@Transient
	public static void audit(FSActionSupport action,  String description, String type, String compName, Long compid, boolean trans) {
		saveLog(action,type, "Audit", description, compName, compid, trans);
	}
	@Transient
	public static void audit(String username, FSActionSupport action, String description,String type, boolean trans) {
		saveLog(username, action,type, "Audit", description, null, null, trans);
	}
	
	
	

}
