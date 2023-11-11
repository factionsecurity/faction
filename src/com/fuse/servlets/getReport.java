package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.fuse.dao.Assessment;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.FinalReport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Permissions;
import com.fuse.dao.User;
//import com.fuse.dao.VTImage;
import com.fuse.dao.Verification;
import com.fuse.tasks.ReportGenThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.GenerateReport;
import org.apache.commons.codec.binary.Base64;

/**
 * Servlet implementation class getImage
 */
public class getReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getReport() {
        super();
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		User user = (User)request.getSession().getAttribute("user");
		
		if(user == null || !(user.getPermissions().isAssessor() || user.getPermissions().isManager() || user.getPermissions().isAdmin())){
			//return;
		}
		String test = request.getParameter("test");
		String team = request.getParameter("team");
		String type = request.getParameter("type");
		
		
		//Session session = HibHelper.getSessionFactory().openSession();
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0); // Proxies.
		//response.setContentType("application/pdf");
		//response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		if(test == null){
			
			String b64Rpt = "";
			Assessment assessment= null;
			FinalReport finalreport = null;
			if(request.getParameter("id") != null){
				Long id = Long.parseLong(request.getParameter("id"));
				assessment = (Assessment) em.createQuery("from Assessment where id = :id").setParameter("id",id).getResultList().stream().findFirst().orElse(null);
				boolean found=false;
				for(User u : assessment.getAssessor()){
					if(u.getId() == user.getId()){
						found=true;
						break;
					}
				}
				if(!found){ // check if authorized via verifications
					String mongo ="{ 'assessment_id' : " + assessment.getId() + "}";
					mongo = mongo.replace("'", "\"");
					Verification ver = (Verification) em.createNativeQuery(mongo, Verification.class).getResultList()
						.stream().findFirst().orElse(null);
					if(ver != null && ver.getAssessor().getId() == user.getId()){
						found = true;
					}
					
				}
				//FIXME: this will not work in all situations
				if(!found && user.getPermissions().getAccessLevel() != Permissions.AccessLevelAllData)
					return; //not authorized
				if(request.getParameter("retest") != null){

					
					// All reports must be placed in a queue to prevent running the server out of memory
					ReportGenThread reportThread = new ReportGenThread("", assessment, assessment.getAssessor(), true);
					TaskQueueExecutor.getInstance().execute(reportThread);
					
					//wait for it to complete
					int breakit = 0;
					while(!reportThread.complete && breakit < 2*20 ){ // wait 5 seconds
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						breakit++;
					}
					b64Rpt = reportThread.getReport();
					
					
				}else{
					b64Rpt = assessment.getFinalReport().getBase64EncodedPdf();
				}
			}else if(request.getParameter("guid") != null){
				
				String guid = request.getParameter("guid");
				finalreport = (FinalReport) em.createQuery("from FinalReport where filename = :guid").setParameter("guid",guid).getResultList().stream().findFirst().orElse(null);
				b64Rpt = finalreport.getBase64EncodedPdf();
				
			}
			else return;
			
			byte[] report;
			try {
				if(b64Rpt == null || b64Rpt.equals("")){
					response.setStatus(302);
					response.setHeader( "Location", "../DownloadError"  );
					response.setHeader( "Connection", "close" );
				}
					
				report = Base64.decodeBase64(b64Rpt.getBytes());
				String filename = "Report.docx";
				if(report.length > 3) {
					if(report[1] == (byte)'P' && report[2] == (byte)'D' && report[3] == (byte)'F') {
						response.setContentType("application/pdf");
						filename = "Report.pdf";
					}else
						response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
						
				}
				
				
				if(request.getParameter("retest") != null && request.getParameter("retest") != "true")
					filename = "Retest " + filename;
				
				if(assessment != null)
					filename = assessment.getName() + " - " + assessment.getType().getType() + " " + filename;
				else{
					String query = "{ 'finalReport_id' : " + finalreport.getId() + "}";
					Assessment tmpAsmt = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList().stream().findFirst().orElse(null);
					if(tmpAsmt == null){
						query = "{ 'retestReport_id' : " + finalreport.getId() + "}";
						tmpAsmt = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList().stream().findFirst().orElse(null);
					}
					if(tmpAsmt != null)
						filename = tmpAsmt.getName() + " - " + tmpAsmt.getType().getType() + " " + filename;
				}
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				ServletOutputStream output = response.getOutputStream();
				output.write(report, 0, report.length);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}else{
			
			//byte [] bytes = GenerateReport.testPage(em, request.getContextPath(), offset);
			String retest = request.getParameter("retest");
			boolean rt = false;
			if(retest!= null && retest.equals("true"))
				rt = true;
			
			GenerateReport genReport = new GenerateReport();
			byte [] bytes = genReport.testDocxPage(em, Long.parseLong(team), Long.parseLong(type), rt);
			if(bytes == null || bytes.length == 0){
				response.setStatus(302);
				response.setHeader( "Location", "../DownloadError"  );
				response.setHeader( "Connection", "close" );
			}
			String filename = "Report.docx";
			if(bytes.length > 3) {
				if(bytes[1] == (byte)'P' && bytes[2] == (byte)'D' && bytes[3] == (byte)'F') {
					response.setContentType("application/pdf");
					filename = "Report.pdf";
				}else
					response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
					
			}
			ServletOutputStream output = response.getOutputStream();
			output.write(bytes, 0, bytes.length);
		}
		
		em.close();
		//session.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
