package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.OOO;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.utils.AccessControl;

/**
 * Servlet implementation class getAssessments
 */
public class getVerifications extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getVerifications() {
        super();
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User)request.getSession().getAttribute("user");
		if(user == null)
			return;
		if(user.getPermissions().isAssessor() || user.getPermissions().isManager()){
		
			PrintWriter out = response.getWriter();
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0); // Proxies.
			
			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			try{
				//"{$query : {\"assessor_Id\" : "+user.getId() +"} , $orderby : {start : +1}}",
				String query = "{\"assessor_Id\" : "+user.getId() +", \"completed\" : ISODate(\"1970-01-01T00:00:00Z\"), $orderby: { \"start\" : -1 }}";
				List<Verification>verifications = (List<Verification>)em.createNativeQuery(query, Verification.class).getResultList();
				
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				
				String json = "{ 'count' : " + verifications.size() + ",\n";
				json += "'verifications' : [";
				boolean isFirst=true;
				for(Verification v : verifications){
					if(!isFirst){
						json += ",";
					}
					v.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					json += "[ '"+v.getAssessment().getName() + "',"
							+ "'" +v.getAssessment().getAppId() + "',"
							+ "'" + format.format(v.getStart()) + "',"
							+ "'" + v.getId()+ "',"
							+ "'" + v.getVerificationItems().get(0).getVulnerability().getName() +"',"
							+ "'" + v.getVerificationItems().get(0).getVulnerability().getOverallStr() + "']\n";
					isFirst=false;
					
				}
				json+="]}";
				json=json.replaceAll("\'", "\"");
				out.println(json);
			}catch(Exception Ex){
				out.println("{ \"count\" : 0}");
			}
			em.close();
			//HibHelper.getInstance().closeEM();
			//em.close();
			//HibHelper.getInstance().closeEMF();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User u = (User)request.getSession().getAttribute("user");
		if(u== null)
			return;
		
		PrintWriter out = response.getWriter();
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0); // Proxies.
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		User user = (User) request.getSession().getAttribute("user");
		Long id = Long.parseLong(request.getParameter("id"));
	
		
		try{
			List<Verification>verifications = (List<Verification>)em.createNativeQuery("{\"assessor_Id\" : "+id +"} ", Verification.class).getResultList();
			List<Assessment>assessments = (List<Assessment>)em.createNativeQuery("{\"assessor_Id\" : "+id +"} ", Assessment.class).getResultList();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			List<OOO>ooo = (List<OOO>)em.createNativeQuery("{\"user_Id\" : "+id+"}", OOO.class).getResultList();
			
			String json = "{ 'count' : " + verifications.size() + ",\n";
			json += " 'ocount' : " + (ooo == null ? "0" : ooo.size())+ ",\n";
			json += " 'acount' : " + (assessments == null ? "0" : assessments.size())+ ",\n";
			json += "'verifications' : [";
			boolean isFirst=true;
			for(Verification v : verifications){
				if(!isFirst){
					json += ",";
				}
				json += "[ '"+v.getAssessment().getName() + "','" +v.getAssessment().getAppId() + "','" + v.getStart() + "','" + v.getId()+ "','" + v.getEnd() + "','" + v.getVerificationItems().get(0).getVulnerability().getName() +"']\n";
				isFirst=false;
				
			}
			json+="],\n";
			
			json += "'assessments' : [";
			isFirst=true;
			for(Assessment a : assessments){
				if(!isFirst){
					json += ",";
				}
				json += "[ '"+a.getName() + "','" +a.getAppId() + "','" + a.getStart() + "','" + a.getId()+ "','" + a.getEnd() + "']\n";
				isFirst=false;
				
			}
			json+="],\n";
			
			
			json+="'ooo' : [ \n";
			isFirst=true;
			for(OOO o : ooo){
				if(!isFirst){
					json += ",";
				}
				json += "['"+o.getTitle() + "', '"+ o.getId() + "','" + o.getStart() + "', '" + o.getEnd()+"']\n";
				isFirst=false;
				
			}
			json+="]}";
			json=json.replaceAll("\'", "\"");
			out.println(json);
		}catch(Exception Ex){
			Ex.printStackTrace();
			out.println("{ \"count\" : 0}");
		}
		//hh.closeEM();
		em.close();
		//HibHelper.getInstance().closeEMF();
		
	}

}
