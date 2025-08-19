package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.fuse.dao.Assessment;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;


/**
 * Servlet implementation class getImage
 */
public class getStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User)request.getSession().getAttribute("user");
		if(user == null)
			return;
		if(user.getPermissions().isAssessor() || user.getPermissions().isManager()){
			response.setContentType("application/json; charset=UTF-8");
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0); // Proxies.
			PrintWriter out = response.getWriter();
			out.println("[");

			
			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			boolean isFirst=true;
			try{
				
				List<Assessment> assessments = AssessmentQueries.getAllAssessments(em, user, AssessmentQueries.OnlyNonCompleted);
				
			
				for(Assessment a : assessments){
					
					String json ="";
					if(isFirst){
						isFirst = false;
					}else
						json += " , ";
					json += "{ 'report' : " + (a.getFinalReport() == null ? "false" : "true") +" , " +
						" 'id' : " + a.getId() + " , ";
					PeerReview pr =null;
					try{
						
						pr = (PeerReview)em
							.createNativeQuery("{\"assessment_id\" : "+a.getId() +"}", PeerReview.class)
							.getResultList().stream().findFirst().orElse(null);
					}catch(Exception ex){}
					json += " 'submitted' : " + (pr != null ? "true" : "false")	 + " , " +
							" 'prCompleted' : " + (pr != null && pr.getCompleted() != null && pr.getCompleted().getTime() != 0 ? "true" : "false") + " } ";
					json = json.replaceAll("'", "\"");
					out.write(json);
					
				}
			}catch(Exception Ex){
				Ex.printStackTrace();
				out.println("[]");
			}
			
			em.close();
			
			out.println("]");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
