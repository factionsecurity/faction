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

import com.fuse.dao.ExploitStep;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
//import com.fuse.dao.VTImage;


/**
 * Servlet implementation class getImage
 */
public class getImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getImage() {
        super();
       
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*User user = (User)request.getSession().getAttribute("user");
		if(!(request.getRemoteAddr().equals(request.getLocalAddr()) || 
				( user != null && (
					user.getPermissions().isAssessor() || 
					user.getPermissions().isManager()
					)
				)
			)){
			return;
		}
		
		Long stepId = request.getParameter("stepId") != null ? Long.parseLong(request.getParameter("stepId")) : null;
		Long vulnid = request.getParameter("vulnid") != null ? Long.parseLong(request.getParameter("vulnid")) : null;
		String uuid = request.getParameter("uuid");
		//Session session = HibHelper.getSessionFactory().openSession();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		response.setContentType("image/png");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0); // Proxies.
		if(stepId != null){
			ExploitStep step = (ExploitStep)em.createQuery("from ExploitStep where id = :id").setParameter("id", stepId).getResultList().stream().findFirst().orElse(null);
			int Size = step.getImages().size();
			if( step.getImages() != null && Size > 0){
				VTImage image = step.getImages().get(0);
				String b64img = image.getBase64Image();
				byte[] stepImage;
				try {
					stepImage = Base64.decode(b64img.getBytes());
					ServletOutputStream output = response.getOutputStream();
					output.write(stepImage, 0, stepImage.length);
				} catch (Base64DecodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else if(uuid != null){
			VTImage image = (VTImage)em.createQuery("from VTImage where guid = :uuid")
					.setParameter("uuid", uuid)
					.getResultList().stream().findFirst().orElse(null);
			String b64img = image.getBase64Image();
			byte[] stepImage;
			try {
				stepImage = Base64.decode(b64img.getBytes());
				ServletOutputStream output = response.getOutputStream();
				output.write(stepImage, 0, stepImage.length);
			} catch (Base64DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		em.close();
		//session.close();*/
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
