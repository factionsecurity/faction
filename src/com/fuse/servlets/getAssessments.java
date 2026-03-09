package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.OOO;
import com.fuse.dao.PeerReview;
import com.fuse.dao.Permissions;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

/**
 * Servlet implementation class getAssessments
 */
public class getAssessments extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public getAssessments() {
		super();

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null)
			return;
		if (user.getPermissions().isAssessor() || user.getPermissions().isManager()) {
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			PrintWriter out = response.getWriter();

			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			try {

				String query = "db.Assessment.find({ '$query' : {'assessor' : " + user.getId()
						+ ", 'completed' : {$exists: false}}} , $orderby: { 'start' : -1 })";

				List<Assessment> assessments = (List<Assessment>) em.createNativeQuery(query, Assessment.class)
						.setMaxResults(50).getResultList();
				List<RiskLevel> levels = (List<RiskLevel>) em.createQuery("from RiskLevel order by riskId desc")
						.getResultList();
				
				int prcount =0;
				
				if(user.getPermissions().getAccessLevel() != Permissions.AccessLevelTeamOnly) {
					List<PeerReview> prs = (List<PeerReview>) em.createQuery("from PeerReview").getResultList();
					prcount = prs.stream().filter(
							pr -> pr.getAssessment().getAssessor().stream().anyMatch(u -> u.getId() != user.getId()))
							.collect(Collectors.toList()).size();
				}


				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String json = "{ 'count' : " + assessments.size() + ",\n";
				json += "'prcount' : " + prcount + ",\n";
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
							html += "<span style=\\\"font-size:small; color:white; background:" + colors[i--]
									+ "\\\" class=\\\"circle\\\" " + "title=\\\"" + name + "\\\"><b>&nbsp;"
									+ counts.get(level.getRiskId()) + "&nbsp;</b></span>&nbsp;";
						}
					}

					json += "[ '" + a.getName() + "'," + "'" + a.getAppId() + "'," + "'" + format.format(a.getStart())
							+ "'," + "'" + html + "', " + "'app" + a.getId() + "']\n";
					isFirst = false;

				}
				json += "]}";
				json = json.replaceAll("\'", "\"");
				out.println(json);

			} catch (Exception Ex) {
				out.println("{ \"count\" : 0}");
				Ex.printStackTrace();
			}
			em.close();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null)
			return;

		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		String[] ids = request.getParameter("id").split(",");

		String valid = "";
		for (String id : ids) {
			valid += Long.parseLong(id) + ",";
		}
		valid = valid.substring(0, valid.length() - 1);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -12);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String threeMonths = format.format(c.getTime());
		List<Assessment> assessments = (List<Assessment>) em.createNativeQuery(
				"{'assessor' : { '$in' : [" + valid + "]}, 'start' : {'$gt':ISODate('" + threeMonths + "')}}",
				Assessment.class).getResultList();

		List<OOO> ooo = (List<OOO>) em.createNativeQuery(
				"{'user_Id' : { '$in' : [" + valid + "]}, 'start' : {'$gt':ISODate('" + threeMonths + "')}}", OOO.class)
				.getResultList();

		String json = "{ 'count' : " + (assessments == null ? "0" : assessments.size()) + ",\n";
		json += " 'ocount' : " + (ooo == null ? "0" : ooo.size()) + ",\n";
		json += "'assessments' : [";
		boolean isFirst = true;
		for (Assessment a : assessments) {
			String assessors = "";
			for (User u : a.getAssessor()) {
				assessors += u.getFname() + " " + u.getLname() + "; ";
			}
			if (!isFirst) {
				json += ",";
			}
			json += "[ \"" + StringEscapeUtils.escapeJavaScript(a.getName()) + "\",\""
					+ StringEscapeUtils.escapeJavaScript(a.getAppId()) + "\",'" + a.getStart() + "','app" + a.getId()
					+ "', '" + a.getEnd() + "', '" + assessors + "']\n";
			isFirst = false;

		}
		json += "],\n";
		json += "'ooo' : [ \n";
		isFirst = true;
		for (OOO o : ooo) {
			if (!isFirst) {
				json += ",";
			}
			json += "['" + o.getTitle() + "', '" + o.getId() + "','" + o.getStart() + "', '" + o.getEnd() + "']\n";
			isFirst = false;

		}
		json += "]}";
		json = json.replaceAll("\'", "\"");
		out.println(json);
		em.close();

	}

}
