package com.fuse.taglib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlTextEscapingMode;
import org.owasp.html.PolicyFactory;

import com.fuse.dao.ExploitStep;
import com.fuse.dao.Feed;
import com.fuse.dao.FeedComment;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;

public class FeedTagHandler extends TagSupport{

    private String msgBody="";
    private String id="";
    private String subject="";
    private Feed feed;
    private User user;
    private int rid;
    
    
     
    @Override
    public int doStartTag() throws JspException {
        try {
            //Get the writer object for output.
            JspWriter out = pageContext.getOut();
            String username = feed.getPoster().getFname() + " " + feed.getPoster().getLname();
            
            
            msgBody = feed.getMessage();

            
           
            
            if(subject == null || subject.equals("") )
            	subject = "";
            
            if(feed.getVulnerability() != null && !feed.getVulnerability().getName().equals(""))
            	subject = feed.getVulnerability().getName();
            
            String status="";
            if(feed.getLikes()!= null && feed.getLikes() > 0)
            	status = "" + feed.getLikes() + " Likes";
            	
            
            String html="<div class=\"box box-widget\">" +
"            <div class=\"box-header with-border\">" +
"              <div class=\"user-block\">" +
"                <img class=\"img-circle\" src=\"../service/profileImage?id=" + (feed.getPoster().getAvatarGuid()==null? "": feed.getPoster().getAvatarGuid()) + "\" alt=\"User Image\">" +
"                <span class=\"username\">" + subject +" <a href=\"#\">by " +  username + "</a></span>" +
"                <span class=\"description\"> " + this.postTime(feed.getPostDate()) + "</span>" +
"              </div>" +
"              <!-- /.user-block -->" +
"              <div class=\"box-tools\">";
            if(feed.getPoster().getId() == user.getId())
            	html+="<span id='delpost_" + id + "' class='glyphicon glyphicon-trash'></span>";
    
html+="              </div>" +
"              <!-- /.box-tools -->" +
"            </div>" +
"            <!-- /.box-header -->" +
"            <div class=\"box-body\">" + format(msgBody);
            //add images
             if(feed.getVulnerability()!= null && feed.getVulnerability().getSteps()!=null){
            	 html+="<div class='row'>";
            	 for(ExploitStep step : feed.getVulnerability().getSteps()){
            		 
	            		 html+="<div class='col-md-6'>";
	            		 html+=step.getDescription();
	            		 html+="</div>";
            		 
            	 }
            	 html+="</div>";
             }
            
            html+="<button type=\"button\" id=\"like_"+id+"\" class=\"btn btn-default btn-xs\"><i class=\"fa fa-thumbs-o-up\"></i> Like</button>" +
"              <span class=\"pull-right text-muted\"> " +status + "</span>" +
"            </div>" +
"            <!-- /.box-body -->";
            if(feed == null || feed.getComments() == null || feed.getComments().size() ==0)
            	html+="            <div class=\"box-footer box-comments\" style='display:none'>";
            else{
            	html+="            <div class=\"box-footer box-comments\">";
            	Random rand = new Random();
            	//int rid = rand.nextInt(5000);
            	rid = feed.getId().intValue();
            	String verb = "are";
            	String noun = "comments";
            	if(feed.getComments().size()==1){
            		verb = "is";
            		noun = "comment";
            	}
            	html+="<div class='commentsize' id='cs_"+rid+ "'>There " + verb + " " + feed.getComments().size() + " " + noun +". <i class='glyphicon glyphicon-comment'></i></div>";
            	            	html+="<div class='hidecomments' style='display:none' id='cm_"+rid+ "'>";
            	TreeMap<Date,FeedComment> fcMap = new TreeMap<Date,FeedComment>();
            	for(FeedComment fc : feed.getComments()){
            		fcMap.put(fc.getDateOfComment(), fc);
            	}
            	for(FeedComment fc : fcMap.values()){
            		
            		html+="              <div class=\"box-comment\" >";
            		html+="					 <img class=\"img-responsive img-circle img-sm\" src=\"../service/profileImage?id=" + (fc.getCommenter().getAvatarGuid()==null? "": fc.getCommenter().getAvatarGuid()) +"\" alt=\"Alt Text\">";
            		html +="<div class=\"comment-text\">" +
                            "<span class=\"username\">" + fc.getCommenter().getFname() + " " + fc.getCommenter().getLname() + 
                            "<span class=\"text-muted pull-right\">";
            				if(fc.getCommenter().getId() == user.getId())
            					html +="<span id='delcomment_" + fc.getGuid() + "' class='glyphicon glyphicon-trash'></span> " ;
            		html+= this.postTime(fc.getDateOfComment()) + "</span> " +
                            "</span><!-- /.username -->" +this.format(fc.getComment()) + "</div>";
            		html+="					</div>";	
            	}
            	html+="</div>";
            	html+="<script>";
            	html+="$(function(){"
            			+ "$('#cs_" + rid + "').click(function(){"
            					+ "var id=$(this).attr('id').replace('cs_','');"
            					+ "$('#cm_'+id).toggle();"
            					+ "hash=document.location.hash.split('_');"
            					+ "document.location.hash = hash[0] + '_id=' + id;"
            					+ "});"
            			+ "}"
            			+ ");"
            			+ "</script>";
            }
            
            out.println(html);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }
    @Override
    public int doEndTag() throws JspException {
    	try {
	    	String html =  "          " +
	    			"            </div>" +
	    			"            <!-- /.box-footer -->" +
	    			"            <div class=\"box-footer\">" +
	    			"              <form action=\"#\" method=\"post\">" +
	    			"                <img class=\"img-responsive img-circle img-sm\" src=\"../service/profileImage\" alt=\"Alt Text\">" +
	    			"                <!-- .img-push is used to add margin to elements next to floating images -->" +
	    			"                <div class=\"img-push\">" +
	    			"                  <input rid='" +rid+"' id='comment_" + id +"' type=\"text\" class=\"form-control input-sm\" placeholder=\"Press enter to post comment\">" +
	    			"                </div>" +
	    			"              </form>" +
	    			"            </div>" +
	    			"            <!-- /.box-footer -->" +
	    			"          </div>";
	    	JspWriter out = pageContext.getOut();
	    	out.println(html);
    	 } catch (IOException e) {
             e.printStackTrace();
         }
    	
    	//Fuck You Java! Soooo...I learned today that taglibraries cache previous values.. what the fuck! and apparently there
    	// there is nothing online that tells you this or tells you how to turn it the fuck off!
    	// therefore there is this hack below to zero everything out...!@#$!#@$!@#$ mother fucker!
    	this.msgBody="";
    	this.id="";
    	this.feed=null;
    	this.subject="";
    	return EVAL_PAGE;
    	
    
    }
    
    private String postTime(Date postDate){
    	if(postDate == null)
    		return "unknown";
    		
    	
    	SimpleDateFormat sdfTime = new SimpleDateFormat("'Shared Today at' hh:mm a z");
        SimpleDateFormat sdfRecent =  new SimpleDateFormat("'Shared' EEE 'at' hh:mm a z");
        SimpleDateFormat sdfDate = new SimpleDateFormat("'Shared' MM/dd/yyyy");
        SimpleDateFormat test = new SimpleDateFormat("MM/dd/yyyy");
        
        
        Calendar now = Calendar.getInstance();
        long week = now.getTimeInMillis() - 60*60*24*7*1000;
        
        String time = "";
        if(test.format(postDate).equals(test.format(now.getTime()))){
        	time = sdfTime.format(postDate);
        }else if (postDate.getTime() > week){
        	time = sdfRecent.format(postDate);
        }else{
        	time = sdfDate.format(postDate);
        }
        
        return time;
    }



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	private String format(String message){
		String regexURL = "\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		//String regexImg = "\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|](.png|jpg|jpeg)$";
		
		Matcher mUrl = Pattern.compile(regexURL)
			     .matcher(message);
		
		 while (mUrl.find()) {
			 message = message.replace(mUrl.group(), "<a href='" + mUrl.group() + "'>" + mUrl.group()+"</a>");
		}
		 
		 return message;
		
	}
	public Feed getFeed() {
		return feed;
	}
	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	

	
	


	
    

}