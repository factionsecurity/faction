package com.fuse.actions.admin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Integrations;
import com.fuse.utils.Integrate;

import vtrack.pylib.VTArray;
import vtrack.pylib.VTIntegration;
import vtrack.pylib.VTKVPair;
import vtrack.pylib.VTPythonException;

@Deprecated
@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/admin/IntegrationAPI/Integration.jsp")
public class Integration extends FSActionSupport{
	
	private String action="";
	private String code="";
	private String module="";
	private List<String> inputs;
	private List<String> outputs;
	private String console;
	private boolean enabled;
	private List<Integrations> integrations;
	private String defaultCode = "from vtrack.pylib import VTKVPair\n"+
			"from vtrack.pylib import VTArray\n"+
			"from vtrack.pylib import VTIntegration\n\n" +
			"class API(VTIntegration):\n\n"+
			"    def runit(self, inputs):\n"+
			"        array=VTArray()\n\n" +
			"        return array";
	

	@Action(value="Integration", results={
			@Result(name="integrationJson",location="/WEB-INF/jsp/admin/IntegrationAPI/integrationJson.jsp"),
			@Result(name="consoleJson",location="/WEB-INF/jsp/admin/IntegrationAPI/consoleJson.jsp")
		})
	public String execute(){
		if(!this.isAcadmin())
			return LOGIN;
		//Session session = HibHelper.getSessionFactory().openSession();
		String returnType=SUCCESS;
		
		
		//Default execution
		if(action.equals("")){
			integrations = (List<Integrations>) em.createQuery("from Integrations").getResultList();
			
		}else if(action.equals("get")){
			Integrations intg = (Integrations) em.createQuery("from Integrations where name = :name")
					.setParameter("name", this.module)
					.getResultList().stream().findFirst().orElse(null);
			if(intg == null){
				intg = new Integrations();
				intg.setCode(this.defaultCode);
				intg.setName(this.module);
				intg.setEnabled(this.enabled);
				intg.setArguments(this.setArguments(this.module));
				intg.setReturnValues(this.setOutputs(this.module));
				em.persist(intg);
				/*session.getTransaction().begin();
				session.save(intg);
				session.getTransaction().commit();*/
			}
			
			try {
				this.code = intg.getCode();
				if(code.equals("")){
					this.code = this.defaultCode;
				}
				this.code = URLEncoder.encode(this.code, "UTF-8").replaceAll("\\+", "%20");
				this.inputs = intg.getArguments();
				this.outputs = intg.getReturnValues();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			returnType="integrationJson";		
		}else if(action.equals("save")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Integrations intg = (Integrations) em.createQuery("from Integrations where name = :name")
					.setParameter("name", this.module)
					.getResultList().stream().findFirst().orElse(null);
			intg.setCode(this.code);
			intg.setEnabled(this.enabled);
			em.persist(intg);
			HibHelper.getInstance().commit();
			returnType=this.SUCCESSJSON;
			
		}else if(action.equals("checked")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Integrations intg = (Integrations) em.createQuery("from Integrations where name = :name")
					.setParameter("name", this.module)
					.getResultList().stream().findFirst().orElse(null);
			intg.setEnabled(this.enabled);
			em.persist(intg);
			HibHelper.getInstance().commit();
			returnType = this.SUCCESSJSON;
		}else if(action.equals("run")){
			Integrations intg = (Integrations) em.createQuery("from Integrations where name = :name")
					.setParameter("name", this.module)
					.getResultList().stream().findFirst().orElse(null);

			VTArray json=new VTArray();
			StringWriter writer = new StringWriter();
			try{
				Object vti = Integrate.create(intg, writer);
				VTKVPair testData = new VTKVPair();
				Random rand = new Random();
				for(String arg : intg.getArguments()){
					if(arg.contains("[")){// nested args
						String [] splitArgs = arg.split("\\[");
						String argKey = splitArgs[0]; 
						String [] subArgs = splitArgs[1].replace("]", "").split(",");
						VTKVPair kvp = new VTKVPair();
						for(String s : subArgs){
							kvp.put(s.trim(), "TestData_" + rand.nextInt(100));
						}
						VTArray tmpAry =  new VTArray();
						tmpAry.add(kvp);
						testData.put(argKey.trim(), tmpAry);
					}else{
						testData.put(arg, "TestData_" + rand.nextInt(100));
					}
				}
				json = ((VTIntegration)vti).runit(testData);
				
			}catch(VTPythonException ex){
				json = ex.getArray();
			}catch(Exception ex){
				VTPythonException e = new VTPythonException(ex);
				json=e.getArray();
			}
			
			
			console = "";
			if(json.size()==1 && ((JSONObject)json.get(0)).containsKey("Error")){
				console += "#############################\r\n";
				console += "### Errors\r\n";
				console += ((JSONObject)json.get(0)).get("Error");
				try {
					console = URLEncoder.encode(console, "UTF-8").replace("+", "%20");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				returnType = "consoleJson";
				
			}else{
				int i=0;
				console += "#############################\r\n";
				console += "###Variable Output:\r\n";
				for(int j=0; j < json.size(); j++){
					JSONObject jo = (JSONObject)json.get(j);
					for(String key : intg.getReturnValues()){
						console += i +". Output Value (" + key + " ): " + jo.get(key) + "\r\n";
					}
					i++;
				}
				console += "#############################\r\n";
				console += "###Console Output:\r\n";
				console += writer.toString() + "\r\n";
				console += "#############################\r\n";
				console += "Test Complete!";
				returnType = "consoleJson";
			}
					
			
		}
		//session.close();
		return returnType;
	}
	
	private List<String>setArguments(String Module){
		String[] args = {};
		if(Module.equals("mod1")){  // App Inventory Module
			args = new String[]{"appid", "appname"};
		}else if(Module.equals("mod2")){ //Assessment Completed Module
			args = new String[] {"appid", "appname", "distro", "assessor", "remediation", "engagement", "start", "end", "completed", "vulns[ vulnId, vulnName, severity, desc ]"};
		}else if(Module.equals("mod3")){ //Verification Completed Module
			args = new String [] {"assessor", "isPassed", "remediation", "start", "end", "completed", "vulnName", "vulnId", "vulnSeverity", "isClosedinDev", "isClosedinProd", "tracking"};
		}
		return Arrays.asList(args);
	}
	
	private List<String>setOutputs(String Module){
		String[] args = {};
		if(Module.equals("mod1")){  // App Inventory Module
			args = new String[]{"appid", "appname", "distro"};
		}else if(Module.equals("mod2")){ //Assessment Completed Module
			args = new String[] {"vulnId" , "tracking"};
		}else if(Module.equals("mod3")){ //Verification Completed Module
			args = new String [] {};
		}
		return Arrays.asList(args);
		
	}
	
	public String getActiveAPI(){
		return "active";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<String> getInputs() {
		return inputs;
	}

	public List<String> getOutputs() {
		return outputs;
	}

	public String getConsole() {
		return console;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<Integrations> getIntegrations() {
		return integrations;
	}
	
	


	

}
