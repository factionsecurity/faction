package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

public class DatatableTagHandler extends TagSupport{

    private String name;
    private String id;
    private String classname;
    private String columns;
    
    
     
    @Override
    public int doStartTag() throws JspException {
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String [] cols = this.columns.split(",");
            String html="<div class='box " + this.classname + "'>"
                +"<div class='box-body'>"
                +"<table id='" + this.id + "' class='table tabletable-striped table-hover dataTable'>"
                +"<thead class=\"theader\">"
                       +"<tr>";
                for(String col : cols){
                		html+="<th>" + col +"</th>";
                }
                html += "</tr>"
                     +"</thead>"
                     +"<tbody>";
					
            out.println(html);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EVAL_BODY_INCLUDE;
    }
    @Override
    public int doEndTag() throws JspException {
    	try {
	    	String html =  "</tbody>"
	                +"<tfoot>"
	                +"</tfoot>"
	              +"</table>"
	            +"</div>"
	          +"</div>";
	    	JspWriter out = pageContext.getOut();
	    	out.println(html);
    	 } catch (IOException e) {
             e.printStackTrace();
         }
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

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	
    

}