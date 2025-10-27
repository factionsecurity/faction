package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class InputgroupTagHandler extends TagSupport{

    private String name;
    private String id;
    private int col;
    private String value;
    private String htmlname;
    private String placeholder;
    private boolean password=false;
    private boolean readOnly=false;
     
    @Override
    public int doStartTag() throws JspException {
        String type="text";
    	if(password)
    		type="password";
    	
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String html="<div class='col-md-"+ this.col + "'>"
            		+ "<div class='form-group'>"
            		+ "<label>"+ this.name + "</label>"
            		+ "<input type='"+type+"' " +  (placeholder==null ? " " : "placeholder='"+this.placeholder + "'")+" class='form-control pull-right' id='"+ this.id + "' "  + (htmlname==null ? " " : " name='"+this.htmlname+"' ") +" value=\"";
            out.print(html);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EVAL_BODY_AGAIN;
    }
    @Override
    public int doEndTag() throws JspException {
         
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String html = "";
            if(readOnly)
            	html="\" readonly disabled>";
            else{
            	html="\">";
            		
            }
            html += "</div>"
            		+ "</div>";
            out.print(html);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        password=false;
        return SKIP_BODY;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getColsize() {
		return col;
	}

	public void setColsize(int col) {
		this.col = col;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public String getHtmlname() {
		return htmlname;
	}
	public void setHtmlname(String htmlname) {
		this.htmlname = htmlname;
	}
	public String getPlaceholder() {
		return placeholder;
	}
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
	public boolean isPassword() {
		return password;
	}
	public void setPassword(boolean password) {
		this.password = password;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	

	
	
	
	
    

}