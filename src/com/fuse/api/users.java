package com.fuse.api;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fuse.api.util.Support;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Permissions;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.AccessControl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/users")
@Path("/users")
public class users {

	@POST
	@ApiOperation(value = "Add a user to Faction.", notes = "This call will give you the ability to create a user in Faction. If the user already exists then"
			+ " an error will be returned. You can choose to send the user an email confirmation link or create "
			+ " an account with an empty password. The later is mostly used with SSO type integrations.")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Error Occured. User most likely already exists."),
			@ApiResponse(code = 200, message = "Request Successfull") })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/addUser")
	public Response createUser(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "Username", required = true) @FormParam("username") String username,
			@ApiParam(value = "Email Address", required = true) @FormParam("email") String email,
			@ApiParam(value = "First Name", required = true) @FormParam("fname") String fname,
			@ApiParam(value = "Last Name", required = true) @FormParam("lname") String lname,
			@ApiParam(value = "Team Name", required = true) @FormParam("team") String team,
			@ApiParam(value = "Authentication Method", required = true) @FormParam("authMethod") String authMethod,
			@ApiParam(value = "Send Verification Email", required = false) @FormParam("verify") Boolean verify,
			@ApiParam(value = "Assessor Role", required = false) @FormParam("isAssessor") Boolean isAssessor,
			@ApiParam(value = "Admin Role", required = false) @FormParam("isAdmin") Boolean isAdmin,
			@ApiParam(value = "Remediation Role", required = false) @FormParam("isRemediation") Boolean isRemediation,
			@ApiParam(value = "Engagement/Scheduler Role", required = false) @FormParam("isEngage") Boolean isEngage,
			@ApiParam(value = "Manager Role", required = false) @FormParam("isManager") Boolean isManager,
			@Context HttpServletRequest req) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try {

			User u = Support.getUser(em, apiKey);

			if (u != null && u.getPermissions().isAdmin() && !Support.getTier().equals("consultant")) {
				List<User> users = em.createQuery("from User where username = :username")
						.setParameter("username", username).getResultList();
				if (users.size() > 0)
					return Response.status(400).entity(String.format(Support.ERROR, "User Already Exists")).build();

				if (username == null || username.isEmpty())
					return Response.status(400).entity(String.format(Support.ERROR, "User Name Invalid")).build();

				User newUser = new User();
				newUser.setUsername(username);
				newUser.setEmail(email);
				newUser.setFname(fname);
				newUser.setLname(lname);
				newUser.setAuthMethod(authMethod);
				newUser.setInActive(false);
				Teams t = (Teams) em.createQuery("from Teams where TeamName = :name").setParameter("name", team)
						.getResultList().stream().findFirst().orElse(null);
				if (t == null) {
					return Response.status(400).entity(String.format(Support.ERROR, "Team Name Does not exist"))
							.build();
				}
				newUser.setTeam(t);
				if (verify != null && verify == true) {
					String pass = UUID.randomUUID().toString();
					newUser.setPasshash(AccessControl.HashPass("", pass));
					// Setting username to an empty string prevents logins with GUID.
					// You must register first and create a password to login.
					String message = "Hello " + fname + " " + lname + "<br><br>";
					message += "Click the link below to update your password:<br><br>";
					String url = req.getRequestURL().toString();
					url = url.replace(req.getRequestURI(), "");
					url = url + req.getContextPath() + "/portal/Register?uid=" + pass;
					message += "<a href='" + url + "'>Click here to Register</a><br>";
					EmailThread emailThread = new EmailThread(email, "New Account Created", message);
					TaskQueueExecutor.getInstance().execute(emailThread);

				}
				Permissions p = new Permissions();
				if (isAdmin != null && isAdmin == true)
					p.setAdmin(true);
				if (isEngage != null && isEngage == true)
					p.setEngagement(true);
				if (isRemediation != null && isRemediation == true)
					p.setRemediation(true);
				if (isAssessor != null && isAssessor == true)
					p.setAssessor(true);
				if (isManager != null && isManager == true)
					p.setManager(true);

				newUser.setPermissions(p);
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.persist(newUser);
				HibHelper.getInstance().commit();
				return Response.status(200).entity(Support.SUCCESS).build();

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
	}

	@POST
	@ApiOperation(value = "Disable A Faction User.", notes = "This operation will set any user into the inactive state. They will not be"
			+ " able to log back in to the system untill the account is reset. ")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Error Occured. User most likely already exists."),
			@ApiResponse(code = 200, message = "Request Successfull") })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/disableUser")
	public Response disableUser(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "UserName to deactivate", required = true) @FormParam("username") String username,
			@Context HttpServletRequest req) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try {
			User u = Support.getUser(em, apiKey);

			if (u != null && (u.getPermissions().isAdmin()) && !Support.getTier().equals("consultant")) {
				List<User> users = em.createQuery("from User where username = :username")
						.setParameter("username", username).getResultList();
				if (users.size() == 0) {
					User deactiveUser = users.get(0);
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					deactiveUser.setInActive(true);
					em.persist(deactiveUser);
					HibHelper.getInstance().commit();
					return Response.status(200).entity(Support.SUCCESS).build();
				} else {
					return Response.status(401).entity(
							String.format(Support.ERROR, "Can't have more than one user with the same username."))
							.build();
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
	}

	@POST
	@ApiOperation(value = "Unlock A Faction User.", notes = "This operation will set any user into back into the acitive state.")
	@ApiResponses(value = { @ApiResponse(code = 401, message = "Not Authorized"),
			@ApiResponse(code = 400, message = "Error Occured. User most likely already exists."),
			@ApiResponse(code = 200, message = "Request Successfull") })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/unlockUser")
	public Response unlockUser(
			@ApiParam(value = "Authentication Header", required = true) @HeaderParam("FACTION-API-KEY") String apiKey,
			@ApiParam(value = "UserName to activate", required = true) @FormParam("username") String username,
			@ApiParam(value = "Reset Use it or loose it", required = false) @FormParam("uioli") Boolean uioli,
			@Context HttpServletRequest req) {

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		try {
			User u = Support.getUser(em, apiKey);

			if (u != null && (u.getPermissions().isAdmin()) && !Support.getTier().equals("consultant")) {
				List<User> users = em.createQuery("from User where username = :username")
						.setParameter("username", username).getResultList();
				if (users.size() == 0) {
					User deactiveUser = users.get(0);
					HibHelper.getInstance().preJoin();
					em.joinTransaction();
					deactiveUser.setInActive(false);
					if (uioli != null && uioli == true)
						deactiveUser.setLastLogin(new Date());
					em.persist(deactiveUser);
					HibHelper.getInstance().commit();
					return Response.status(200).entity(Support.SUCCESS).build();
				} else {
					return Response.status(401).entity(
							String.format(Support.ERROR, "Can't have more than one user with the same username."))
							.build();
				}

			} else {
				return Response.status(401).entity(String.format(Support.ERROR, "Not Authorized")).build();
			}
		} finally {
			em.close();
		}
	}
}
