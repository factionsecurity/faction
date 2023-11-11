package vtrack.pylib;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class VTReturn {
	
	private JSONArray returnArray;
	private JSONObject returnObject;
	
	public VTReturn(VTKVPair obj){
		this.returnObject = (JSONObject) obj;
	}
	public VTReturn(VTArray obj){
		this.returnArray = (JSONArray) obj;
	}

}
