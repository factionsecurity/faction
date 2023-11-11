package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringEscapeUtils;

import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;

/**
 * Servlet implementation class fileUpload
 */
@MultipartConfig
public class fileUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private boolean isMultipart;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public fileUpload() {
		super();

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User u = (User) request.getSession().getAttribute("user");
		if (u == null)
			return;
		String file = request.getParameter("getFile");
		String id = request.getParameter("id");

		Map<String, Files> files = (Map<String, Files>) request.getSession().getAttribute("Files");
		if (id != null && !id.equals("")) {

			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			Files f = (Files) em.createQuery("from Files where uuid = :id").setParameter("id", id).getResultList()
					.stream().findFirst().orElse(null);
			response.setContentType(f.getContentType());
			// response.setContentType("application/octet-stream");
			// response.setHeader("Content-Disposition", "attachment; filename=" +
			// f.getName());
			byte[] bytes = f.getRealFile();
			ServletOutputStream output = response.getOutputStream();
			output.write(bytes, 0, bytes.length);

			em.close();
		} else if (files == null) {
			response.setContentType("text/json");
			PrintWriter out = response.getWriter();
			out.println("{}");
		} else if (file != null && !file.equals("")) {
			Files f = files.get(file);
			response.setContentType(f.getContentType());
			// response.setContentType("application/octet-stream");
			// response.setHeader("Content-Disposition", "attachment; filename=" +
			// f.getName());
			byte[] bytes = f.getRealFile();
			ServletOutputStream output = response.getOutputStream();
			output.write(bytes, 0, bytes.length);

		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User u = (User) request.getSession().getAttribute("user");
		if (u == null)
			return;

		response.setContentType("text/json");
		String name = request.getParameter("name");
		String apid = request.getParameter("apid");
		String verificationId = request.getParameter("vid");
		String delid = request.getParameter("delId");
		isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) { // Must be a file upload
			String uuid = UUID.randomUUID().toString();
			Part filePart = request.getPart("file_data");
			Files f = new Files();
			f.setUuid(uuid);
			f.setContentType(filePart.getContentType());
			f.setName(getFileName(filePart));
			byte[] bytes = new byte[(int) filePart.getSize()];
			filePart.getInputStream().read(bytes);
			f.setRealFile(bytes);
			if (apid != null && !apid.equals("")) { // if there is an apId then we can add this to the current
													// application
				f.setType(Files.ASSESSMENT);
				f.setEntityId(Long.parseLong(apid));
				EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(f);
				HibHelper.getInstance().commit();
				em.close();
			} else if (verificationId != null && !verificationId.equals("")) { // if there is an verificationId then we
																				// can add
																				// this to the current verification
				f.setType(Files.VERIFICATION);
				f.setEntityId(Long.parseLong(verificationId));
				EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(f);
				HibHelper.getInstance().commit();
				em.close();

			} else { // otherwise lets save it in the session for now.
				Map<String, Files> files = (Map<String, Files>) request.getSession().getAttribute("Files");
				if (files == null) {
					files = new HashMap<String, Files>();
					// System.out.println("Files is null");
				}
				files.put(uuid, f);
				request.getSession().setAttribute("Files", files);
			}
			// System.out.println("---------------------------------");
			// for(Files fi : files.values()){
			// System.out.println(fi.getName());
			// }
			PrintWriter out = response.getWriter();
			String uid = "A" + UUID.randomUUID().toString();
			String json = "{";
			if (f.getContentType().contains("image") && (apid == null || apid.equals(""))) {
				json += " \"initialPreview\" : [\"<img src='../service/fileUpload?id=" + uuid
						+ "' class='file-preview-image' />\"], ";
			} else if (f.getContentType().contains("image") && apid != null && !apid.equals("")) {
				json += " \"initialPreview\" : [\"<img src='../service/fileUpload?id=" + uuid
						+ "' class='file-preview-image' />\"], ";
			} else if (f.getContentType().contains("text")) {
				// try {
				json += "\"initialPreview\": [\"<pre class='file-preview-text' title='" + f.getName()
						+ "' style='width:100%;height:158px;' >"
						+ StringEscapeUtils.escapeJson(new String(f.getRealFile())) + "</pre>";
				json += "\"], ";

			} else {
				json += "\"initialPreview\": [\"<object class='file-object'  type='" + f.getContentType()
						+ "' height='160px' width='160px'>" + "<param name='movie' value='" + f.getName() + "'>"
						+ "<param name='controller' value='true'>" + "<param name='allowFullScreen' value='true'>"
						+ "<param name='allowScriptAccess' value='always'>" + "<param name='autoPlay' value='false'>"
						+ "<param name='autoStart' value='false'>" + "<param name='quality' value='high'>"
						+ "<div class='file-preview-other'>"
						+ "<span class='file-icon-4x'><i class='glyphicon glyphicon-file'></i></span>" + "</div>"
						+ "</object>";
				if (apid != null && !apid.equals(""))
					json += "<button style='width:100%;padding:0px;' class='btn btn-default' id='dl-" + uuid
							+ "'><i class='glyphicon glyphicon-download'></i></button>";
				json += "\"], ";
			}
			if (apid != null && !apid.equals("")) {
				json += "\"initialPreviewConfig\" : [" + "{ " + "\"caption\": \"" + f.getName() + "\", "
						+ "\"width\" : \"100px\", " + "\"url\" : \"../service/fileUpload?delid=" + f.getUuid()
						+ "&apid=" + f.getEntityId() + "&name=" + f.getName() + "\", "
						+ "\"downloadUrl\": \"../service/fileUpload?id=" + f.getUuid() + "\",\"key\" : 1}" + "]" + "}";
			} else {
				json += "\"initialPreviewConfig\" : [{ \"caption\": \"" + f.getName()
						+ "\", \"width\" : \"100px\", \"url\" : \"../service/fileUpload?name=" + uuid + "\", "
						+ "\"downloadUrl\": \"../service/fileUpload?id=" + f.getUuid() + "\",\"key\" : 1}]}";
			}
			// String json = "{ 'initialPreview' : ['<img
			// src=\\\"../service/fileUpload?getFile=" + f.getName() +"\\\"
			// class=\\\"file-preview-image\\\" />'], 'initialPreviewConfig' : [{ 'caption':
			// '"+ f.getName()+ "', 'width' : '100px', 'url' : '../service/fileUpload?name="
			// + f.getName() +"', 'key' : 1}]}";
			System.out.println(json);
			out.println(json);
		} else if (delid != null && !delid.equals("")) { /// These are file deleted from assessment pages
			// HibHelper hh = new HibHelper();
			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			// Session session = HibHelper.getSessionFactory().openSession();
			Files f = (Files) em.createQuery("from Files where uuid = :id").setParameter("id", delid).getResultList()
					.stream().findFirst().orElse(null);
			if (f != null) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.remove(f);
				HibHelper.getInstance().commit();
				em.close();
				PrintWriter out = response.getWriter();
				out.println("{}");
			}
			// session.close();

		} else if (name != null && !name.equals("") && apid != null && !apid.equals("")) { // These are files deleted
																							// from Engagement Edit
																							// pages.
			// Session session = HibHelper.getSessionFactory().openSession();
			// HibHelper hh = new HibHelper();
			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			Files f = (Files) em.createQuery("from Files where name = :name and entityId = :eid")
					.setParameter("name", name).setParameter("eid", Long.parseLong(apid)).getResultList().stream()
					.findFirst().orElse(null);
			if (f != null) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.remove(f);
				HibHelper.getInstance().commit();
				// hh.closeEM();
				/*
				 * session.getTransaction().begin(); session.delete(f);
				 * session.getTransaction().commit();
				 */
				PrintWriter out = response.getWriter();
				out.println("{}");
			}
			em.close();

		} else if (name != null && !name.equals("")) {
			Map<String, Files> files = (Map<String, Files>) request.getSession().getAttribute("Files");
			response.setContentType("text/json");
			files.remove(name);
			PrintWriter out = response.getWriter();
			out.println("{}");
		}

	}

	private String getFileName(Part p) {
		String header = p.getHeader("Content-Disposition");
		String[] headers = header.split(";");
		for (String item : headers) {
			if (item.trim().startsWith("filename="))
				return item.replace("filename=", "").replace("\"", "").trim();
		}
		return "";
	}

}