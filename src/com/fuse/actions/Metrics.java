package com.fuse.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.Assessment;
import com.fuse.dao.Comment;
import com.fuse.dao.PeerReview;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.utils.UserRate;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/metrics/Metrics.jsp")
public class Metrics extends FSActionSupport {

	private int criticalN = 0;
	private int highN = 0;
	private int mediumN = 0;
	private int lowN = 0;
	private int pditems = 0;
	private int pritems = 0;
	private int pdvitems = 0;
	private Map<String, UserRate> topUsers = new HashMap<String, UserRate>();
	private TreeMap<Integer, String> topUsersSorted = new TreeMap<Integer, String>(Collections.reverseOrder());
	private LinkedHashMap<String, Integer> topVulns = new LinkedHashMap<String, Integer>();
	private Map<String, Integer> topReviews = new HashMap<String, Integer>();
	private Map<String, Integer> openReviews = new HashMap<String, Integer>();
	private Map<String, Integer> pastDueAsmt = new HashMap<String, Integer>();
	private Map<String, Integer> pastDueVulns = new HashMap<String, Integer>();
	private String action = "";
	private String appId;
	private String appName;
	private String dates = "";
	private String crits = "";
	private String highs = "";
	private String meds = "";
	private String lows = "";
	private Long campId;
	private Long id;
	private Calendar past7days = Calendar.getInstance();
	private List<Assessment> asmts = new ArrayList<Assessment>();
	private List<RiskLevel> levels = new ArrayList<>();
	private HashMap<Integer, String> data = new HashMap<>();
	private String[] defaultColors = new String[] { "#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a",
			"#39cccc", "#00c0ef", "#f39c12", "#dd4b39" };
	private List<String> colors = new ArrayList<>();


	@Action(value = "Metrics", results = {
			@Result(name = "lineChartDataJson", location = "/WEB-INF/jsp/metrics/lineChartDataJson.jsp"),
			@Result(name = "health", location = "/WEB-INF/jsp/metrics/Health.jsp") })
	public String execute() {

		if (!(this.isAll()))
			return LOGIN;
		User user = this.getSessionUser();
		past7days.set(Calendar.DATE, past7days.get(Calendar.DATE) - 7);
		levels = em.createQuery("from RiskLevel order by riskId desc").getResultList();
		// Session session = HibHelper.getSessionFactory().openSession();
		if (action.equals("getcamp")) {
			String query = "{ 'campaign_id' : " + campId + " }";
			asmts = AssessmentQueries.getAssessmentsByCampaign(em, user, campId, AssessmentQueries.OnlyCompleted);

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

			int c = 9;
			for (RiskLevel level : levels) {
				String risk = level.getRisk();
				if (risk == null || risk.equals("") || risk.toLowerCase().startsWith("unassigned"))
					continue;
				colors.add(defaultColors[c--]);
			}
			plotAssessment(asmts, levels, true);
			return "lineChartDataJson";
		} else if (action.equals("getapp")) {
			asmts = AssessmentQueries.getAssessmentsByAppDesc(em, user, appId, appName,
					AssessmentQueries.OnlyCompleted);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

			int c = 9;
			for (RiskLevel level : levels) {
				String risk = level.getRisk();
				if (risk == null || risk.equals("") || risk.toLowerCase().startsWith("unassigned"))
					continue;
				colors.add(defaultColors[c--]);
			}
			plotAssessment(asmts, levels, false);

			return "lineChartDataJson";

		} else if (action.equals("getuser")) {
			asmts = AssessmentQueries.getAssessmentsByUserId(em, user, this.id, AssessmentQueries.OnlyCompleted);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

			int c = 9;
			for (RiskLevel level : levels) {
				String risk = level.getRisk();
				if (risk == null || risk.equals("") || risk.toLowerCase().startsWith("unassigned"))
					continue;
				colors.add(defaultColors[c--]);
			}
			plotAssessment(asmts, levels, true);

			return "lineChartDataJson";

		} else if (action.equals("health")) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -12);
			List<Assessment> as = em.createQuery("from Assessment as a where a.start >= :start")
					.setParameter("start", cal.getTime()).getResultList();
			List<Verification> vs = em
					.createQuery("from Verification as v where v.start >= :start and workflowStatus != :status")
					.setParameter("start", cal.getTime()).setParameter("status", Verification.RemediationCompleted)
					.getResultList();
			List<PeerReview> prs = em.createQuery("from PeerReview").getResultList();
			List<Comment> coms = em.createQuery("from Comment").getResultList();
			this.getOpenVulns(as, em);
			this.addPRList(coms);
			this.getOpenPrs(prs);
			this.getOpenVerifications(vs);
			for (String name : this.topUsers.keySet()) {
				this.topUsersSorted.put(this.topUsers.get(name).findings, name);
			}
			// session.close();
			return "health";
		} else {
			return SUCCESS;
		}

	}

	private String rc(String data) { // Remove last Comma
		return data.substring(0, data.length() - 2);
	}

	private void getOpenVerifications(List<Verification> vs) {
		Date now = new Date();
		for (Verification v : vs) {
			if (v.getEnd().getTime() < now.getTime())
				pdvitems++;
		}
	}

	private void getOpenPrs(List<PeerReview> prs) {
		for (PeerReview pr : prs) {
			if (pr.getCompleted() == null || pr.getCompleted().getTime() == 0) {
				// TODO: this is kinda hacky how we show who is responsible
				User hacker = pr.getAssessment().getAssessor().get(0);
				String name = hacker.getFname() + " " + hacker.getLname();
				if (openReviews.containsKey(name)) {
					openReviews.put(name, openReviews.get(name).intValue() + 1);
				} else {
					openReviews.put(name, 1);
				}

				if (pr.getCreated().getTime() < past7days.getTimeInMillis()) {
					pritems++;
				}

			}
		}
	}

	private void getOpenVulns(List<Assessment> as, EntityManager em) {
		for (int i = 0; i < 10; i++) {

			data.put(i, "0");
		}
		for (Assessment a : as) {
			if ((a.getCompleted() == null || a.getCompleted().getTime() == 0)
					&& a.getEnd().getTime() < past7days.getTimeInMillis())
				pditems++;

			for (Vulnerability v : a.getVulns()) {

				if (v.getOverall() == null)
					continue;

				String count = data.get(v.getOverall().intValue());
				if (v.getOverall() == null || v.getOverall() == -1l)
					continue;
				data.put(v.getOverall().intValue(), "" + (Integer.parseInt(count) + 1));
				addVulntoList(v);
				User u;
				if (v.getAssessorId() != null)
					u = em.find(User.class, v.getAssessorId());
				else {
					u = a.getAssessor().get(0);
				}
				String name = u.getFname() + " " + u.getLname();
				if (!topUsers.containsKey(name)) {
					UserRate ur = new UserRate();
					ur.name = name;
					topUsers.put(name, ur);

				} else
					topUsers.get(name).findings++;
			}
		}
	}


	private void addVulntoList(Vulnerability v) {
		String name = "";
		if (v.getDefaultVuln() == null)
			name = "Uncategorized";
		else
			name = v.getDefaultVuln().getName();

		if (topVulns.containsKey(name)) {
			topVulns.put(name, topVulns.get(name).intValue() + 1);
		} else {
			topVulns.put(name, 1);
		}
		topVulns = this.sortByComparator(topVulns, false);

	}

	private void addPRList(List<Comment> coms) {
		for (Comment com : coms) {
			if (com.getCommenters() != null) {
				for (User commenter : com.getCommenters()) {
					String name = commenter.getFname() + " " + commenter.getLname();
					if (topReviews.containsKey(name)) {
						topReviews.put(name, topReviews.get(name).intValue() + 1);
					} else {
						topReviews.put(name, 1);
					}
				}
			}
		}
		topReviews = this.sortByComparator(topReviews, false);

	}

	public String getActiveMetrics() {
		return "active";
	}

	public int getCriticalN() {
		return criticalN;
	}

	public int getHighN() {
		return highN;
	}

	public int getMediumN() {
		return mediumN;
	}

	public int getLowN() {
		return lowN;
	}

	public Map<String, UserRate> getTopUsers() {
		return topUsers;
	}

	public Map<String, Integer> getTopVulns() {
		int i = 0;

		List<String> delete = new ArrayList<>();
		for (String key : this.topVulns.keySet()) {
			if (i++ > 10)
				delete.add(key);
		}
		for (String key : delete) {
			this.topVulns.remove(key);
		}

		return topVulns;
	}

	public Map<String, Integer> getTopReviews() {
		return topReviews;
	}

	public Map<String, Integer> getOpenReviews() {
		return openReviews;
	}

	public int getPditems() {
		return pditems;
	}

	public int getPritems() {
		return pritems;
	}

	public int getPdvitems() {
		return pdvitems;
	}

	public Map<String, Integer> getPastDueAsmt() {
		return pastDueAsmt;
	}

	public Map<String, Integer> getPastDueVulns() {
		return pastDueVulns;
	}


	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDates() {
		return dates;
	}

	public String getCrits() {
		return crits;
	}

	public String getHighs() {
		return highs;
	}

	public String getMeds() {
		return meds;
	}

	public String getLows() {
		return lows;
	}

	public List<Assessment> getAsmts() {
		return asmts;
	}

	public TreeMap<Integer, String> getTopUsersSorted() {
		return topUsersSorted;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

	public List<String> getColors() {
		return colors;
	}

	public HashMap<Integer, String> getData() {
		return data;
	}

	public void setCampId(Long campId) {
		this.campId = campId;
	}

	private LinkedHashMap<String, Integer> sortByComparator(Map<String, Integer> unsortMap, boolean order) {

		List<Entry<String, Integer>> list = new ArrayList<>();
		list.addAll(unsortMap.entrySet());

		// Sorting the list based on values
		if(order) {
			Collections.sort(list, 
				(Entry<String, Integer> o1, Entry<String, Integer> o2) -> o1.getValue().compareTo(o2.getValue())
			);
		}else {
			Collections.sort(list, 
				(Entry<String, Integer> o1, Entry<String, Integer> o2) -> o2.getValue().compareTo(o1.getValue())
			);
		}

		// Maintaining insertion order with the help of LinkedList
		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	private void plotAssessment(List<Assessment> asmts, List<RiskLevel> levels, boolean isCamp) {
		for (Assessment a : asmts) {
			if (a.getCompleted() == null || a.getCompleted().getTime() == (new Date(0)).getTime()) {
				continue;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date chartDate = a.getCompleted() == null ? a.getEnd() : a.getCompleted();
			if (isCamp) {
				dates += ", [\"" + sdf.format(chartDate) + "\",\"" + a.getAppId() + "\",\" " + a.getName() + "\"]";
			} else {
				dates += ", \"" + sdf.format(chartDate) + "\"";
			}
			HashMap<Integer, Integer> count = new HashMap();
			for (RiskLevel level : levels) {
				count.put(level.getRiskId(), 0);
			}
			for (Vulnerability v : a.getVulns()) {
				if (v.getOverall() == null || v.getOverall() == -1l)
					continue;
				int c = count.get(v.getOverall().intValue());
				count.put(v.getOverall().intValue(), c + 1);
			}

			for (RiskLevel level : levels) {

				String risk = level.getRisk();
				if (risk == null || risk.equals("") || risk.toLowerCase().startsWith("unassigned"))
					continue;
				String array = data.get(level.getRiskId()) == null ? "" : data.get(level.getRiskId());
				data.put(level.getRiskId(), array + "," + count.get(level.getRiskId()));

			}

		}
	}

}
