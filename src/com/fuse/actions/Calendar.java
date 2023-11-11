package com.fuse.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Query;
import org.hibernate.Session;

import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.OOO;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Verification;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/calendar/Calendar.jsp")
public class Calendar extends FSActionSupport {

	private List<AssessmentType> atypes;
	private List<Teams> teams;
	private List<User> users;
	private Long userid;
	private Long type;
	private Long team;
	private Date start;
	private Date end;
	private String action = "";
	private List<Assessment> assessments;
	private List<OOO> ooo;
	private List<Verification> verifications;
	private String title;
	private Long oid;

	@Action(value = "Calendar", results = {
			@Result(name = "searchJSON", location = "/WEB-INF/jsp/calendar/calendarSearchJSON.jsp") })
	public String execute() {
		if (!(this.isAll()))
			return LOGIN;
		User user = this.getSessionUser();
		// Session session = HibHelper.getSessionFactory().openSession();
		if (this.action.equals("")) {
			users = new ArrayList<User>();
			users.add(new User(-1l, "Please", "Select"));
			atypes = new ArrayList<AssessmentType>();
			atypes.add(new AssessmentType(-1l, "Please Select"));
			teams = new ArrayList<Teams>();
			teams.add(new Teams(-1l, "Please Select"));
			users.addAll((List<User>) em.createQuery("from User").getResultList());
			atypes.addAll((List<AssessmentType>) em.createQuery("from AssessmentType").getResultList());
			teams.addAll((List<Teams>) em.createQuery("from Teams").getResultList());
		} else if (action.equals("search")) {
			List<Object> feeds = this.createQuery(em.unwrap(Session.class), start, end, userid, team);
			assessments = (List<Assessment>) feeds.get(0);
			verifications = (List<Verification>) feeds.get(1);
			ooo = (List<OOO>) feeds.get(2);
			// session.close();
			return "searchJSON";
		} else if (action.equals("add")) {
			if (!this.testToken())
				return this.ERRORJSON;
			if (this.isAcmanager()) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				User u = em.find(User.class, userid);
				OOO ooo = new OOO();
				ooo.setStart(start);
				ooo.setEnd(end);
				ooo.setTitle(title);
				ooo.setUser(u);
				em.persist(ooo);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			} else if (user.getId() == this.userid) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				User u = em.find(User.class, userid);
				OOO ooo = new OOO();
				ooo.setStart(start);
				ooo.setEnd(end);
				ooo.setTitle(title);
				ooo.setUser(u);
				em.persist(ooo);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			} else {
				User u = em.find(User.class, userid);

				if (u != null)
					AuditLog.notAuthorized(this,
							"User attempted to add a calendar event for " + u.getFname() + " " + u.getLname(), true);
				else
					AuditLog.notAuthorized(this, "User attempted to add a calendar event for null user", true);

				this._message = "Not Authorized";
				return this.ERRORJSON;
			}

		} else if (action.equals("delete")) {
			if (!this.testToken())
				return this.ERRORJSON;
			OOO o = (OOO) em.find(OOO.class, oid);
			if (this.isAcmanager()) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();

				em.remove(o);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;
			} else if (user.getId() == o.getUser().getId()) {

				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.remove(o);
				HibHelper.getInstance().commit();
				return this.SUCCESSJSON;

			} else {
				User u = em.find(User.class, userid);

				if (u != null)
					AuditLog.notAuthorized(this,
							"User attempted to delete a calendar event for " + u.getFname() + " " + u.getLname(), true);
				else
					AuditLog.notAuthorized(this, "User attempted to delete a calendar event for null user", true);
				this._message = "Not Authorized";
				return this.ERRORJSON;
			}
		} else {
			java.util.Calendar start = java.util.Calendar.getInstance();
			start.add(java.util.Calendar.MONTH, -3);
			assessments = em.createQuery("from Assessment where start > :start").setParameter("start", start.getTime())
					.getResultList();
			ooo = em.createQuery("from OOO where start > :start").setParameter("start", start.getTime())
					.getResultList();
			verifications = em.createQuery("from Verification where start > :start")
					.setParameter("start", start.getTime()).getResultList();
			// session.close();
			return "searchJSON";
		}
		// session.close();
		return SUCCESS;
	}

	public String getActiveCal() {
		return "active";
	}

	public List<Teams> getTeams() {
		return teams;
	}

	public List<User> getUsers() {
		return users;
	}

	public List<AssessmentType> getAtypes() {
		return atypes;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<Assessment> getAssessments() {
		return assessments;
	}

	public List<OOO> getOoo() {
		return ooo;
	}

	public List<Verification> getVerifications() {
		return verifications;
	}

	public void setType(Long type) {
		this.type = type;
	}

	public void setTeam(Long team) {
		this.team = team;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	private List<Object> createQuery(Session sn, Date s, Date e, Long u, Long tm) {

		List<Object> feeds = new ArrayList<Object>();
		List<Verification> vs = new ArrayList<Verification>();
		List<Assessment> as = new ArrayList<Assessment>();
		List<OOO> os = new ArrayList<OOO>();

		String aquery = "from Assessment as a ";
		String vquery = "from Verification as a ";
		String qooo = "from OOO as a ";
		if (s != null && e != null) {
			String sub = " where  (a.start >= :start and a.start <= :end) or (a.end >= :start and a.end <= :end)";
			aquery += sub;
			qooo += sub;
			vquery += sub;

		} else {
			String sub = " where  a.start >= :start ";
			aquery += sub;
			qooo += sub;
			vquery += sub;
		}

		Query q1 = sn.createQuery(aquery);
		Query q2 = sn.createQuery(vquery);
		Query q3 = sn.createQuery(qooo);
		if (s != null && e != null) {
			q1.setDate("start", s).setDate("end", e);
			q2.setDate("start", s).setDate("end", e);
			q3.setDate("start", s).setDate("end", e);
		} else {
			java.util.Calendar c = java.util.Calendar.getInstance();
			c.add(java.util.Calendar.MONTH, -3);
			q1.setDate("start", c.getTime());
			q2.setDate("start", c.getTime());
			q3.setDate("start", c.getTime());

		}

		as = q1.list();
		vs = q2.list();
		os = q3.list();
		if (u != null && u != -1) {
			as.removeIf(asmt -> !asmt.getAssessor().stream().anyMatch(user -> (user.getId() == u.longValue())));

			vs.removeIf(v -> v.getAssessor().getId() != u.longValue());
			os.removeIf(o -> o.getUser() != null && o.getUser().getId() != u.longValue());
		}

		if (tm != null && tm.longValue() != -1l) {

			vs.removeIf(v -> v.getAssessor().getTeam().getId().longValue() != tm.longValue());
			os.removeIf(o -> o.getUser() != null && o.getUser().getTeam().getId().longValue() != tm.longValue());
			as.removeIf(a -> !a.getAssessor().stream()
					.anyMatch(user -> user.getTeam().getId().longValue() == tm.longValue()));

		}

		feeds.add(0, as);
		feeds.add(1, vs);
		feeds.add(2, os);
		return feeds;

	}

}
