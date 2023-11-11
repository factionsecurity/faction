package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class MdColTagHandler extends TagSupport {

	private int colsize;
	private String style = "";

	@Override
	public int doStartTag() throws JspException {

		try {
			// Get the writer object for output.
			JspWriter out = pageContext.getOut();
			String html = "<div class='col-md-" + this.colsize + " col-xs-" + this.colsize + "' style='" + this.style
					+ "'>\r\n";
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
			String html = "</div>\r\n";
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

	public void setStyle(String style) {
		this.style = style;

	}

	public String getStyle() {
		return this.style;

	}

}