package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class BoxTagHandler extends TagSupport {

	private String type;
	private String title;
	private String id="";

	@Override
	public int doStartTag() throws JspException {

		try {
			// Get the writer object for output.
			JspWriter out = pageContext.getOut();
			String html = "<div id='" + this.id + "' class='box box-" + this.type + "'>\r\n" + "<div class='box-header with-border'>\r\n"
					+ "  <h3 class='box-title'>" + this.title + "</h3>\r\n" + "</div>\r\n"
					+ "  <div class='box-body'>\r\n";
			out.println(html);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return EVAL_BODY_AGAIN;
	}

	@Override
	public int doEndTag() throws JspException {

		try {
			// Get the writer object for output.
			JspWriter out = pageContext.getOut();
			String html = " </div>\r\n" + "</div>\r\n";
			out.println(html);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setId(String id) {
		this.id = id;
	}

}