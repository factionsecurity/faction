package com.fuse.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.APIKeys;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.ProfileImage;
import com.fuse.dao.User;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.AccessControl;
import com.fuse.utils.FSUtils;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/profile/Profile.jsp")
public class Profile extends FSActionSupport{
	
	
	private String password="";
	private String confirm="";
	private String fname="";
	private String lname="";
	private String email="";
	private String action="";
	private User user;
	private String current="";
	private String message;
	private File profileImage;
	private String contentType;
    private String filename;
    private String apiKey;

    
    
    @Action(value="DeleteProfileImage", results={
			@Result(name="uploadedJson",location="/WEB-INF/jsp/profile/uploadedJson.jsp")
		})
    public String deleteProfileImage() {
    	user = this.getSessionUser();
		
		if(user == null)
			return LOGIN;
		
		ProfileImage pi = (ProfileImage) em.createQuery("from ProfileImage where guid = :guid")
				.setParameter("guid", user.getAvatarGuid())
				.getResultList().stream().findFirst().orElse(null);
		if(pi ==  null)
			return "uploadJson";
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.remove(pi);
		User u = em.find(User.class, user.getId());
		u.setAvatarGuid(null);
		user.setAvatarGuid(null);
		em.persist(u);
		AuditLog.audit(this, "User deleted the profile image", AuditLog.Login,false);
		HibHelper.getInstance().commit();
		
		return "uploadedJson";
		
    }
  
    @Action(value="Profile", results={
			@Result(name="uploadedJson",location="/WEB-INF/jsp/profile/uploadedJson.jsp")
		})
	public String execute(){
		
		
		user = this.getSessionUser();
		
		if(user == null)
			return LOGIN;
		
		APIKeys key = (APIKeys) em.createQuery("from APIKeys where userid = :id")
			.setParameter("id", user.getId())
			.getResultList().stream().findFirst()
			.orElse(null);
		if(key!=null)
			apiKey = key.getKey();
		
		if(this.action.equals("imgUpload")){
			try {
				if(this.contentType.contains("image")){
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					RandomAccessFile raf = new RandomAccessFile(profileImage,"r");
					byte [] img = new byte[(int)raf.length()];
					raf.readFully(img);
					String b64String = Base64.getEncoder().encodeToString(img);
					//Session s = HibHelper.getSessionFactory().openSession();
					ProfileImage pi=null;
					if(user.getAvatarGuid() == null || user.getAvatarGuid().equals(""))
						pi = new ProfileImage();
					else
						pi = (ProfileImage) em.createQuery("from ProfileImage where guid = :guid")
												.setParameter("guid", user.getAvatarGuid())
												.getResultList().stream().findFirst().orElse(null);
					pi.setBase64Image(b64String);
					pi.setContenType(this.contentType);
					pi.setUserid(user.getId());
					
					
					//s.getTransaction().begin();
					em.persist(pi);
					/*if(user.getAvatarGuid() == null || user.getAvatarGuid().equals(""))
						em.persist(pi);//s.save(pi);
					else
						em.persist(pi);//s.update(pi);*/
					user.setAvatarGuid(pi.getGuid());
					em.merge(user);
					AuditLog.audit(this, "User updated the profile with an image", AuditLog.Login, false);
					HibHelper.getInstance().commit();
					
					String HTML = "Hello " + user.getFname() + " " +user.getLname() + ", <br>"
							+ "Your profile image was updated.<br>";
					EmailThread emailThread = new EmailThread(user.getEmail(), "Profile Updated.", HTML);
					TaskQueueExecutor.getInstance().execute(emailThread);
					
					return "uploadedJson";
				}else{
					this.message = "Invalid Image";
					return "errorJson";
				}
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.message = "File Not Found";
				return "errorJson";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.message = "Invalid Image";
				return "errorJson";
			}
			
		}else if(this.action.equals("update")){
			//Check the current password before making changes.
			if(AccessControl.HashPass(this.user.getUsername(), this.current).equals(this.user.getPasshash())){
				if(this.email == null || this.email.trim().equals("")) {
					this._message="Email is missing.";
					return this.ERRORJSON;
				}
				if(!FSUtils.checkEmail(this.email.trim())) {
					this._message="Invalid Email format";
					return this.ERRORJSON;
				}
				if(this.fname == null || this.fname.trim().equals("")) {
					this._message="First name is missing.";
					return this.ERRORJSON;
				}
				if(this.lname == null || this.lname.trim().equals("")) {
					this._message="Last name is missing.";
					return this.ERRORJSON;
				}
				
				
				
				
				List<String> emails = new ArrayList<String>();
				emails.add(user.getEmail());
				emails.add(this.email);
				
				this.user.setEmail(this.email.trim());
				this.user.setFname(this.fname.trim());
				this.user.setLname(this.lname.trim());
				if(!this.confirm.equals("")){ // we are updating the password too.
					message = AccessControl.checkPassword(password, confirm);
					if(message.equals("")){
						this.user.setPasshash(AccessControl.HashPass(user.getUsername(), password));
					}else{
						return "errorJson";
					}	
				}
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.merge(this.user);
				AuditLog.audit(this, "User update their profile info", AuditLog.Login, false);
				HibHelper.getInstance().commit();
				
				String HTML = "Hello " + user.getFname() + " " + user.getLname() + ", <br>"
						+ "Your profile information has been updated.<br>";
				EmailThread emailThread = new EmailThread(emails, "Profile Updated.", HTML);
				TaskQueueExecutor.getInstance().execute(emailThread);
				
				this.JSESSION.put("user", user);
				return SUCCESSJSON;
						
				
			}else{
				message="Incorrect Password.";
				return this.ERRORJSON;
			}
				
		}
		return SUCCESS;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public User getUser() {
		return user;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	public String getMessage() {
		return message;
	}

	public File getProfileImage() {
		return profileImage;
	}
	
	public void setProfileImage(File profileImage) {
		this.profileImage = profileImage;
	}

	public void setProfileImageContentType(String contentType) {
         this.contentType = contentType;
      }
 
    public void setProfileImageFileName(String filename) {
         this.filename = filename;
      }

	public String getApiKey() {
		return apiKey;
	}
	
	
	

}
