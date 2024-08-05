package com.fuse.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Campaign;
import com.fuse.dao.CustomField;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;

@Namespace("/services")
public class AjaxServices extends FSActionSupport {

	private String appid;
	private String appname;
	private String campname;
	private String username;

	@Action(value = "getAssessments",
			results = {@Result(
				name="_json",type = "stream"
				, params = {
						"contentType", "application/json;charset=UTF-8", 
				        "inputName", "_stream"})})
	public String getAssessments() {
		User user = this.getSessionUser();
		if (user == null) {

			return LOGIN;
		}
		if (user.getPermissions().isAssessor() || user.getPermissions().isManager()) {

			try {

				String query = "db.Assessment.find({ \"$query\" : {\"assessor\" : " + user.getId()
						+ ", \"completed\" : {\"$exists\": false}} , \"$orderby\": { \"start\" : 1 }})";

				SystemSettings settings = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst().orElse(null);
				
				List<Assessment> assessments = (List<Assessment>) em.createNativeQuery(query, Assessment.class)
						.setMaxResults(50).getResultList();
				List<RiskLevel> levels = (List<RiskLevel>) em.createQuery("from RiskLevel order by riskId desc")
						.getResultList();
				List<PeerReview> prs = (List<PeerReview>) em.createQuery("from PeerReview where completed = :date")
						.setParameter("date", new Date(0)).getResultList();

				// remove PR's of which i am a user unless settings allows it
				if(!(settings != null && settings.getSelfPeerReview() != null && settings.getSelfPeerReview())) {
				prs = prs.stream().filter(
						pr -> !pr.getAssessment().getAssessor().stream().anyMatch(u -> u.getId() == user.getId()))
						.collect(Collectors.toList());
				}
				// remove PRs from different teams
				prs = prs.stream()
						.filter(pr -> pr.getAssessment().getAssessor().stream()
								.anyMatch(u -> u.getTeam().getId().longValue() == user.getTeam().getId().longValue()))
						.collect(Collectors.toList());

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String json = "{ 'count' : " + assessments.size() + ",\n";
				json += "'prcount' : " + prs.size() + ",\n";
				json += "'assessments' : [";
				boolean isFirst = true;
				for (Assessment a : assessments) {
					if (!isFirst) {
						json += ",";
					}
					HashMap<Integer, Integer> counts = new HashMap();
					for (int i = 0; i < 10; i++) {
						counts.put(i, 0);
					}
					for (Vulnerability v : a.getVulns()) {
						if (v.getOverall() == null || v.getOverall() == -1l)
							continue;
						else {
							counts.put(v.getOverall().intValue(), counts.get(v.getOverall().intValue()) + 1);
						}
					}

					String[] colors = new String[] { "#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a",
							"#39cccc", "#00c0ef", "#f39c12", "#dd4b39" };
					String html = "";
					int i = 9;
					for (RiskLevel level : levels) {
						String name = level.getRisk();
						if (name != null && !name.equals("") && !name.toLowerCase().equals("unassigned")) {
							html += "<span style=\\\"font-size:small; color:" + colors[i] + "; border: solid 1px "
									+ colors[i--] + "\\\" class=\\\"circle\\\" " + "title=\\\"" + name
									+ "\\\"><b>&nbsp;" + counts.get(level.getRiskId()) + "&nbsp;</b></span>&nbsp;";
						}
					}

					json += "[ '" + a.getName() + "'," + "'" + a.getAppId() + "'," + "'" + format.format(a.getStart())
							+ "'," + "'" + html + "', " + "'app" + a.getId() + "']\n";
					isFirst = false;

				}
				json += "]}";
				json = json.replaceAll("\'", "\"");
				return this.jsonOutput(json);

			} catch (Exception Ex) {
				Ex.printStackTrace();

			}
		} else {

			AuditLog.notAuthorized(this, "User attempted to get the assessment queue", true);
		}
		return this.jsonOutput("{ \"count\" : 0}");
	}

	@Action(value = "getVerifications",
			results = {@Result(
				name="_json",type = "stream"
				, params = {
						"contentType", "application/json;charset=UTF-8", 
				        "inputName", "_stream"})})
	public String getVerifications() {
		User user = this.getSessionUser();
		if (user == null)
			return LOGIN;
		if (user.getPermissions().isAssessor() || user.getPermissions().isManager()) {

			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			try {

				List<Verification> verifications = verifications = (List<Verification>) em
						.createQuery("from Verification v where v.assessor = :id and v.workflowStatus = :wf1 ")
						.setParameter("id", user).setParameter("wf1", Verification.InAssessorQueue).getResultList();

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

				String json = "{ 'count' : " + verifications.size() + ",\n";
				json += "'verifications' : [";
				boolean isFirst = true;
				for (Verification v : verifications) {
					if (!isFirst) {
						json += ",";
					}
					v.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					json += "[ '" + v.getAssessment().getName() + "'," + "'" + v.getAssessment().getAppId() + "'," + "'"
							+ format.format(v.getStart()) + "'," + "'" + v.getId() + "'," + "'"
							+ v.getVerificationItems().get(0).getVulnerability().getName() + "'," + "'"
							+ v.getVerificationItems().get(0).getVulnerability().getOverallStr() + "']\n";
					isFirst = false;

				}
				json += "]}";
				json = json.replaceAll("\'", "\"");
				return this.jsonOutput(json);
			} catch (Exception Ex) {
				return this.jsonOutput("{ \"count\" : 0}");

			}

		} else {
			AuditLog.notAuthorized(this, "User attempted to get the verification queue", true);
		}
		return this.jsonOutput("{ \"count\" : 0}");
	}

	@Action(value = "metricSearch")
	public String metricSearch() {
		User user = this.getSessionUser();
		if (user == null)
			return LOGIN;
		if (user.getPermissions().isAssessor() || user.getPermissions().isManager()) {

			List<Assessment> as = null;
			if (!this.isNullStirng(this.appid) && !this.isNullStirng(appname)) {
				String query = "{ '$query' : {$or : [{'appId' : { $regex : '.*" + FSUtils.sanitizeMongo(appid)
						+ ".*', $options : 'i'}}, { 'name' : { $regex : '.*" + FSUtils.sanitizeMongo(appname)
						+ ".*', $options : 'i'}}]}, '$orderby' : {'appId':1}}";
				// String query = "{ 'name' : { $regex : '.*"+FSUtils.sanitizeMongo(appname) +
				// ".*', $options : 'i'}, "
				// + "$where: '/^"+FSUtils.sanitizeMongo(appid)+".*/.test(this.appId)'}";
				as = FSUtils.sortUniqueAssessment(em.createNativeQuery(query, Assessment.class)
						.getResultList());

			} else if (!this.isNullStirng(appid)) {
				// String query = "{ $where:
				// '/^"+FSUtils.sanitizeMongo(appid)+".*/.test(this.appId)'}";
				String query = "{ '$query' : { 'appId' : { $regex : '.*" + FSUtils.sanitizeMongo(appid)
						+ ".*', $options : 'i'}}, '$orderby' : {'appId':1}}";
				as = FSUtils.sortUniqueAssessment(em.createNativeQuery(query, Assessment.class).getResultList());

			} else if (!this.isNullStirng(appname)) {
				String query = "{ '$query' : { 'name' : { $regex : '.*" + FSUtils.sanitizeMongo(appname)
						+ ".*', $options : 'i'}}, '$orderby' : {'appId':-1}}";
				as = FSUtils.sortUniqueAssessment(em.createNativeQuery(query, Assessment.class).getResultList());

			} else if (!this.isNullStirng(campname)) {
				String query = "{ '$query' : { 'name' : { $regex : '.*" + FSUtils.sanitizeMongo(campname)
						+ ".*', $options : 'i'}}}, '$orderby' : {'name':1}}";
				List<Campaign> camp = em.createNativeQuery(query, Campaign.class).getResultList();
				JSONArray array = new JSONArray();
				for (Campaign c : camp) {
					JSONObject json = new JSONObject();
					json.put("appid", "");
					json.put("appname", c.getName());
					json.put("campid", c.getId());
					json.put("campName", c.getName());

					array.add(json);

				}
				return this.jsonOutput(array.toJSONString());
			} else if (!this.isNullStirng(username)) {
				String query = "{ '$query' : {$or : [{'fname' : { $regex : '.*" + FSUtils.sanitizeMongo(username)
						+ ".*', $options : 'i'}}, { 'lname' : { $regex : '.*" + FSUtils.sanitizeMongo(username)
						+ ".*', $options : 'i'}}]}, '$orderby' : {'fname':1}}";
				List<User> users = em.createNativeQuery(query, User.class).getResultList();
				JSONArray array = new JSONArray();
				for (User u : users) {
					if (u.getPermissions().isAssessor()) {
						JSONObject json = new JSONObject();
						json.put("id", u.getId());
						json.put("name", u.getFname() + " " + u.getLname());
						array.add(json);
					}
				}
				return this.jsonOutput(array.toJSONString());
			}

			JSONArray array = new JSONArray();
			if(as != null) {
				for (Assessment a : as) {
					JSONObject json = new JSONObject();
					json.put("appid", a.getAppId());
					json.put("appname", a.getName());
					json.put("type", a.getType().getId());
					json.put("distro", a.getDistributionList());
					json.put("remediationId", a.getRemediation().getId());
					json.put("engId", a.getEngagement().getId());
					json.put("remediationName", a.getRemediation().getFname() + " " + a.getRemediation().getLname());
					json.put("campName", a.getCampaign().getName());
					json.put("cid", a.getCampaign().getId());
					JSONArray fields = new JSONArray();
					if (a.getCustomFields() != null) {
						for (CustomField cf : a.getCustomFields()) {
							JSONObject field = new JSONObject();
							field.put("fid", cf.getType().getId());
							field.put("value", cf.getValue());
							fields.add(field);
						}
						json.put("fields", fields);
					}
					array.add(json);
				}
			}

			return this.jsonOutput(array.toJSONString());
		} else {
			AuditLog.notAuthorized(this, "User attempted to search metrics", true);
			this._message = "Not Authorized";
			return this.ERRORJSON;
		}
	}

	public String getAppid() {
		return appid;
	}

	public String getAppname() {
		return appname;
	}

	public String getCampname() {
		return campname;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public void setCampname(String campname) {
		this.campname = campname;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
