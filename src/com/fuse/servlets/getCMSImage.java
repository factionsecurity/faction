package com.fuse.servlets;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.User;

/**
 * Servlet implementation class getImage
 */
public class getCMSImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getCMSImage() {
        super();
       
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User)request.getSession().getAttribute("user");
		if(!(request.getRemoteAddr().equals(request.getLocalAddr()) || 
				( user != null && user.getPermissions().isAdmin() )
				
			)){
			return;
		}
		
		
		Long id = Long.parseLong(request.getParameter("id"));
		//Session session = HibHelper.getSessionFactory().openSession();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		Image image = (Image)em.createQuery("from Image where id = :id").setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		response.setContentType(image.getContentType());
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0); // Proxies.

		String b64img = image.getBase64Image();
		byte[] stepImage;
		try {
		
			stepImage = Base64.decodeBase64(b64img.getBytes());
			ServletOutputStream output = response.getOutputStream();
			output.write(stepImage, 0, stepImage.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
