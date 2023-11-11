package com.fuse.taglib;

import java.io.IOException;
import java.util.Random;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

public class TextEditorHandler extends TagSupport{

    private String name;
    private String id;
    private String classname;
    private boolean  readonly=false;
    private String toolbar;
    private boolean clickToEnable=false;
    
    
     
    @Override
    public int doStartTag() throws JspException {
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String ro = "";
            if(this.readonly)
            	ro="readonly";
            if(this.isClickToEnable())	{
	            String html=String
	            	.format("<div><div class='fseditor' id=\"%s\" name=\"%s\" rows=\"10\" cols=\"80\" %s >",
	            			this.id, this.name, ro);
						
	            out.println(html);
            }else {
	            String html=String
		            	.format("<div><textarea id=\"%s\" name=\"%s\" rows=\"10\" cols=\"80\" %s >",
		            			this.id, this.name, ro);
							
		            out.println(html);
            }
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EVAL_BODY_INCLUDE;
    }
    @Override
    public int doEndTag() throws JspException {
    	try {
	    	String html =  "";
	    	if(this.isClickToEnable())	{
	    		html+="</div><div class='fseditor_icon'><i class='fa fa-edit'></i></div></div>";
	    	}else {
	    		html+="</textarea></div>";
	    	}
	    	Random rand = new Random();

	    	
	    	if(this.isClickToEnable())	{
	    		int  n = rand.nextInt(50) + 1;
		    	String strId = "id_"+n;
		    	String strName="name_"+n;
	    	
		    	html+=String.format("<script>"
		    			+ "/*$(function(){"
		    				+ "$('#%s').click(function(){"
		    					+ "showTextEditor('%s','%s','%s');"
		    				+ "});"
		    			+ "});*/"
		    			+ "</script>",
		    			this.id,this.id, this.name,this.toolbar);
	    	}else {
	    		html+=String.format("<script>"
		    			+ "/*$(function(){"
		    					+ "showTextEditor('%s','%s');"
		    			+ "});*/"
		    			+ "</script>",
		    			this.name,this.toolbar);
	    	}
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
	public boolean isReadonly() {
		return readonly;
	}
	public String getToolbar() {
		return toolbar;
	}
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	public void setToolbar(String toolbar) {
		this.toolbar = toolbar;
	}
	public boolean isClickToEnable() {
		return clickToEnable;
	}
	public void setClickToEnable(boolean clickToEnable) {
		this.clickToEnable = clickToEnable;
	}
	
	

	
	
    

}