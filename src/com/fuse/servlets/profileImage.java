package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import com.fuse.dao.ExploitStep;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.ProfileImage;
import com.fuse.dao.User;
//import com.fuse.dao.VTImage;


/**
 * Servlet implementation class getImage
 */
public class profileImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public profileImage() {
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
		String guid = request.getParameter("id");
		//Session s = HibHelper.getSessionFactory().openSession();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		ProfileImage image=null;
		if(guid == null){
			guid = user.getAvatarGuid();
			
		}
		
		if(guid == null || guid.equals("")){
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", "../dist/img/default-avatar.png");
			
		}else{
			image = (ProfileImage)em.createQuery("from ProfileImage where guid = :guid")
						.setParameter("guid", guid)
						.getResultList().stream().findFirst().orElse(null);
			
			
			
			if(image == null){
				response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				response.setHeader("Location", "../dist/img/default-avatar.png");
				
			}else{
				response.setContentType(image.getContenType());
				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
				response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
				response.setDateHeader("Expires", 0); // Proxies.
		
				String b64img = image.getBase64Image();
				byte[] outImage;
				outImage = Base64.getDecoder().decode(b64img);
				ServletOutputStream output = response.getOutputStream();
				output.write(outImage, 0, outImage.length);
			}
		}
		em.close();
		

		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
