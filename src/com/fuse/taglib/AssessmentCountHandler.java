package com.fuse.taglib;

import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.fuse.dao.Assessment;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.Vulnerability;

public class AssessmentCountHandler extends TagSupport{

	private Assessment asmt;
	private List<RiskLevel>levels;
     
    @Override
    public int doStartTag() throws JspException {
         
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            HashMap<Integer, Integer>counts = new HashMap();
            for(int i=0; i<10; i++){
    			counts.put(i, 0);
    		}
            for(Vulnerability v : asmt.getVulns()){
            	if(v.getOverall() == null || v.getOverall() == -1l)
            		continue;
            	else
            		counts.put(v.getOverall().intValue(), counts.get(v.getOverall().intValue())+1);
    		}
            
            
	        String []  colors = new String []{	"#8E44AD", "#9B59B6", "#2C3E50", 
					"#34495E", "#95A5A6", "#00a65a", 
					"#39cccc", "#00c0ef", "#f39c12", 
					"#dd4b39"};
            String html = "";
            int i=9;
            for(RiskLevel level : levels){
            	String name = level.getRisk();
            	if(name != null && !name.equals("") && !name.toLowerCase().equals("unassigned")){
            		html+="<span style='font-size:small; color:" + colors[i] + "; border: solid 1px " + colors[i--] + "' class=\"circle\" "
            				+ "title=\"" + name + "\"><b>&nbsp;"+counts.get(level.getRiskId())+"&nbsp;</b></span>&nbsp;";
            	}
            }

            out.println(html);
 
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
	public List<RiskLevel> getLevels() {
		return levels;
	}
	public void setLevels(List<RiskLevel> levels) {
		this.levels = levels;
	}
	

	
	



}