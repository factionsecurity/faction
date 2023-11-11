package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.fuse.dao.HibHelper;
import com.fuse.dao.ReportOptions;

/**
 * Servlet implementation class rd_styles
 */
public class rd_styles extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public rd_styles() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Session session = HibHelper.getSessionFactory().openSession();
		//HibHelper hh = new HibHelper();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		
		ReportOptions rpo = (ReportOptions)em.createQuery("from ReportOptions").getResultList().stream().findFirst().orElse(null);
		int offset = request.getParameter("offset") == null || request.getParameter("offset") == ""
				? 0 : Integer.parseInt(request.getParameter("offset"));
		//String num = rpo.getSize().split("[a-zA-Z]{1,3}")[0];
		//String units = rpo.getSize().split("[0-9]{1,3}")[1];
		//String fontsize = (Integer.parseInt(num) + offset ) + units;
		response.setContentType("text/css");
		String css = "body"+
				"{"+
				"   font-family: Arial;"+
				"}"+
				".cke_editable"+
				"{"+
				"	font-size: 20px;"+
				"	line-height: 1.2;"+
				"}"+
				"blockquote"+
				"{"+
				"	font-style: italic;"+
				"	font-family: Georgia, Times, \"Times New Roman\", serif;"+
				"	padding: 2px 0;"+
				"	border-style: solid;"+
				"	border-color: #ccc;"+
				"	border-width: 0;"+
				"}"+
				".cke_contents_ltr blockquote"+
				"{"+
				"	padding-left: 20px;"+
				"	padding-right: 8px;"+
				"	border-left-width: 5px;"+
				"}"+
				".cke_contents_rtl blockquote"+
				"{"+
				"	padding-left: 8px;"+
				"	padding-right: 20px;"+
				"	border-right-width: 5px;"+
				"}"+
				"a"+
				"{"+
				"	color: #0782C1;"+
				"}"+
				"ol,ul,dl"+
				"{"+
				"	/* IE7: reset rtl list margin. (#7334) */"+
				"	*margin-right: 0px;"+
				"	/* preserved spaces for list items with text direction other than the list. (#6249,#8049)*/"+
				"	padding: 0 40px;"+
				"}"+
				"h1,h2,h3,h4,h5,h6"+
				"{"+
				"	font-weight: normal;"+
				"	line-height: 1.2;"+
				"}"+
				"hr"+
				"{"+
				"	border: 0px;"+
				"	border-top: 1px solid #ccc;"+
				"}"+
				"img.right"+
				"{"+
				"	border: 1px solid #ccc;"+
				"	float: right;"+
				"	margin-left: 15px;"+
				"	padding: 5px;"+
				"}"+
				"img.left"+
				"{"+
				"	border: 1px solid #ccc;"+
				"	float: left;"+
				"	margin-right: 15px;"+
				"	padding: 5px;"+
				"}"+
				"pre"+
				"{"+
				"	white-space: pre-wrap; /* CSS 2.1 */"+
				"	word-wrap: break-word; /* IE7 */"+
				"	-moz-tab-size: 4;"+
				"	tab-size: 4;"+
				"}"+
				".marker"+
				"{"+
				"	background-color: Yellow;"+
				"}"+
				"span[lang]"+
				"{"+
				"	font-style: italic;"+
				"}"+
				"figure"+
				"{"+
				"	text-align: center;"+
				"	border: solid 1px #ccc;"+
				"	border-radius: 2px;"+
				"	background: rgba(0,0,0,0.05);"+
				"	padding: 10px;"+
				"	margin: 10px 20px;"+
				"	display: inline-block;"+
				"}"+
				"figure > figcaption"+
				"{"+
				"	text-align: center;"+
				"	display: block; /* For IE8 */"+
				"}"+
				"a > img {"+
				"	padding: 1px;"+
				"	margin: 1px;"+
				"	border: none;"+
				"	outline: 1px solid #0782C1;"+
				"}"+
				"p{"+
				"word-wrap: break-word; "+
				"overflow:hidden;"+
				"white-space: wrap;"+
				"padding:0px;"+
				"margin:0px" +
				"}";
		css += (rpo == null ? "" : rpo.getBodyCss());
		
		PrintWriter out = response.getWriter();
		out.print(css);
		em.close();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
