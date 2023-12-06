package com.fuse.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.CookiesAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.utils.CSRF;
import com.fuse.utils.FSUtils;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.annotations.After;
import com.opensymphony.xwork2.interceptor.annotations.Before;

@InterceptorRef(value="defaultSecurityStack")
@Results( value = {
		@Result(name="login", type="redirectAction", location="../login"),
		@Result(name="successJson", location="/WEB-INF/jsp/successJson.jsp"),
		@Result(name="errorJson", location="/WEB-INF/jsp/errorJson.jsp"),
		@Result(name="invalid.token", location="/WEB-INF/jsp/errorJson.jsp"),
		@Result( name="_json",type = "stream"
				, params = {
						"contentType", "application/json", 
				        "inputName", "_stream"}),
		@Result( name="_raw",type = "stream"
				, params = {
						"contentType", "application/octet-stream", 
				        "inputName", "_stream"})
})
public class FSActionSupport extends ActionSupport implements SessionAware, ServletRequestAware, ServletResponseAware{
	

	protected SessionMap<String,Object> JSESSION;  
	protected Map<String,String> COOKIES;
	public HttpServletRequest request;
	public HttpServletResponse response;
	public String LOGIN = "login";
	public String SUCCESSJSON = "successJson";
	public String ERRORJSON = "errorJson";
	public String JSON = "_json";
	public String RAW = "_raw";
	protected InputStream _stream;
	public  EntityManager em;
	private String MENUOPTION="";
	protected boolean prEnabled;
	protected boolean feedEnabled;
	protected boolean retestsEnabled;
	protected String tier="";
	protected String _title1 = "Fuse";
	protected String _title2 = "FACTION";
	protected String _token ="";
	public String _message;
	public boolean userLimitReached=false;
	public boolean expireDateApproaching=false;
	public boolean licenseExpired=false;
	protected String version="";
	
	
	private boolean isIndex() {
		String resultPath = request.getRequestURI().toString().replace(request.getContextPath(), "");
		if(resultPath.equals("/"))
			return true;
		else return false;
	}
	
	@Before
	public String openConnection(){
		em = HibHelper.getInstance().getEMF().createEntityManager();
		return null;
	}
	
	@After
	public String closeConnection(){
		em.close();
		return null;
	}
	
	@Override
	public void setSession(Map<String, Object> arg0) {
		JSESSION = (SessionMap)arg0;
		
		if(JSESSION!= null){
			prEnabled = (Boolean) (JSESSION.get("prEnabled") == null ? false : JSESSION.get("prEnabled"));
			feedEnabled = (Boolean) (JSESSION.get("feedEnabled") == null ? false : JSESSION.get("feedEnabled"));
			retestsEnabled = (Boolean) (JSESSION.get("retestEnabled") == null ? false : JSESSION.get("retestsEnabled"));
			tier = (String) (JSESSION.get("tier") == null ? "" : JSESSION.get("tier"));
			_title1 = (String)JSESSION.get("title1");
			_title2 = (String)JSESSION.get("title2");
			version = (String)JSESSION.get("version");
			if(version == null) {
				try {
					version = FSUtils.getVersion(ServletActionContext.getServletContext());
					version = version.replace("-SNAPSHOT", "");
					JSESSION.put("version", version);
				}catch(Exception ex) {
					ex.printStackTrace();
					version = "-";
					JSESSION.put("version", version);
				}
			}
			
		}
		
	}

	/*@Override
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
		JSESSION = request.getSession();
		em = HibHelper.getEM();
		
	}*/
	private boolean getRole(String role){
		return (boolean) (ActionContext.getContext().get(role) == null ? false :ActionContext.getContext().get(role)) ;
	}
	
	public boolean isAcassessor(){
		boolean result = getRole("isAssessor") ;
		return result;
	}
	public boolean isAcmanager(){
		return getRole("isManager");
	}
	public boolean isAcengagement(){
		return getRole("isEngagement");
	}
	public boolean isAcremediation(){
		return getRole("isRemediation");
	}
	public boolean isAcadmin(){
		return getRole("isAdmin");
	}
	public User getSessionUser(){
		return (User)ActionContext.getContext().get("user");
	}
	public boolean isAll(){
		return this.isAcassessor() || this.isAcadmin() || this.isAcengagement() || this.isAcmanager() || this.isAcremediation();
	}
	public boolean getPrEnabled(){
		return prEnabled;
	}
	public boolean getFeedEnabled(){
		return feedEnabled;
	}
	public boolean getRetestsEnabled(){
		return retestsEnabled;
	}
	
	public String get_title1() {
		return _title1;
	}

	public String get_title2() {
		return _title2;
	}

	public String getMENUOPTION() {
		return MENUOPTION;
	}

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		this.request=arg0;
		Cookie [] cookies = request.getCookies();
		if(cookies == null)
			return;
		for(Cookie c : cookies){
			if(c.getName().equals("faction_menu") && c.getValue().equals("hide")){
				this.MENUOPTION = "sidebar-collapse";
			}
		}
		
	}
	@Override
	public void setServletResponse(HttpServletResponse arg0) {
		this.response = arg0;
	}
	
	public String jsonOutput(String json) {
		try {
			this._stream = new ByteArrayInputStream( json.getBytes("UTF-8") );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return this.JSON;
	}
	
	public String rawOutput(byte [] json) {

		this._stream = new ByteArrayInputStream( json );
		return this.RAW;
	}
	//THis is for unit testing
	public String printStream() {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(this._stream));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}
	
	//CSRF Tests
	public void set_token(String token) {
		this._token = token;
	}
	/*public String get_token() {
		return CSRF.getToken(ServletActionContext.getRequest().getSession());
	}*/
	public String get_token() {
		if(this._token == null || this._token.equals("")) {
			return CSRF.getToken(ServletActionContext.getRequest().getSession());
		}else {
			return this._token;
		}
	}
	
	public boolean testToken() {
		if(CSRF.checkToken(ServletActionContext.getRequest().getSession(), this._token))
			return true;
		else {
			this._message = "Failed CSRF Token";
			return false;
		}
	}

	public String getTier(){
		return tier;
	}
	public boolean testToken(Boolean renew) {
		if(CSRF.checkToken(ServletActionContext.getRequest().getSession(), this._token, renew))
			return true;
		else {
			this._message = "Failed CSRF Token";
			return false;
		}
	}

	public String get_message() {
		return _message;
	}
	
	public InputStream get_stream() {
		return _stream;
	}
	
	public Object getSession(String attr) {
		return ServletActionContext.getRequest().getSession().getAttribute(attr);
	}
	public void setSession(String attr, Object value) {
		ServletActionContext.getRequest().getSession().setAttribute(attr, value);
	}
	
	public boolean isNullStirng(String var) {
		if(var == null)
			return true;
		if(var.trim().equals(""))
			return true;
		
		return false;
	}
	public String getVersion() {
		return this.version; 
	}
}
