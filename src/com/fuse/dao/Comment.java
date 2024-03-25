package com.fuse.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.logging.log4j.util.PropertySource.Comparator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fuse.dao.AssessmentType;

/*
 * This table contains all PR comments and archived for history
 */
@Entity
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "commentGen")
	@TableGenerator(name = "commentGen", table = "commentGenseq", pkColumnValue = "comment", valueColumnName = "nextcomment", initialValue = 0, allocationSize = 1)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@NotFound(action = NotFoundAction.IGNORE)
	private User commenter;
	@OneToMany(fetch = FetchType.EAGER)
	@NotFound(action = NotFoundAction.IGNORE)
	private List<User> commenters;
	private Date dateOfComment;
	private String comment;
	private String summary1;
	private String summary2;
	private String summary1_notes;
	private String summary2_notes;
	@ManyToOne
	private AssessmentType type;
	private Boolean acceptedEdits=false;
	@ElementCollection
	private List<String> vulnerabilities;

	@Transient
	public List<User> getCommenter() {
		return this.getCommenters();
	}

	@Transient
	public void setCommenter(User commenter) {
		List<User> users = this.getCommenters();
		if (!users.contains(commenter)) {
			users.add(commenter);
			this.setCommeters(users);
		}
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

	public List<String> getVulnerabilities() {
		return vulnerabilities;
	}

	public void setVulnerabilities(List<String> vulnerabilities) {
		this.vulnerabilities = vulnerabilities;
	}

	public String getSummary1() {
		return summary1;
	}

	public String getSummary2() {
		return summary2;
	}

	public String getSummary1_notes() {
		return summary1_notes;
	}

	public String getSummary2_notes() {
		return summary2_notes;
	}

	public void setSummary1(String summary1) {
		this.summary1 = summary1;
	}

	public void setSummary2(String summary2) {
		this.summary2 = summary2;
	}

	public void setSummary1_notes(String summary1_notes) {
		this.summary1_notes = summary1_notes;
	}

	public void setSummary2_notes(String summary2_notes) {
		this.summary2_notes = summary2_notes;
	}

	public List<User> getCommenters() {
		if (commenters == null) {
			return new ArrayList<User>();
		} else {
			return commenters;
		}
	}

	public void setCommeters(List<User> commenters) {
		this.commenters = (List<User>) commenters;
	}
	

	public Boolean getAcceptedEdits() {
		return acceptedEdits;
	}

	public void setAcceptedEdits(Boolean acceptedEdits) {
		this.acceptedEdits = acceptedEdits;
	}
	
	public void setType(AssessmentType type) {
		this.type = type;
	}
	public AssessmentType getType() {
		return this.type;
	}

	@Transient
	private void addVuln(Vulnerability v, boolean blankNotes) {
		JSONObject json = new JSONObject();
		json.put("id", (Long) v.getId());
		json.put("catName",""+ v.getCategory().getName());
		json.put("catId",(Long) v.getCategory().getId());
		json.put("name", v.getName());
		json.put("desc", v.getDescription());
		json.put("rec", v.getRecommendation());
		json.put("details", v.getDetails());
		json.put("cvss_score", v.getCvssScore());
		json.put("cvss_string", v.getCvssString());
		if (blankNotes) {
			json.put("rec_notes", "<p></p>");
			json.put("desc_notes", "<p></p>");
			json.put("detail_notes", "<p></p>");
		} else {
			json.put("rec_notes", v.getRec_notes());
			json.put("desc_notes", v.getDesc_notes());
			json.put("detail_notes", v.getDetail_notes());
		}
		json.put("dv", v.getDefaultVuln() == null? null : v.getDefaultVuln().getId());
		json.put("overall", v.getOverall());
		json.put("likelihood", v.getLikelyhood());
		json.put("impact", v.getImpact());

		if (this.vulnerabilities == null) {
			this.vulnerabilities = new ArrayList();
		}
		this.vulnerabilities.add(json.toJSONString());

	}


	@Transient
	public void addVulns(List<Vulnerability> vulns, boolean blankNotes) {
		for (Vulnerability v : vulns) {
			this.addVuln(v, blankNotes);
		}
	}

	@Transient
	public void deleteAllVulns() {
		this.vulnerabilities = new ArrayList();
	}

	@Transient
	public void copyAssessment(Assessment a, boolean blankNotes) {
		this.summary1 = (a.getSummary() == null ? "<p></p>" : a.getSummary());
		this.summary2 = (a.getRiskAnalysis() == null ? "<p></p>" : a.getRiskAnalysis());
		this.summary1_notes = ("<p></p>");
		this.summary2_notes = ("<p></p>");
		this.type=a.getType();
		this.addVulns(a.getVulns(), blankNotes);
	}

	@Transient
	public void copyComment(Comment c) {
		this.summary1 = c.getSummary1();
		this.summary1_notes = c.getSummary1_notes();

		this.summary2 = c.getSummary2();
		this.summary2_notes = c.getSummary2_notes();
		
		List<String>vulnsClone = new ArrayList<>();
		for(String v : c.getVulnerabilities()) {
			vulnsClone.add(v);
		}
		this.vulnerabilities = vulnsClone;
		
	}

	@Transient
	public Assessment exportAssessment(EntityManager em) throws ParseException {
		Assessment a = new Assessment();
		a.setSummary(this.summary1);
		a.setRiskAnalysis(this.summary2);
		a.setPr_sum_notes(this.summary1_notes);
		a.setPr_risk_notes(this.summary2_notes);
		a.setType(this.type);
		JSONParser parse = new JSONParser();
		List<Vulnerability> vulns = new ArrayList<>();
		for (String json : this.vulnerabilities) {
			Vulnerability v = new Vulnerability();
			JSONObject vuln = (JSONObject) parse.parse(json);
			v.setId((Long) vuln.get("id"));
			v.setName("" + (vuln.get("name") == null ? "" : vuln.get("name")));
			DefaultVulnerability dv = em.find(DefaultVulnerability.class, (Long) vuln.get("dv"));
			if( vuln.get("catId") != null) {
				v.setCategory(em.find(Category.class, (Long) vuln.get("catId")));
			}else {
				v.setCategory(dv.getCategory());
			}
			v.setDescription("" + (vuln.get("desc") == null ? "" : vuln.get("desc")));
			v.setRecommendation("" + (vuln.get("rec") == null ? "" : vuln.get("rec")));
			v.setDefaultVuln(dv);
			v.setImpact((Long) vuln.get("impact"));
			v.setOverall((Long) vuln.get("overall"));
			v.setDetails(""+vuln.get("details"));
			v.setDetail_notes(""+ vuln.get("detail_notes"));
			v.setLikelyhood((Long) vuln.get("likelihood"));
			v.setDesc_notes("" + (vuln.get("desc_notes") == null ? "" : vuln.get("desc_notes")));
			v.setRec_notes("" + (vuln.get("rec_notes") == null ? "" : vuln.get("rec_notes")));
			v.setCvssString("" + (vuln.get("cvss_string") == null ? "" : vuln.get("cvss_string")));
			v.setCvssScore("" + (vuln.get("cvss_score") == null ? "" : vuln.get("cvss_score")));
			vulns.add(v);
		}
		a.setVulns(vulns);
		return a;
	}

}
