package com.fuse.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.utils.AccessControl;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/register/newuser.jsp")
public class Register extends FSActionSupport {

	private String uid = "";
	private String password = "";
	private String confirm = "";
	private String username = "";
	private String message = "";

	@Action(value = "Register", results = {
			@Result(name = "gotologin", type = "redirectAction", location = "../login"), })
	public String execute() {

		// Session session = HibHelper.getSessionFactory().openSession();
		String hash = AccessControl.HashPass("", uid);
		User u = (User) em.createQuery("from User where passhash = :hash").setParameter("hash", hash).getResultList()
				.stream().findFirst().orElse(null);
		if (u == null) {
			AuditLog.audit(this, "Registration Link was not valid", AuditLog.Login, true);
			message = "Link is no longer valid.";
			return SUCCESS;
		}
		this.username = u.getUsername();

		if (this.request.getMethod().equals("GET"))
			return SUCCESS;

		message = errors();
		if (message.equals("")) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			String hash2 = AccessControl.HashPass(username.toLowerCase(), password);
			u.setPasshash(hash2);
			u.setFailedAuth(0);
			em.persist(u);
			AuditLog.audit(username, this, "Rest of password was successfull for " + username, AuditLog.Login, false);
			HibHelper.getInstance().commit();

			return "gotologin";
		} else {
			AuditLog.audit(username, this, "Rest of password was successfull for " + username, AuditLog.Login, true);
			return ERRORJSON;
		}

	}

	private String errors() {
		// Pattern upper = Pattern.compile("[A-Z]");
		Pattern number = Pattern.compile("[0-9]");
		Pattern lower = Pattern.compile("[a-z]");

		// Matcher upMatch = upper.matcher(password);
		Matcher numMatch = number.matcher(password);
		Matcher lowMatch = lower.matcher(password);

		if (confirm.equals("") || password.equals(""))
			return "Invalid Password";
		else if (!password.equals(confirm))
			return "Passwords Don't Match";
		else if (password.length() < 8)
			return "Password Must be 8 Characters in Length";
		// else if(!upMatch.find())
		// return "Password Must Have One Upper Case Character";
		else if (!lowMatch.find())
			return "Password Must Have One Lower Case Character";
		else if (!numMatch.find())
			return "Password Must Have One Number";

		return "";
	}

	public String getUsername() {
		return username;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getMessage() {
		return message;
	}

}
