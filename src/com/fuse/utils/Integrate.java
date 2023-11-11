package com.fuse.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.fuse.dao.Integrations;

import org.json.simple.JSONArray;
import vtrack.pylib.VTIntegration;
import vtrack.pylib.VTKVPair;
import vtrack.pylib.VTPythonException;
import vtrack.pylib.VTArray;

public class Integrate {
	
	private static Integrate instance = null;
	
	
    
    public static Object create(Integrations Obj) throws VTPythonException{
    	PythonInterpreter interpreter = new PythonInterpreter();
		String pyCode = Obj.getCode();
		try{
			interpreter.exec(pyCode);
			StringWriter writer = new StringWriter();
			interpreter.setOut(writer);
			PyObject obj = interpreter.get("API");
			PyObject obj2 =  obj.__call__();
			return obj2.__tojava__(VTIntegration.class);
		}catch(Exception ex){
			ex.printStackTrace();
			throw new VTPythonException(ex);
			
		}

		

	}
    
    public static Object create(Integrations Obj, StringWriter writer) throws VTPythonException{
    	PythonInterpreter interpreter = new PythonInterpreter();
		String pyCode = Obj.getCode();
		try{
			interpreter.exec(pyCode);
			interpreter.setOut(writer);
			PyObject obj = interpreter.get("API");
			PyObject obj2 =  obj.__call__();
			return obj2.__tojava__(VTIntegration.class);
		}catch(Exception ex){
			ex.printStackTrace();
			throw new VTPythonException(ex);
			
		}

		

	}
    
    public static boolean isError(Object obj){
    	String cName = obj.getClass().getName();
    	if(cName.equals("vtrack.pylib.VTArray")){
    		return true;
    	}else{
    		return false;
    	}
    }
	
	
	/*public static HashMap<String,String> exec(Integrations Obj, HashMap<String,String> args){
		HashMap<String,String>outputs = new HashMap<String,String>();
		try{
			PythonInterpreter interpreter = new PythonInterpreter();
			String pyCode = Obj.getCode();
			if(args != null){
				for(String key : args.keySet())	{
					if(args.get(key) == null)
						args.put(key,"");
					interpreter.set(key, args.get(key));	
				}
			}
	        interpreter.exec(pyCode);
	        VTArray out = interpreter.get("output", VTArray.class);
	       
	        for(String key : Obj.getReturnValues()){
	        	outputs.put(key, interpreter.get(key).asString());
	        }
		}catch(Exception Ex){
			StringWriter str = new StringWriter();
			PrintWriter o = new PrintWriter(str);
			Ex.printStackTrace(o);
			outputs.put("Errors", str.toString());
		}
        
      
		return outputs;
		
	}
	
	public static JSONObject execJson(Integrations Obj, HashMap<String,String> args){
		HashMap<String,String> out = exec(Obj,args);
		JSONObject json = new JSONObject();
		for(String key : out.keySet()){
			json.put(key,out.get(key));
		}
		json.put("Errors", out.get("Errors"));
		return json;
		
		
	}
	
	/*public static JSONArray exec2(Integrations Obj, HashMap<String,String> args){
		PythonInterpreter interpreter = new PythonInterpreter();
		JSONArray array = new JSONArray();
		String pyCode = Obj.getCode();
		for(String key : args.keySet())	{
			if(args.get(key) == null)
				args.put(key,"");
			interpreter.set(key, args.get(key));	
		}
        interpreter.exec(pyCode);
        
		
	}*/
	

}
