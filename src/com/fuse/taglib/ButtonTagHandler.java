package com.fuse.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class ButtonTagHandler extends TagSupport{

    private String text;
    private String id;
    private String color;
    private String size;
    private int colsize;
    private boolean addlabel=false;
     
    @Override
    public int doStartTag() throws JspException {
         
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String html="";
            if(addlabel) {
            	html +="<div class='col-md-"+ this.colsize + "'>\r\n"
                		+ "<div class='form-group'>\r\n"
                		+ "<label>&nbsp;</label>\r\n"
                		+ "<button class='btn btn-block btn-" + this.color + " btn-" + this.size + "' id='"+this.id+"'>" + this.text + "</button>"
                		+ "\r\n</div></div>";
            }else {
            	html="<div class='col-md-"+ this.colsize + "'>\r\n<button class='btn btn-block btn-" + this.color + " btn-" + this.size + "' id='"+this.id+"'>" + this.text + "</button>\r\n</div>";
            }
            out.println(html);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getColor() {
		return color;
	}


	public void setColor(String color) {
		this.color = color;
	}


	public String getSize() {
		return size;
	}


	public void setSize(String size) {
		this.size = size;
	}


	public int getColsize() {
		return colsize;
	}


	public void setColsize(int colsize) {
		this.colsize = colsize;
	}


	public void setAddlabel(boolean inlabel) {
		this.addlabel = inlabel;
	}
	
	
	
	



}