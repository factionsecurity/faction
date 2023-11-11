package com.fuse.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class RowTagHandler extends TagSupport{


     
    @Override
    public int doStartTag() throws JspException {
         
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String html="<div class='row'>\r\n";
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
            String html="</div>\r\n";
            out.println(html);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }


}