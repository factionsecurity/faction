package com.fuse.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.ogm.OgmSession;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.python.bouncycastle.asn1.isismtt.x509.Restriction;

import com.fuse.dao.Assessment;
import com.fuse.dao.Campaign;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Integrations;
import com.fuse.dao.User;
import com.faction.extender.InventoryResult;
import com.fuse.extenderapi.Extensions;
import com.fuse.utils.FSUtils;
import com.fuse.utils.Integrate;

import vtrack.pylib.VTArray;
import vtrack.pylib.VTIntegration;
import vtrack.pylib.VTKVPair;
import vtrack.pylib.VTPythonException;


/**
 * Servlet implementation class applicationInventory
 */
public class applicationInventory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public applicationInventory() {
        super();
       
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User u = (User)request.getSession().getAttribute("user");
		if(u== null)
			return;
		
		
		String appname = request.getParameter("appname") == null ? "" : request.getParameter("appname");
		String appid = request.getParameter("appid") == null ? "" : request.getParameter("appid");
		String campname = request.getParameter("campname") == null ? "" : request.getParameter("campname");
		
		
		//Session session = HibHelper.getSessionFactory().openSession();
		//HibHelper hh = new HibHelper();
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		Integrations inv = (Integrations)em.createQuery("from Integrations where name = :name").setParameter("name", "mod1").getResultList().stream().findFirst().orElse(null);
		Extensions appInv = new Extensions(Extensions.EventType.INVENTORY);
		if(inv != null && inv.isEnabled()){
			VTArray json=new VTArray();
			VTKVPair kv = new VTKVPair();
			kv.put("appid", appid);
			kv.put("appname", appname);
			try{
				Object vti = Integrate.create(inv);
				json = ((VTIntegration)vti).runit(kv);
			}catch(VTPythonException ex){
				json = ex.getArray();
			}catch(Exception ex){
				VTPythonException e = new VTPythonException(ex);
				json=e.getArray();
			}
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			out.print(json.toJSONString());
		}else if ( appInv.checkIfExtended()) {
			InventoryResult[] results = (InventoryResult[]) appInv
					.execute(new Class[]{String.class,  String.class}, appid,appname);
			JSONArray array = new JSONArray();
			for(InventoryResult result : results){
				JSONObject json = new JSONObject();
				json.put("appid", result.getApplicationId());
				json.put("appname", result.getApplicationName());
				json.put("distro", result.getDistrobutionList());
				JSONArray fields = new JSONArray();
				if(result.getCustomFields()!= null){
					for(String  key : result.getCustomFields().keySet()){
						CustomType ct = (CustomType)em.createQuery("from CustomType where variable = :value")
								.setParameter("value", key).getResultList().stream().findFirst().orElse(null);
					
						JSONObject field = new JSONObject();
						field.put("fid", ct.getId());
						field.put("value", result.getCustomFields().get(key));
						fields.add(field);
					}
					json.put("fields", fields);
				}
				array.add(json);
			}
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			out.print(array.toJSONString());
		}else{
			List<Assessment> as = null;
			if(!appid.equals("") && !appname.equals("")){
				String query = "{$or : [{_appId : { $regex : '.*"+FSUtils.sanitizeMongo(appid)+".*', $options : 'i'}}, { 'name' : { $regex : '.*"+FSUtils.sanitizeMongo(appname)+".*', $options : 'i'}}]}";
				//String query = "{ 'name' : { $regex : '.*"+FSUtils.sanitizeMongo(appname) + ".*', $options : 'i'}, "
				//		+ "$where: '/^"+FSUtils.sanitizeMongo(appid)+".*/.test(this.appId)'}";
				as = FSUtils.sortUniqueAssessment(em.createNativeQuery(query, Assessment.class).getResultList());
				

			}else if(!appid.equals("")){
				//String query = "{ $where: '/^"+FSUtils.sanitizeMongo(appid)+".*/.test(this.appId)'}";
				String query = "{_appId : { $regex : '.*"+FSUtils.sanitizeMongo(appid)+".*', $options : 'i'}}";
				as = FSUtils.sortUniqueAssessment(em.createNativeQuery(query, Assessment.class).getResultList());
				
			}else if(!appname.equals("")){
				String query = "{ 'name' : { $regex : '.*"+FSUtils.sanitizeMongo(appname) + ".*', $options : 'i'}}";
				as = FSUtils.sortUniqueAssessment(em.createNativeQuery(query, Assessment.class).getResultList());
				

			}else if(!campname.equals("")){
				String query = "{ 'name' : { $regex : '.*"+FSUtils.sanitizeMongo(campname) + ".*', $options : 'i'}}";
				List<Campaign> camp = em.createNativeQuery(query, Campaign.class).getResultList();
				JSONArray array = new JSONArray();
				for(Campaign c : camp){
					JSONObject json = new JSONObject();
					json.put("appid", "");
					json.put("appname", c.getName());
					json.put("campid", c.getId());
					json.put("campName", c.getName());
					
					array.add(json);
					
				}
				PrintWriter out = response.getWriter();
				response.setContentType("application/json");
				out.print(array.toJSONString());
				em.close();
				return;
			}
			JSONArray array = new JSONArray();
			for(Assessment a : as){
			JSONObject json = new JSONObject();
				json.put("appid", a.getAppId());
				json.put("appname", a.getName());
				json.put("type", a.getType().getId());
				json.put("distro", a.getDistributionList());
				json.put("remediationId", a.getRemediation().getId());
				json.put("engId", a.getEngagement().getId());
				json.put("remediationName", a.getRemediation().getFname() + " " + a.getRemediation().getLname());
				json.put("campName", a.getCampaign().getName());
				json.put("cid", a.getCampaign().getId());
				JSONArray fields = new JSONArray();
				if(a.getCustomFields()!= null){
					for(CustomField cf : a.getCustomFields()){
						JSONObject field = new JSONObject();
						field.put("fid", cf.getType().getId());
						field.put("value", cf.getValue());
						fields.add(field);
					}
					json.put("fields", fields);
				}
				array.add(json);
			}
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			out.print(array.toJSONString());
			em.close();
		}
		
	}

}
