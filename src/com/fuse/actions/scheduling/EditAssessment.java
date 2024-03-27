package com.fuse.actions.scheduling;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Campaign;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;
import com.faction.extender.AssessmentManager;
import com.fuse.extenderapi.Extensions;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/engagement/EditAssessment.jsp")
public class EditAssessment extends FSActionSupport {

	private List<CustomType> custom;
	private List<CustomField> fields;
	private List<User> users;
	private List<Teams> teams;
	private String action;
	private Date sdate;
	private Date edate;
	private List<User> assessors = new ArrayList<User>();
	private String appid;
	private String appName;
	private List<Integer> assessorId;
	private Integer remId;
	private Integer engId;
	private List<AssessmentType> assessmentTypes;
	private Integer type;
	private String assessorName;
	private List<Assessment> assessments;
	private Integer aid;
	private Assessment currentAssessment;
	private String notes;
	private String distro;
	private Campaign campaign;
	private List<Campaign> campaigns;
	private Long campId;
	private List<Files> files;
	private String cf;
	private Boolean randId = true;
	private List<AuditLog> logs;
	private String updatedText = "";
	private String back;

	@Action(value = "EditAssessment")
	public String execute() throws ParseException {
		//TODO: Update this method into separate methods for each function
		User user = this.getSessionUser();
		if (!(this.isAcengagement() || this.isAcmanager() || this.isAcassessor())) {
			return LOGIN;
		}

		custom = em.createQuery("from CustomType where type = 0 and (deleted IS NULL or deleted = false)").getResultList();

		users = em.createQuery("from User").getResultList();
		teams = em.createQuery("from Teams").getResultList();
		assessmentTypes = em.createQuery("from AssessmentType").getResultList();
		assessors = em.createQuery("from User").getResultList();
		campaigns = em.createQuery("from Campaign").getResultList();
		List<User> tmp = new ArrayList(assessors);
		for (User u : tmp) {
			if (!u.getPermissions().isAssessor())
				assessors.remove(u);
		}
		SystemSettings ss = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ss.getEnableRandAppId() != null)
			this.randId = ss.getEnableRandAppId();
		else
			this.randId = true;

		if (action != null && action.equals("get")) {
			currentAssessment = em.find(Assessment.class, (long) this.aid);
			files = (List<Files>) em.createQuery("from Files where entityId = :id").setParameter("id", (long) this.aid)
					.getResultList();
			assessors = currentAssessment.getAssessor();

			logs = AssessmentQueries.getLogs(em, currentAssessment);

		} else if (action != null && action.equals("dateSearch")) {
			assessors = users;

			// session.close();
			return "assessorJSON";
		} else if (action != null && action.equals("update")) {

			if (!this.testToken(false)) {
				return this.ERRORJSON;
			}
			//Check that the user has permissions to edit the assessment
			/// They must be either a manager, an admin, or an assessor of
			/// the assessment
			if( user.getPermissions().isAssessor() 
					&&  !(user.getPermissions().isAdmin() || user.getPermissions().isManager())
					&&  !assessors.stream().anyMatch( u -> u.getId() == user.getId() )
			) {
				this._message = "Not Authorized to Update This assessment. You must be a manager, admin, or contributer to the assessment";
				return this.ERRORJSON;
			}

			List<User> assessors = new ArrayList<User>();
			if (assessorId != null) {
				for (Integer asid : assessorId) {

					User assessor = em.find(User.class, asid.longValue());
					if (assessor != null && assessor.getPermissions().isAssessor())
						assessors.add(assessor);
				}
			}

			User remediation = em.find(User.class, (long) remId);
			if (remediation != null && !remediation.getPermissions().isRemediation())
				remediation = null;

			User engagement = em.find(User.class, (long) engId);
			if (engagement != null && !engagement.getPermissions().isRemediation())
				engagement = null;
			AssessmentType Type = em.find(AssessmentType.class, (long) type);

			Campaign camp = null;
			if (campId != null && campId != -1) {
				camp = em.find(Campaign.class, campId);
			}
			Assessment am = em.find(Assessment.class, (long) this.aid);

			if (am == null) {
				this._message = "Assessment is not Valid";
				return this.ERRORJSON;
			}
			if (this.appid == null || this.appid.equals("")) {
				this._message = "Application Id is missing.";
				return this.ERRORJSON;
			}
			if (this.appName == null || this.appName.equals("")) {
				this._message = "Application Name is Missing";
				return this.ERRORJSON;
			}

			if (sdate == null || edate == null) {
				this._message = "Start and End Dates Could Be Missing";
				return this.ERRORJSON;
			}


			HibHelper.getInstance().preJoin();
			em.joinTransaction();

			try {
				if (am.isFinalized()) {
					AuditLog.audit(this,
							String.format("Assessment Updated after finalized. Was %s %s, now %s %s", am.getAppId(),
									am.getName(), this.appid, this.appName),
							AuditLog.UserAction, AuditLog.CompAssessment, am.getId(), false);
				}
				am.setAppId(this.appid);
				am.setName(this.appName);

				// If assessment is finalized this info is locked
				if (!am.isFinalized()) {
					am.setStart(this.sdate);
					am.setEnd(this.edate);
					am.setEngagement(engagement);
					am.setRemediation(remediation);
					am.setAssessor(assessors);
					am.setType(Type);
					am.setAccessNotes(this.notes);
					am.setDistributionList(this.distro);
					Map<String, Files> sessionfiles = null;
					if (camp != null)
						am.setCampaign(camp);
					try {
						// HttpSession sess = this.request.getSession();
						// sessionfiles = (Map<String, Files>) sess.getAttribute("Files");
						sessionfiles = (Map<String, Files>) ServletActionContext.getRequest().getSession()
								.getAttribute("Files");
						ServletActionContext.getRequest().getSession().setAttribute("Files", null);
						// sessionfiles = (Map<String, Files>) this.JSESSION.get("Files");
						// sess.setAttribute("Files",null);
						/// this.JSESSION.put("Files", null);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// session.getTransaction().begin();
					if (sessionfiles != null) {
						for (Files f : sessionfiles.values()) {
							f.setCreator(this.getSessionUser());
							f.setEntityId(am.getId());
							f.setType(Files.ASSESSMENT);
							// session.save(f);
							em.persist(f);
						}

					}
					JSONArray cfstuff = new JSONArray();
					if (this.cf != null) {
						JSONParser parse = new JSONParser();
						JSONArray array = (JSONArray) parse.parse(cf);

						// am.setCustomFields(new ArrayList());

						for (int i = 0; i < array.size(); i++) {
							if (am.getCustomFields() == null)
								am.setCustomFields(new ArrayList());

							JSONObject json = (JSONObject) array.get(i);
							CustomField cfObj = null;
							Long cfid = Long.parseLong("" + json.get("id"));

							for (CustomField obj : am.getCustomFields()) {
								if (obj.getType().getId().equals(cfid)) {
									cfObj = obj;
									cfObj.setValue("" + json.get("text"));
									break;
								}
							}
							if (cfObj == null) {
								cfObj = new CustomField();
								CustomType ct = em.find(CustomType.class, cfid);
								cfObj.setType(ct);
								cfObj.setValue("" + json.get("text"));
								am.getCustomFields().add(cfObj);
							}

						}

					}
				}
				em.persist(am);
				if (!am.isFinalized())
					AuditLog.audit(this, "Assessment Updated", AuditLog.UserAction, AuditLog.CompAssessment, am.getId(),
							false);

				// Run All extensions
				Extensions amgr = new Extensions(Extensions.EventType.ASMT_MANAGER);
				amgr.execute(am, AssessmentManager.Operation.Update);
			} finally {
				HibHelper.getInstance().commit();
			}
			return this.SUCCESSJSON;

		}
		return SUCCESS;
	}
	

	private String getChanges(Assessment a) {
		String changes = "";
		if (a.getAppId() != null && !a.getAppId().equals(this.appid)) {
			changes += String.format("App id changed from %s to %s.<br>", a.getAppId(), this.appid);
		}
		if (a.getName() != null && !a.getName().equals(this.appName)) {
			changes += String.format("App name changed from %s to %s.<br>", a.getName(), this.appName);
		}
		if (a.getAccessNotes() != null && !a.getAccessNotes().equals(this.notes)) {
			changes += "Access notes changed.<br>";
		}
		if (a.getEngagement() != null && a.getEngagement().getId() != (long) this.engId.intValue()) {
			changes += String.format("Enagement changed from %s to %s.<br>", a.getEngagement().getUsername(),
					(em.find(User.class, (long) this.engId.intValue()).getUsername()));
		}
		if (a.getRemediation() != null && a.getRemediation().getId() != (long) this.remId.intValue()) {
			changes += String.format("Remediation changed from %s to %s.<br>", a.getRemediation().getUsername(),
					(em.find(User.class, (long) this.remId.intValue()).getUsername()));
		}
		if (a.getStart() != null && !a.getStart().equals(this.sdate)) {
			changes += String.format("Start Date  changed from %s to %s.<br>", a.getStart(), this.sdate);
		}
		if (a.getEnd() != null && !a.getEnd().equals(this.edate)) {
			changes += String.format("End Date  changed from %s to %s.<br>", a.getEnd(), this.edate);
		}
		return changes;
	}

	private String sanitize(String input) {
		return input.replaceAll("[\"'{}]", "");
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getActiveEngagement() {
		return "active";
	}

	public List<CustomType> getCustom() {
		// TODO Fix this later
		return custom;
	}

	public List<User> getEngagement() {
		List<User> eng = new ArrayList<User>();
		for (User u : users) {
			if (u.getPermissions().isEngagement()) {
				eng.add(u);
			}
		}
		return eng;
	}

	public List<User> getRemediation() {
		List<User> rem = new ArrayList<User>();
		for (User u : users) {
			if (u.getPermissions().isRemediation()) {
				rem.add(u);
			}
		}
		return rem;
	}

	public List<Teams> getTeams() {
		return teams;
	}

	public void setTeams(List<Teams> teams) {
		this.teams = teams;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getSdate() {
		return sdate;
	}

	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}

	public Date getEdate() {
		return edate;
	}

	public void setEdate(Date edate) {
		this.edate = edate;
	}

	public List<User> getAssessors() {
		return assessors;
	}

	public void setAssessors(List<User> assessors) {
		this.assessors = assessors;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<Integer> getAssessorId() {
		return assessorId;
	}

	public void setAssessorId(List<Integer> assessorId) {
		this.assessorId = assessorId;
	}

	public int getRemId() {
		return remId;
	}

	public void setRemId(int remId) {
		this.remId = remId;
	}

	public int getEngId() {
		return engId;
	}

	public void setEngId(int engId) {
		this.engId = engId;
	}

	public List<AssessmentType> getAssessmentTypes() {
		return assessmentTypes;
	}

	public void setAssessmentTypes(List<AssessmentType> assessmentTypes) {
		this.assessmentTypes = assessmentTypes;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAssessorName() {
		return assessorName;
	}

	public void setAssessorName(String assessorName) {
		this.assessorName = assessorName;
	}

	/*
	 * public void setAppid(String appid) { this.appid = appid; }
	 */
	public List<Assessment> getAssessments() {
		return assessments;
	}

	public void setAssessments(List<Assessment> assessments) {
		this.assessments = assessments;
	}

	public Integer getAid() {
		return aid;
	}

	public void setAid(Integer aid) {
		this.aid = aid;
	}

	public Assessment getCurrentAssessment() {
		return currentAssessment;
	}

	public void setCurrentAssessment(Assessment currentAssessment) {
		this.currentAssessment = currentAssessment;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDistro() {
		return distro;
	}

	public void setDistro(String distro) {
		this.distro = distro;
	}

	/*
	 * public void setAssessorId(Integer assessorId) { this.assessorId = assessorId;
	 * }
	 */
	public void setRemId(Integer remId) {
		this.remId = remId;
	}

	public void setEngId(Integer engId) {
		this.engId = engId;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getStartStr() {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("MM/dd/yyyy");
		return formatter.format(currentAssessment.getStart());
	}

	public String getEndStr() {
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("MM/dd/yyyy");
		return formatter.format(currentAssessment.getEnd());

	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public List<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public Long getCampId() {
		return campId;
	}

	public void setCampId(Long campId) {
		this.campId = campId;
	}

	public List<Files> getFiles() {
		return files;
	}

	public String getCf() {
		return cf;
	}

	public void setCf(String cf) {
		this.cf = cf;
	}

	public Boolean getRandId() {
		return randId;
	}

	public List<AuditLog> getLogs() {
		return logs;
	}
	
	public String getBack() {
		return back;
	}
	
	public void setBack(String back) {
		this.back = back;
	}

	public void validate() {
		if (this.distro == null || this.distro.trim().equals(""))
			return;

		// String emailRegex =
		// "^['_a-z0-9-\\+]+(\\.['_a-z0-9-\\+]+)?@[a-z0-9-]+(\\.[a-z0-9-]+)?\\.[a-z]{2,6}$";
		String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		Pattern p = Pattern.compile(emailRegex);
		String[] emails = this.distro.split(";");
		for (String mail : emails) {
			Matcher m = p.matcher(mail.trim());
			if (m.matches())
				continue;
			else
				addActionError("Email Address is not Valid");
		}

	}

	public String getUpdatedText() {
		return this.updatedText;
	}

}
