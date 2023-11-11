package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class DatetimeTagHandler extends TagSupport{

    private String name;
    private String id;
    private int colsize;
    private boolean readOnly = false;
    
    
     
    @Override
    public int doStartTag() throws JspException {
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String html="<div class='col-md-"+ this.colsize+"'>"
            		+" <div class='form-group'>"
            		+"     <label>"+ this.name + "</label>"
            		+"     <div class='input-group'>"
            		+"       <div class='input-group-addon'>"
            		+"         <i class='fa fa-calendar'></i>"
            		+"       </div>"
            		+"       <input type='text' class='form-control pull-right' id='"+ this.id+"' value='"; 
					
            out.println(html);
 
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
            String html="";
            if(readOnly) {
            	html+="' readonly />";
            
            }else {
            		html+="'/>";
            }
            html+="     </div>\n" + 
            		"</div>\n" + 
            		"</div>";
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
	public int getColsize() {
		return colsize;
	}
	public void setColsize(int colsize) {
		this.colsize = colsize;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	

	
    

}