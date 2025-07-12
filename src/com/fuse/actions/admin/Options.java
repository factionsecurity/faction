package com.fuse.actions.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Campaign;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.HibHelper;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.utils.FSUtils;
import com.fuse.utils.SendEmail;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/admin/Options.jsp")
public class Options extends FSActionSupport {

	private String action;
	private Long id;
	private String name;
	private List<AssessmentType> types;
	private List<Campaign> campaigns;
	private String server;
	private String type;
	private String port;
	private String username;
	private String fromaddress;
	private String password;
	private SystemSettings EMS;
	private String prefix;
	private String signature;
	private String webport;
	private String tmp;
	private String pdf;
	private String authChecked;
	private String tlsChecked;
	private String sslChecked;
	private boolean authischecked;
	private boolean tlsischecked;
	private boolean sslischecked;
	private String sender;
	private String to;
	private List<CustomType> custom = new ArrayList();
	private String cfname;
	private String cfvar;
	private Integer cftype;
	private Integer cffieldtype;
	private String cfdefault;
	private String prChecked;
	private String feedChecked;
	private String[] title = { "FACTION", "oss" };
	private String message;
	private String randChecked;
	private Boolean readonly;
	private String clientid;
	private String profile;
	private String status;
	private String selfPeerReview;
	private String riskName;
	private Long riskId;
	private Integer riskType;
	private Boolean selected;

	@Action(value = "Options")
	public String execute() {

		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return AuditLog.notAuthorized(this, "User is not Admin, Manager, or Engagement", true);
		}
		User user = this.getSessionUser();

		// Session session = HibHelper.getSessionFactory().openSession();

		types = (List<AssessmentType>) em.createQuery("from AssessmentType").getResultList();
		campaigns = (List<Campaign>) em.createQuery("from Campaign").getResultList();
		EMS = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
		custom = (List<CustomType>) em.createQuery("from CustomType where deleted = false or deleted IS NULL").getResultList();
		if (EMS != null) {
			if(EMS.getServer() == null || EMS.getServer().equals("")) {
				EMS.initSMTPSettings();
			}
		}else {
			EMS = new SystemSettings();
			EMS.initSMTPSettings();
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(EMS);
			HibHelper.getInstance().commit();
		}
		if (EMS.getEmailAuth() != null && EMS.getEmailAuth())
			authChecked = "checked";
		if (EMS.getEmailSSL() != null && EMS.getEmailSSL())
			sslChecked = "checked";
		if (EMS.getTls() != null && EMS.getTls())
			tlsChecked = "checked";

		this.prChecked = EMS.getPeerreview() == null || !EMS.getPeerreview() ? "" : "checked";
		this.feedChecked = EMS.getEnablefeed() == null || !EMS.getEnablefeed() ? "" : "checked";
		this.randChecked = EMS.getEnableRandAppId() == null || !EMS.getEnableRandAppId() ? "" : "checked";
		this.selfPeerReview = EMS.getSelfPeerReview() == null || !EMS.getSelfPeerReview() ? "" : "checked";
		this.title[0] = EMS.getBoldTitle() == null ? "FACTION" : EMS.getBoldTitle();
		this.title[1] = EMS.getOtherTitle() == null ? "community" : EMS.getOtherTitle();
		
		

		if (action != null && action.equals("addType") && this.name != null && !this.name.equals("")) {
			if (!this.testToken(false))
				return this.ERRORJSON;
			if (this.name == null || this.name.equals("")) {
				this._message = "Name is Empty";
				return this.ERRORJSON;
			}
			AssessmentType AT = AssessmentQueries.getAssessmentTypeByName(em, this.name);

			if (AT != null) {
				this._message = "Type already exists with this name.";
				return this.ERRORJSON;
			}

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			AssessmentType type = new AssessmentType();
			type.setType(this.name.trim());
			
			switch(this.riskType) {
				case 1: type.setRatingSystem("CVSS 3.1"); break;
				case 2: type.setRatingSystem("CVSS 4.0"); break;
				default: type.setRatingSystem("Native");
			}
			em.persist(type);
			AuditLog.audit(this, "Assessment Type " + this.name + " added", AuditLog.UserAction, false);
			HibHelper.getInstance().commit();

		} else if (action != null && action.equals("delType") && this.id != null) {
			if (!this.testToken(false))
				return this.ERRORJSON;

			String mongo = "db.Assessment.count({ \"type_id\": " + this.getId() + "})";
			Long count = (Long) em.createNativeQuery(mongo).getSingleResult();
			if (count > 0l) {
				this.message = "Can't Delete since the value has been assigned to an assessment";

				return this.ERRORJSON;
			}
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			AssessmentType type = em.find(AssessmentType.class, this.id);
			em.remove(type);
			AuditLog.audit(this, "Assessment Type " + type.getType() + " deleted", AuditLog.UserAction, false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;

		} else if (action != null && action.equals("addCampaign") && this.name != null && !this.name.equals("")) {
			if (!this.testToken(false))
				return this.ERRORJSON;

			if (this.name == null || this.name.equals("")) {
				this.message = "Campaign Name is Empty.";
				return this.ERRORJSON;
			}
			if(this.selected == null) {
				this.selected = false;
			}
			
			Campaign CA = AssessmentQueries.getCampaignByName(em, this.name);

			if (CA != null) {
				this.message = "Campaign Name Already Exists.";
				return this.ERRORJSON;
			}

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Campaign camp = new Campaign();
			camp.setName(this.name.trim());
			camp.setSelected(this.selected);
			em.persist(camp);
			AuditLog.audit(this, "Campaign " + this.name + " added", AuditLog.UserAction, false);
			HibHelper.getInstance().commit();

		} else if (action != null && action.equals("delCampaign") && this.id != null) {
			if (!this.testToken(false))
				return this.ERRORJSON;

			String mongo = "db.Assessment.count({ 'campaign_id' : " + this.getId() + "})";
			Long count = (Long) em.createNativeQuery(mongo).getSingleResult();
			if (count > 0l) {
				this.message = "Can't Delete since the value has been assigned to an assessment";
				return this.ERRORJSON;
			}
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Campaign camp = em.find(Campaign.class, this.id);
			em.remove(camp);
			AuditLog.audit(this, "Campaign " + camp.getName() + " deleted", AuditLog.UserAction, false);
			HibHelper.getInstance().commit();

		} else if (action != null && action.equals("emailSettings") && this.isAcadmin()) {
			if (!this.testToken(false))
				return this.ERRORJSON;

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
					.findFirst().orElse(new SystemSettings());
			ems.setServer(this.server);
			ems.setPort(this.port);
			ems.setUname(this.username);
			ems.setFromAddress(this.fromaddress);
			if (!this.password.equals("*****"))
				ems.setPassword(FSUtils.encryptPassword(this.password));
			ems.setType(this.type);
			ems.setPrefix(this.prefix);
			ems.setSignature(this.signature);
			ems.setEmailAuth(this.authischecked);
			ems.setTls(this.tlsischecked);
			ems.setEmailSSL(this.sslischecked);
			em.persist(ems);
			AuditLog.audit(this, "Email Settings Updated.", AuditLog.UserAction, false);
			HibHelper.getInstance().commit();
		} else if (action != null && action.equals("systemSettings") && this.isAcadmin()) {
			if (!this.testToken(false))
				return this.ERRORJSON;

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
					.findFirst().orElse(null);
			if (ems == null) {
				ems = new SystemSettings();
			}
			ems.setWebport(this.webport);
			ems.setTmp(this.tmp);
			ems.setWkhtmltopdf(this.pdf);
			em.persist(ems);
			AuditLog.audit(this, "System Settings Updated.", AuditLog.UserAction, false);
			HibHelper.getInstance().commit();
		} else if (action != null && action.equals("test")) {
			if (!this.testToken(false))
				return this.ERRORJSON;
			try {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
						.findFirst().orElse(null);
				ems.setServer(this.server);
				ems.setPort(this.port);
				ems.setUname(this.username);
				ems.setFromAddress(this.fromaddress);
				if (!this.password.equals("*****"))
					ems.setPassword(FSUtils.encryptPassword(this.password));
				ems.setType(this.type);
				ems.setPrefix(this.prefix);
				ems.setSignature(this.signature);
				ems.setEmailAuth(this.authischecked);
				ems.setTls(this.tlsischecked);
				ems.setEmailSSL(this.sslischecked);
				em.persist(ems);
				AuditLog.audit(this, "Email Settings Updated and Email Sent to " + this.to, AuditLog.UserAction, false);
				HibHelper.getInstance().commit();
				SendEmail sendEmail = new SendEmail(em);
				sendEmail.send(this.to, "This is a test email", "Test Email from Web Client");

				return SUCCESSJSON;
			} catch (Exception ex) {
				ex.printStackTrace();
				return ERRORJSON;
			}

		} else if (action != null && action.equals("updateRisk")) {

		}
		// session.close();
		return SUCCESS;
	}

	@Action(value = "CreateCF")
	public String CreateCF() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		CustomType foundType = (CustomType) em.createQuery("from CustomType where variable = :variable").setParameter("variable", this.cfvar)
				.getResultList().stream().findFirst().orElse(null);
		if (foundType != null && foundType.getDeleted() != null && foundType.getDeleted()) {
			message = "This variable has already been used by a deleted field";
			return this.ERRORJSON;
		}else if (foundType != null && (foundType.getDeleted() == null || !foundType.getDeleted())) {
			message = "This variable has already been used by an active field";
			return this.ERRORJSON;
		}

		CustomType type = new CustomType(this.cfname, this.cfvar.replaceAll(" ", ""), this.cftype);
		type.setDefaultValue(this.cfdefault);
		type.setFieldType(this.cffieldtype);
		type.setReadonly(this.readonly);
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(type);
		HibHelper.getInstance().commit();

		return this.SUCCESSJSON;
	}

	private Long cfid;

	@Action(value = "UpdateCF")
	public String UpdateCF() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		CustomType foundType = (CustomType) em.createQuery("from CustomType where variable = :variable").setParameter("variable", this.cfvar)
				.getResultList().stream().findFirst().orElse("null");
		if (foundType != null && foundType.getDeleted()) {
			message = "This variable has already been used by a deleted field";
			return this.ERRORJSON;
		}else if (foundType != null && !foundType.getDeleted() && !foundType.getId().equals(this.cfid)) {
			message = "This variable has already been used by an active field";
			return this.ERRORJSON;
		}
		
		CustomType type = (CustomType) em.createQuery("from CustomType where id = :id").setParameter("id", this.cfid)
				.getResultList().stream().findFirst().orElse(null);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		type.setKey(this.cfname);
		type.setVariable(this.cfvar.replaceAll(" ", ""));
		type.setReadonly(this.readonly);
		type.setDefaultValue(cfdefault);
		em.persist(type);
		HibHelper.getInstance().commit();

		return this.SUCCESSJSON;
	}

	@Action(value = "DeleteCF")
	public String DeleteCF() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		CustomType type = (CustomType) em.createQuery("from CustomType where id = :id").setParameter("id", this.cfid)
				.getResultList().stream().findFirst().orElse(null);
		String query = "{ 'type_id' : " + type.getId() + "}";
		List<CustomField> fields = em.createNativeQuery(query, CustomField.class).getResultList();
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		
		if (fields != null && fields.size() != 0) {
			//perform a soft delete since its used by other assessment or vulns
			type.setDeleted(true); 
		}else {
			em.remove(type);
		}
		HibHelper.getInstance().commit();

		return this.SUCCESSJSON;
	}

	@Action(value = "updatePrConfig")
	public String updatePrConfig() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		if (this.getTier().equals("consultant")) {
			this._message = "Upgrade to enable this feature";
			return this.ERRORJSON;
		}

		EMS = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
		if (EMS == null) {
			EMS = new SystemSettings();
		}
		if(this.prChecked != null) {
			EMS.setPeerreview(Boolean.parseBoolean(this.prChecked));
		}else if(this.selfPeerReview != null) {
			EMS.setSelfPeerReview(Boolean.parseBoolean(this.selfPeerReview));
		}
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(EMS);
		HibHelper.getInstance().commit();

		ServletActionContext.getRequest().getSession().setAttribute("struts.tokens.token",
				UUID.randomUUID().toString());
		return this.SUCCESSJSON;

	}

	@Action(value = "updateRandConfig")
	public String updateRandConfig() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken())
			return this.ERRORJSON;

		EMS = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
		if (EMS == null) {
			EMS = new SystemSettings();
		}
		EMS.setEnableRandAppId(Boolean.parseBoolean(this.randChecked));
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(EMS);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;

	}

	@Action(value = "updateFeedConfig")
	public String updateFeedConfig() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		EMS = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
		if (EMS == null) {
			EMS = new SystemSettings();
		}
		EMS.setEnablefeed(Boolean.parseBoolean(this.feedChecked));
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(EMS);
		HibHelper.getInstance().commit();

		return this.SUCCESSJSON;

	}

	@Action(value = "updateTitles")
	public String updateTiltes() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		EMS = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
		if (EMS == null) {
			EMS = new SystemSettings();
		}
		EMS.setBoldTitle(this.title[0]);
		EMS.setOtherTitle(this.title[1]);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(EMS);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;

	}

	@Action(value = "editCamp")
	public String editCamp() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		Campaign c = AssessmentQueries.getCampaignById(em, this.getId());

		if (c == null)
			return this.SUCCESSJSON;

		Campaign c2 = AssessmentQueries.getCampaignByName(em, this.getName());

		if (c2 != null && c2.getId().longValue() != c.getId().longValue()) {
			this._message = " Cannot have the same name.";
			return this.ERRORJSON;
		}

		c.setName(this.getName().trim());
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(c);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;

	}
	@Action(value = "editSelectedCampaign")
	public String editSelectedCampaign() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		
		List<Campaign> camps = em.createQuery("from Campaign").getResultList();
		if (camps == null)
			return this.SUCCESSJSON;
		
		for(Campaign c : camps) {
			if(c.getId().equals(this.getId())) {
				c.setSelected(this.selected);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(c);
				HibHelper.getInstance().commit();
			}else {
				c.setSelected(false);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(c);
				HibHelper.getInstance().commit();
			}
		}
		return this.SUCCESSJSON;

	}

	@Action(value = "editType")
	public String editType() {
		if (!(this.isAcadmin() || this.isAcmanager() || this.isAcengagement())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;

		AssessmentType t = AssessmentQueries.getAssessmentTypeById(em, this.getId());

		if (t == null) {
			this._message = "Assessment Type does not exist";
			return this.ERRORJSON;
		}

		AssessmentType t2 = AssessmentQueries.getAssessmentTypeByName(em, this.getName());

		if (t2 != null && t2.getId().longValue() != t.getId().longValue()) {
			this._message = "Cannot have the same name";
			return this.ERRORJSON;
		}

		t.setType(this.getName().trim());
		switch(this.riskType) {
			case 1: t.setRatingSystem("CVSS 3.1"); break;
			case 2: t.setRatingSystem("CVSS 4.0"); break;
			default: t.setRatingSystem("Native");
		}
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(t);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}

	@Action("createStatus")
	public String createStatus() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;
		if (status == null || status.trim().equals("")) {
			this._message = "Status is Empty";
			return this.ERRORJSON;
		}
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ems == null) {
			this._message = "No System Settings to update";
			return this.SUCCESSJSON;
		}

		if (ems.getStatus().stream().anyMatch(s -> s.toLowerCase().equals(this.status.toLowerCase().trim()))) {
			this._message = "Status Exists";
			return this.ERRORJSON;
		} else {
			List<String> stats = ems.getStatus();
			stats.add(this.status);
			stats.sort(String.CASE_INSENSITIVE_ORDER);
			ems.setStatus(stats);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(ems);
			AuditLog.audit(this, "User Added Status [" + this.status + "]", AuditLog.UserAction, null, null, false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		}

	}

	@Action("deleteStatus")
	public String deleteStatus() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;
		if (status == null || status.trim().equals("")) {
			this._message = "Status is Empty";
			return this.ERRORJSON;
		}
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ems == null) {
			this._message = "No System Settings to update";
			return this.SUCCESSJSON;
		}

		if (ems.getStatus().stream().anyMatch(s -> s.equals(this.status.trim()))) {

			List<String> stats = ems.getStatus();
			stats.remove(this.status.trim());
			stats.sort(String.CASE_INSENSITIVE_ORDER);
			ems.setStatus(stats);
			if (ems.getDefaultStatus().equals(this.status.trim()))
				ems.setDefaultStatus("");

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(ems);
			AuditLog.audit(this, "User Deleted Status [" + this.status + "]", AuditLog.UserAction, null, null, false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		} else {
			this._message = "Status Does not Exist";
			return this.ERRORJSON;
		}

	}

	@Action("setDefaultStatus")
	public String setDefaultStatus() {
		if (!(this.isAcadmin())) {
			return LOGIN;
		}
		if (!this.testToken(false))
			return this.ERRORJSON;
		if (status == null || status.trim().equals("")) {
			this._message = "Status is Empty";
			return this.ERRORJSON;
		}
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ems == null) {
			this._message = "No System Settings to update";
			return this.ERRORJSON;
		}

		if (ems.getStatus().stream().anyMatch(s -> s.toLowerCase().equals(this.status.toLowerCase().trim()))) {

			ems.setDefaultStatus(this.status.trim());
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(ems);
			AuditLog.audit(this, "User Added Status [" + this.status + "]", AuditLog.UserAction, null, null, false);
			HibHelper.getInstance().commit();
			return this.SUCCESSJSON;
		} else {
			this._message = "This Status Does not Exist";
			return this.ERRORJSON;
		}

	}

	public String getActiveOptions() {
		return "active";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

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

	public List<AssessmentType> getTypes() {
		return types;
	}

	public void setTypes(List<AssessmentType> types) {
		this.types = types;
	}

	public List<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFromaddress() {
		return fromaddress;
	}

	public void setFromaddress(String fromaddress) {
		this.fromaddress = fromaddress;
	}

	public SystemSettings getEMS() {
		return EMS;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public void setWebport(String webport) {
		this.webport = webport;
	}

	public void setTmp(String tmp) {
		this.tmp = tmp;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}

	public String getAuthChecked() {
		return authChecked;
	}

	public void setAuthChecked(String authChecked) {
		this.authChecked = authChecked;
	}

	public String getTlsChecked() {
		return tlsChecked;
	}

	public void setTlsChecked(String tlsChecked) {
		this.tlsChecked = tlsChecked;
	}

	public String getSslChecked() {
		return sslChecked;
	}

	public void setSslChecked(String sslChecked) {
		this.sslChecked = sslChecked;
	}

	public boolean getAuthischecked() {
		return authischecked;
	}

	public void setAuthischecked(boolean authischecked) {
		this.authischecked = authischecked;
	}

	public boolean getTlsischecked() {
		return tlsischecked;
	}

	public void setTlsischecked(boolean tlsischecked) {
		this.tlsischecked = tlsischecked;
	}

	public boolean getSslischecked() {
		return sslischecked;
	}

	public void setSslischecked(boolean sslischecked) {
		this.sslischecked = sslischecked;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setRiskName(String riskName) {
		this.riskName = riskName;
	}

	public void setRiskId(Long riskId) {
		this.riskId = riskId;
	}

	public List<CustomType> getCustom() {
		return custom;
	}

	public void setCfname(String cfname) {
		this.cfname = cfname;
	}

	public void setCfvar(String cfvar) {
		this.cfvar = cfvar;
	}

	public void setCftype(Integer cftype) {
		this.cftype = cftype;
	}

	public Long getCfid() {
		return cfid;
	}

	public void setCfid(Long cfid) {
		this.cfid = cfid;
	}

	public String isPrChecked() {
		return prChecked;
	}

	public void setPrChecked(String prChecked) {
		this.prChecked = prChecked;
	}

	public void setFeedChecked(String feedChecked) {
		this.feedChecked = feedChecked;
	}

	public String getFeedChecked() {
		return feedChecked;
	}

	public String[] getTitle() {
		return title;
	}

	public void setTitle(String[] title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRandChecked() {
		return randChecked;
	}

	public void setRandChecked(String randChecked) {
		this.randChecked = randChecked;
	}

	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}


	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCffieldtype(Integer cffieldtype) {
		this.cffieldtype = cffieldtype;
	}

	public void setCfdefault(String cfdefault) {
		this.cfdefault = cfdefault;
	}
	
	public void setSelfPeerReview(String selfPeerReview) {
		this.selfPeerReview = selfPeerReview;
	}
	public String getSelfPeerReview() {
		return this.selfPeerReview;
	}
	
	public void setRiskType(Integer riskType) {
		this.riskType = riskType;
	}
	
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	

}
