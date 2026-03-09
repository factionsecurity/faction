package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class SelectTagHandler extends TagSupport {

	private int colsize;
	private String id;
	private String name;
	private boolean readOnly = false;
	private String cssClass = "";

	@Override
	public int doStartTag() throws JspException {

		try {
			// Get the writer object for output.
			JspWriter out = pageContext.getOut();
			String html = " <div class='col-md-" + this.colsize + "'>" + "<div class='form-group'>" + "<label>"
					+ this.name + "</label>" + "<select class='form-control select2 " + this.cssClass
					+ "' style='width: 100%;' id='" + this.id + "'";
			if (readOnly) {
				html += " readonly disabled>";
			} else {
				html += ">";
			}

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
			String html = "</select>" + "</div>" + "</div>";
			out.println(html);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	public int getColsize() {
		return colsize;
	}

	public void setColsize(int colsize) {
		this.colsize = colsize;
	}

	public String getCssClass() {
		return this.cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

}