package com.fuse.taglib;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.fuse.dao.Assessment;

public class AssessmentStatus  extends TagSupport{
	private Assessment asmt;
     
    @Override
    public int doStartTag() throws JspException {
         
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();

			String status = "Scheduled";
			Date now = new Date();
			if(now.after(asmt.getStart())) {
				status = "In Progress";
			}
			if(now.after(asmt.getEnd())) {
				status = "Past Due";
			}
			if(asmt.getCompleted() != null) {
				status="Completed";
			}
            out.println(status);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EVAL_BODY_AGAIN;
    }
    @Override
    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

	public void setAsmt(Assessment asmt) {
		this.asmt = asmt;
	}
	


}
