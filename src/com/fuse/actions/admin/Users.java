package com.fuse.actions.admin;

import java.util.Date;
import java.util.List;
import java.util.UUID;


import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.authentication.LDAPValidator;
import com.fuse.authentication.oauth.SecurityConfigFactory;
import com.fuse.dao.APIKeys;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PasswordReset;
import com.fuse.dao.Permissions;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.query.UserQueries;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.AccessControl;
import com.fuse.utils.FSUtils;
import com.fuse.utils.SendEmail;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/users/Users.jsp")
public class Users extends FSActionSupport {

	private List<User> users;
	private List<Teams> teams;
	private String team_name;
	private String update;
	private String delete;
	private String team_id;
	private String userId;
	private User selectedUser;
	private String fname;
	private String lname;
	private String team;
	private String email;
	private String create;
	private String username;
	private boolean mgr;
	private boolean eng;
	private boolean rem;
	private boolean inactive;
	private boolean exec;
	private boolean assessor;
	private boolean admin;
	private boolean api;
	private String action = "";
	private String apiKey = "";
	private Integer uioli = 0;
	private Integer accesscontrol = 2;
	private String platformTier = "";
	private String authMethod = "native";
	private String ldapURL;
	private String ldapBaseDn;
	private String ldapUserName;
	private String ldapPassword;
	private String ldapSecurity;
	private Boolean isInsecure=false;
	private String ldapObjectClass;
	private String credential;
	private String oauthClientId;
	private String oauthClientSecret;
	private String oauthDiscoveryURI;
	private String saml2MetaUrl;

	@Action(value = "Users")
	public String execute() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		User user = this.getSessionUser();

		teams = em.createQuery("from Teams").getResultList();
		users = em.createQuery("from User").getResultList();
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ems != null) {
			this.uioli = ems.getInactiveDays();
			this.ldapURL = ems.getLdapURL() != null ? ems.getLdapURL() : "";
			this.ldapBaseDn = ems.getLdapBaseDn();
			this.ldapUserName = ems.getLdapBindDn();
			this.ldapSecurity = ems.getLdapSecurity();
			this.isInsecure = ems.getLdapInsecureSSL();
			this.ldapObjectClass = ems.getLdapObjectClass();
			this.oauthClientId = ems.getOauthClientId();
			this.oauthDiscoveryURI = ems.getOauthDiscoveryURI();
			this.saml2MetaUrl = ems.getSaml2MetaUrl();
		}

		return SUCCESS;
	}

	@Action(value = "GetUser", results = {
			@Result(name = "userJSON", location = "/WEB-INF/jsp/users/userInfoJson.jsp") })
	public String getUser() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		User user = this.getSessionUser();
		Long id = Long.parseLong(userId.replace("user", ""));
		this.selectedUser = (User) em.createQuery("from User where Id = :uid").setParameter("uid", id).getResultList()
				.stream().findFirst().orElse(null);
		if (this.selectedUser == null)
			return this.ERRORJSON;
		APIKeys api = (APIKeys) em.createQuery("from APIKeys where userid = :id").setParameter("id", id).getResultList()
				.stream().findFirst().orElse(null);
		if (api != null)
			this.apiKey = api.getKey().substring(0, 8) + "-XXXX-XXXX-XXXX-XXXXXXXXXXXX";

		return "userJSON";
	}

	@Action(value = "AddUser")
	public String addUser() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false)) {
			return this.ERRORJSON;
		}

		User user = this.getSessionUser();
		if (this.isNullStirng(this.username) || this.isNullStirng(this.email) || this.isNullStirng(fname)
				|| this.isNullStirng(this.lname)) {
			this._message = "Some inputs are empty";
			return this.ERRORJSON;
		}
		if (!this.checkRoleAdded()) {
			this._message = "Must set at least one Role: Manager, Assessor, Remediation, Engagement, or Admin";
			return this.ERRORJSON;
		}
		Teams t = null;
		try {
			t = (Teams) em.createQuery("from Teams where id = :tid").setParameter("tid", Long.parseLong(this.team))
					.getResultList().stream().findFirst().orElse(null);
		}catch(Exception ex) {
			
		}
		if (t == null) {
			this._message = "Invalid Team Selected";
			return this.ERRORJSON;
		}
		
		PasswordReset reset = null;

		this.username = this.username.toLowerCase();
		User userExists = (User) em.createQuery("from User where username = :username")
				.setParameter("username", this.username.trim()).getResultList().stream().findFirst().orElse(null);

		if (userExists == null) {
			Integer userLimit = getUserLimit();
			if (userLimit != -1) {
				Integer userCount = em.createQuery("from User").getResultList().size();
				if (userCount >= userLimit) {
					this._message = "Upgrade to Add More Users";
					return this.ERRORJSON;
				}
			}
			User u = new User();
			if(this.authMethod.equals("LDAP")) {
				SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
						.findFirst().orElse(new SystemSettings());
				LDAPValidator validator = new LDAPValidator(settings.getLdapURL(), settings.getLdapBaseDn(),
						settings.getLdapBindDn(), settings.getLdapSecurity(), settings.getLdapInsecureSSL());
				String password = FSUtils.decryptPassword(settings.getLdapPassword());
				u = validator.getUserInfo(this.username.trim(), password, settings.getLdapObjectClass());
			}else {
				u.setUsername(this.username.trim());
				u.setEmail(this.email.trim());
				u.setFname(this.fname.trim());
				u.setLname(this.lname.trim());
			}
			this.authMethod = this.authMethod.trim();
			u.setAuthMethod(this.authMethod);
			String message = "Hello " + this.fname + " " + this.lname + "<br><br>";
			if (this.authMethod.equals("LDAP") || this.authMethod.equals("OAUTH2.0") || this.authMethod.equals("SAML2") ) {
				User emailExists = (User) em.createQuery("from User where email = :email")
						.setParameter("email", this.email.trim()).getResultList().stream().findFirst().orElse(null);
				if(emailExists != null) {
					this._message = "Can't have two LDAP or OAuth users with the same email address.";
					return this.ERRORJSON;
				}else {
					String key = UUID.randomUUID().toString();
					u.setPasshash(key);
					message += "Your account has been created. Please access the link below and click to access with your SSO Credentials<br><br>";
					String url = request.getRequestURL().toString();
					url = url.replace(request.getRequestURI(), "");
					url = url + request.getContextPath();
					message += "<a href='" + url + "'>Click here to Login</a><br>";
					
					
				}
			} else if(this.credential != null && !this.credential.trim().equals("")){
				String passErrorMessage = AccessControl.checkPassword(this.credential, this.credential);
				if(passErrorMessage.equals("")) {
					u.setPasshash(AccessControl.HashPass(this.credential));
					message += "Your account has been created. Please access the link below and click to access the portal. You will need to ask your Administrator for the credentials.<br><br>";
					String url = request.getRequestURL().toString();
					url = url.replace(request.getRequestURI(), "");
					url = url + request.getContextPath();
					message += "<a href='" + url + "'>Click here to Login</a><br>";
				}else {
					this._message = passErrorMessage;
					return this.ERRORJSON;
				}
				
			}else {
				String key = UUID.randomUUID().toString();
				u.setPasshash(AccessControl.HashPass(UUID.randomUUID().toString()));
				reset = new PasswordReset();
				reset.setKey(key);
				reset.setUser(u);
				reset.setCreated(new Date());
				message += "Click the link below to update your password:<br><br>";
				String url = request.getRequestURL().toString();
				url = url.replace(request.getRequestURI(), "");
				url = url + request.getContextPath() + "/portal/Register?uid=" + key;
				message += "<a href='" + url + "'>Click here to Register</a><br>";
			}

			Permissions p = new Permissions();
			p.setAdmin(admin);
			p.setEngagement(eng);
			p.setAssessor(assessor);
			p.setManager(mgr);
			p.setAccessLevel(this.accesscontrol);
			u.setPermissions(p);
			u.setTeam(t);
			if (getTier() != "consultant") {
				p.setRemediation(false);
				p.setExecutive(false);

			} else {
				p.setRemediation(rem);
				p.setExecutive(exec);
			}

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(u);
			if(reset != null) {
				em.persist(reset);
			}
			AuditLog.audit(this, "User " + u.getUsername() + " added", AuditLog.UserAction, false);

			HibHelper.getInstance().commit();

			EmailThread emailThread = new EmailThread(this.email, "New Account Created", message);
			TaskQueueExecutor.getInstance().execute(emailThread);
			if (api) {
				APIKeys api = new APIKeys();
				String apistr = UUID.randomUUID().toString();
				api.setUser(u);
				api.setKey(apistr);
				api.setCreated(new Date());
				api.setLastUsed(new Date(0));
				String apimesage = "Hello " + this.fname + " " + this.lname + "<br><br>";
				apimesage += "Below is your new API key. Please keep it in a safe place and do not share it:<br><br>";
				apimesage += "<h2>" + apistr + "</h2><br>";
				(new SendEmail(em)).send(this.email, apimesage, "New API KEY Created");
				em.persist(api);
				HibHelper.getInstance().commit();
				// session.save(api);

			}

			return this.SUCCESSJSON;
		} else {
			this._message = "User Exists";
			return this.ERRORJSON;
		}

	}

	@Action(value = "UpdateAPI")
	public String updateAPI() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		HibHelper.getInstance().preJoin();
		em.joinTransaction();

		Long id = Long.parseLong(userId.replace("user", ""));
		APIKeys api = (APIKeys) em.createQuery("from APIKeys where userid = :id").setParameter("id", id).getResultList()
				.stream().findFirst().orElse(null);
		if (api == null)
			return this.ERRORJSON;
		User u = (User) em.createQuery("from User where Id = :uid").setParameter("uid", id).getResultList().stream()
				.findFirst().orElse(null);
		if (u == null)
			return this.ERRORJSON;

		api.setKey(UUID.randomUUID().toString());
		api.setCreated(new Date());
		api.setLastUsed(new Date(0));
		em.persist(api);
		AuditLog.audit(this, "API Key created for  " + u.getUsername() + "", AuditLog.UserAction, false);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;

	}

	@Action(value = "UpdateUser")
	public String updateUser() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;
		User user = this.getSessionUser();
		Long id = Long.parseLong(userId.replace("user", ""));
		
		this.selectedUser = em.find(User.class, id);
		String currentEmail = selectedUser.getUsername();
		if (this.selectedUser == null) {
			this._message = "Cannot find user to update";
			return this.ERRORJSON;
		}
		if (!this.checkRoleAdded()) {
			this._message = "Must set at least one Role: Manager, Assessor, Remediation, Engagement, or Admin";
			return this.ERRORJSON;
		}
		if (this.isNullStirng(this.username) || this.isNullStirng(this.email) || this.isNullStirng(fname) || this.isNullStirng(this.lname)) {
			this._message = "Some inputs are empty";
			return this.ERRORJSON;
		}
		if(this.authMethod.equals("Native") && this.credential != null && !this.credential.trim().equals("")) {
			String passErrorMessage = AccessControl.checkPassword(this.credential, this.credential);
			if(!passErrorMessage.equals("")) {
					this._message = passErrorMessage;
					return this.ERRORJSON;
			}else {
				this.selectedUser.setPasshash(AccessControl.HashPass(this.credential));
			}
			this.selectedUser.setEmail(this.email.trim());
		}else if(this.authMethod.equals("LDAP")) {
			SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
					.findFirst().orElse(new SystemSettings());
			LDAPValidator validator = new LDAPValidator(settings.getLdapURL(), settings.getLdapBaseDn(),
					settings.getLdapBindDn(), settings.getLdapSecurity(), settings.getLdapInsecureSSL());
			String password = FSUtils.decryptPassword(settings.getLdapPassword());
			User ldapInfo = validator.getUserInfo(this.selectedUser.getUsername(), password, settings.getLdapObjectClass());
			if(ldapInfo!= null) {
				this.selectedUser.setLdapUserDn(ldapInfo.getLdapUserDn());
				this.selectedUser.setEmail(ldapInfo.getEmail());
			}
		}else {
			this.selectedUser.setEmail(this.email.trim());
		}
		User userExists = (User) em.createQuery("from User where username = :username")
				.setParameter("username", this.username.trim()).getResultList().stream().findFirst().orElse(null);
		if(userExists != null && !id.equals(userExists.getId())) {
			this._message = "Username already exists";
			return this.ERRORJSON;
		}
		
		User emailExists = (User) em.createQuery("from User where email = :email")
				.setParameter("email", this.selectedUser.getEmail()).getResultList().stream().findFirst().orElse(null);
		
		if(emailExists != null && !id.equals(emailExists.getId())) {
			this._message = "Email address already in use";
			return this.ERRORJSON;
		}
		
		this.selectedUser.setUsername(username);
		this.selectedUser.setFname(this.fname.trim());
		this.selectedUser.setLname(this.lname.trim());
		this.selectedUser.setAuthMethod(this.authMethod.trim());
		Teams t = em.find(Teams.class, Long.parseLong(this.team));
		if (t == null) {
			this._message = "Could not find a valid team.";
			return this.ERRORJSON;
		}
		this.selectedUser.setTeam(t);
		this.selectedUser.getPermissions().setAdmin(admin);
		this.selectedUser.getPermissions().setEngagement(eng);
		this.selectedUser.getPermissions().setRemediation(rem);
		this.selectedUser.getPermissions().setExecutive(exec);
		this.selectedUser.getPermissions().setAssessor(assessor);
		this.selectedUser.getPermissions().setManager(mgr);
		this.selectedUser.setInActive(inactive);
		this.selectedUser.getPermissions().setAccessLevel(this.accesscontrol);

		if (!this.authMethod.equals("Native")) {
			this.selectedUser.setPasshash(AccessControl.HashPass(UUID.randomUUID().toString()));
		}

		HibHelper.getInstance().preJoin();
		em.joinTransaction();

		APIKeys apikeys = (APIKeys) em.createQuery("from APIKeys where userid = :id").setParameter("id", id)
				.getResultList().stream().findFirst().orElse(null);
		if (this.api && apikeys == null) {

			String apistr = UUID.randomUUID().toString();
			apikeys = new APIKeys();
			apikeys.setUser(this.selectedUser);
			apikeys.setKey(apistr);
			apikeys.setCreated(new Date());
			apikeys.setLastUsed(new Date(0));
			em.persist(apikeys);
			AuditLog.audit(this, "API Key created for  " + this.selectedUser.getUsername() + "", AuditLog.UserAction,
					false);


			String apimesage = "Hello " + this.fname + " " + this.lname + "<br><br>";
			apimesage += "Below is your new API key. Please keep it in a safe place and do not share it:<br><br>";
			apimesage += "<h2>" + apistr + "</h2><br>";

			EmailThread emailThread = new EmailThread(this.email, "New API KEY Created", apimesage);
			TaskQueueExecutor.getInstance().execute(emailThread);

		} else if (!this.api && apikeys != null) {
			em.remove(apikeys);
			AuditLog.audit(this, "API Key removed for  " + this.selectedUser.getUsername() + "", AuditLog.UserAction,
					false);


		}

		em.persist(selectedUser);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}

	@Action(value = "DeleteUser")
	public String deleteUser() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		Long id = Long.parseLong(userId.replace("user", ""));
		this.selectedUser = (User) em.createQuery("from User where Id = :uid").setParameter("uid", id).getResultList()
				.stream().findFirst().orElse(null);

		if (this.selectedUser == null) {
			this._message = "Cannot find user to update";
			return this.ERRORJSON;
		}
		
		if(this.selectedUser.getId() == user.getId()) {
			this._message = "You Cannot Delete Yourself";
			return this.ERRORJSON;
			
		}

		String query = "{ $or: [ { \"assessor\": " + id + " } ,";
		query += "{ \"engagement_Id\": " + id + " }, ";
		query += "{ \"remediation_Id\": " + id + " }]} ";

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		try {
			List<Assessment> assessments = (List<Assessment>) em.createNativeQuery(query, Assessment.class)
					.getResultList();

			if (assessments.size() > 0) {
				this.selectedUser.setInActive(true);
				em.persist(this.selectedUser);
				AuditLog.audit(this, "User " + this.selectedUser.getUsername() + " was made inactive",
						AuditLog.UserAction, false);

			} else {
				em.remove(this.selectedUser);
				AuditLog.audit(this, "User " + this.selectedUser.getUsername() + " was deleted", AuditLog.UserAction,
						false);

			}
		} finally {
			HibHelper.getInstance().commit();
		}
		return this.SUCCESSJSON;
	}

	@Action(value = "CreateTeamName")
	public String createTeamName() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		if (this.isNullStirng(this.team_name)) {
			this._message = "Team name was empty";
			return this.ERRORJSON;
		}
		Teams team = UserQueries.getTeam(em, this.team_name);

		if (team != null) {
			this._message = "Team Name Exists";
			return this.ERRORJSON;
		}

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		try {
			Teams t = new Teams();
			t.setTeamName(team_name.trim());
			em.persist(t);
			AuditLog.audit(this, "Team  " + team_name + " was created", AuditLog.UserAction, false);
		} finally {
			HibHelper.getInstance().commit();
		}
		return this.SUCCESSJSON;
	}

	@Action(value = "DeleteTeamName")
	public String deleteTeamName() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		Long teamId = Long.parseLong(team_id.replace("team", ""));
		List<User> users = UserQueries.getUsersByTeamId(em, teamId);
		if (users.size() != 0) {
			this._message = "Cannot delete a team name that is asigned to other users";
			return this.ERRORJSON;
		}
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		try {
			Teams t = UserQueries.getTeamById(em, teamId);
			em.remove(t);
			AuditLog.audit(this, "Team  " + t.getTeamName() + " was deleted", AuditLog.UserAction, false);
		} finally {
			HibHelper.getInstance().commit();
		}
		return this.SUCCESSJSON;
	}

	@Action(value = "UpdateTeamName")
	public String updateTeamName() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		if (this.isNullStirng(this.team)) {
			this._message = "Team name is empty.";
			return this.ERRORJSON;
		}

		Teams t = UserQueries.getTeamById(em, Long.parseLong(team_id.replace("team", "")));

		if (t == null) {
			this._message = "Could not find the team";
			return this.ERRORJSON;
		}

		Teams t2 = UserQueries.getTeam(em, team);

		if (t2 != null && t2.getId().longValue() != t.getId().longValue()) {
			this._message = "Team Name Exists";
			return this.ERRORJSON;
		}

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		try {
			t.setTeamName(this.team.trim());
			AuditLog.audit(this, "Team  " + this.team + " was updated", AuditLog.UserAction, false);
			em.persist(t);
		} finally {
			HibHelper.getInstance().commit();
		}
		return SUCCESS;

	}

	@Action(value = "Unlock")
	public String unlock() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		User unlocked = em.find(User.class, Long.parseLong(this.userId));
		if (unlocked == null) {
			this._message = "Not a valid user";
			return this.ERRORJSON;
		}

		// If the system has enabled Use it or looseit then reset the last login time
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ems != null) {
			Date ll = unlocked.getLastLogin();
			if(ll != null && ems.getInactiveDays() != null && !ems.getInactiveDays().equals(-1)) {
				java.util.Calendar backdate = java.util.Calendar.getInstance();
				backdate.add(java.util.Calendar.DATE, -ems.getInactiveDays());
				if (ll.getTime() < backdate.getTimeInMillis()) {
					unlocked.setLastLogin(new Date());
				}
			}
		}

		unlocked.setFailedAuth(0);
		unlocked.setInActive(false);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(unlocked);
		AuditLog.audit(this, "User  " + unlocked.getUsername() + " was unlocked", AuditLog.UserAction, false);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}

	@Action(value = "UpdateUIOLI")
	public String updateUioli() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;
		if (this.uioli == null) {
			this._message = "Invalid number of days";
			return this.ERRORJSON;
		}
		if (this.uioli.intValue() > 0 && this.uioli.intValue() < 30) {
			this._message = "Number of days must be greater than 30 if not zero";
			return this.ERRORJSON;
		}

		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ems == null)
			ems = new SystemSettings();
		ems.setInactiveDays(this.uioli);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(ems);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}

	@Action(value = "SaveLDAP")
	public String saveLDAP() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(new SystemSettings());

		settings.setLdapURL(this.ldapURL.trim());
		settings.setLdapBaseDn(this.ldapBaseDn.trim());
		settings.setLdapBindDn(this.ldapUserName.trim());
		if (!this.ldapPassword.trim().equals(""))
			settings.setLdapPassword(FSUtils.encryptPassword(this.ldapPassword.trim()));
		settings.setLdapSecurity(this.ldapSecurity);
		settings.setLdapInsecureSSL(this.isInsecure);
		settings.setLdapObjectClass(this.ldapObjectClass);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(settings);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}

	@Action(value = "TestLDAP")
	public String testLDAP() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(new SystemSettings());

		User user = new User();
		LDAPValidator validator = new LDAPValidator(settings.getLdapURL(), settings.getLdapBaseDn(),
				settings.getLdapBindDn(), settings.getLdapSecurity(), settings.getLdapInsecureSSL());
		String password = FSUtils.decryptPassword(settings.getLdapPassword());
		if (validator.validateBindCredentials(password)) {
			password = "";
			return this.SUCCESSJSON;
		} else {
			return this.ERRORJSON;
		}
	}

	@Action(value = "SearchLDAP", results = {
			@Result(name = "usersJson", location = "/WEB-INF/jsp/users/userListJSON.jsp") })
	public String searchLDAP() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(new SystemSettings());
		LDAPValidator validator = new LDAPValidator(settings.getLdapURL(), settings.getLdapBaseDn(),
				settings.getLdapBindDn(), settings.getLdapSecurity(), settings.getLdapInsecureSSL());
		String password = FSUtils.decryptPassword(settings.getLdapPassword());
		users = validator.searchLdap(username, password, settings.getLdapObjectClass());
		password = "";
		return "usersJson";

	}
	
	@Action(value = "SaveOAUTH")
	public String saveOAUTH() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(new SystemSettings());
		settings.setOauthClientId(this.oauthClientId);
		settings.setOauthDiscoveryURI(this.oauthDiscoveryURI);
		if(this.oauthClientSecret != null && !this.oauthClientSecret.equals("")) {
			settings.setOauthClientSecret(FSUtils.encryptPassword(this.oauthClientSecret));
		}
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(settings);
		HibHelper.getInstance().commit();
		//update the odic config in the filter
		//settings.updateSSOFilters();
		SecurityConfigFactory.refreshConfig();
		
		return this.SUCCESSJSON;
	}
	
	@Action(value = "SaveSAML2")
	public String saveSAML2() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(new SystemSettings());
		settings.setSaml2MetaUrl(this.saml2MetaUrl);
		settings.createKeystoreIfNotExists();
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(settings);
		HibHelper.getInstance().commit();
		SecurityConfigFactory.refreshConfig();
		
		return this.SUCCESSJSON;
	}

	public boolean checkRoleAdded() {

		if (admin == true)
			return true;
		else if (assessor == true)
			return true;
		else if (rem == true)
			return true;
		else if (eng == true)
			return true;
		else if (this.mgr == true)
			return true;
		else
			return false;
	}

	public String getActiveUsers() {
		return "active";
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<Teams> getTeams() {
		return teams;
	}

	public void setTeams(List<Teams> teams) {
		this.teams = teams;
	}

	public String getTeam_name() {
		return team_name;
	}

	public void setTeam_name(String team_name) {
		this.team_name = team_name;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}

	public String getTeam_id() {
		return team_id;
	}

	public void setTeam_id(String team_id) {
		this.team_id = team_id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public User getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(User selectedUser) {
		this.selectedUser = selectedUser;
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

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCreate() {
		return create;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isMgr() {
		return mgr;
	}

	public void setMgr(boolean mgr) {
		this.mgr = mgr;
	}

	public boolean isEng() {
		return eng;
	}

	public void setEng(boolean eng) {
		this.eng = eng;
	}

	public boolean isRem() {
		return rem;
	}

	public void setRem(boolean rem) {
		this.rem = rem;
	}

	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	public boolean isExec() {
		return exec;
	}

	public void setExec(boolean exec) {
		this.exec = exec;
	}

	public boolean isAssessor() {
		return assessor;
	}

	public void setAssessor(boolean assessor) {
		this.assessor = assessor;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isApi() {
		return api;
	}

	public void setApi(boolean api) {
		this.api = api;
	}

	public String getApiKey() {
		return apiKey;
	}


	public Integer getUioli() {
		return uioli;
	}

	public void setUioli(Integer uioli) {
		this.uioli = uioli;
	}

	public Integer getAccesscontrol() {
		return accesscontrol;
	}

	public void setAccesscontrol(Integer accesscontrol) {
		this.accesscontrol = accesscontrol;
	}

	public String getTier() {
		return FSUtils.getEnv("FACTION_TIER");
	}

	public int getUserLimit() {
		String limit = FSUtils.getEnv("FACTION_USERS");
		return limit == "" ? 100: Integer.parseInt(limit);
	}

	public String getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	public String getLdapURL() {
		return ldapURL;
	}

	public void setLdapURL(String ldapURL) {
		this.ldapURL = ldapURL;
	}

	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
	}

	public String getLdapUserName() {
		return ldapUserName;
	}

	public void setLdapUserName(String ldapUserName) {
		this.ldapUserName = ldapUserName;
	}

	public String getLdapPassword() {
		return ldapPassword;
	}

	public void setLdapSecurity(String ldapSecurity) {
		this.ldapSecurity = ldapSecurity;
	}

	public String getLdapSecurity() {
		return ldapSecurity;
	}

	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}

	public Boolean getIsInsecure() {
		return isInsecure;
	}

	public void setIsInsecure(Boolean isInsecure) {
		this.isInsecure = isInsecure;
	}

	public String getLdapObjectClass() {
		return ldapObjectClass;
	}

	public void setLdapObjectClass(String ldapObjectClass) {
		this.ldapObjectClass = ldapObjectClass;
	}

	public String getCredential() {
		return credential;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	public String getOauthClientId() {
		return oauthClientId;
	}

	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}

	public String getOauthDiscoveryURI() {
		return oauthDiscoveryURI;
	}

	public void setOauthDiscoveryURI(String oauthDiscoveryURI) {
		this.oauthDiscoveryURI = oauthDiscoveryURI;
	}

	public void setOauthClientSecret(String oauthClientSecret) {
		this.oauthClientSecret = oauthClientSecret;
	}
	
	public void setSaml2MetaUrl(String saml2MetaUrl) {
		this.saml2MetaUrl = saml2MetaUrl;
	}
	public String getSaml2MetaUrl() {
		return this.saml2MetaUrl;
	}
	
	

}
