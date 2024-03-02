package com.fuse.dao;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fuse.utils.FSUtils;

@Entity
public class AppStore {
	
	@Id
	@GeneratedValue
	private Long id;
	private String uuid;
	private Integer order;
	private String name;
	private String author;
	private String url;
	private String description;
	private String version;
	private Boolean approved;
	private Boolean enabled = false;
	private String base64Logo;
	private Boolean assessmentEnabled = false;
	private Integer assessmentOrder;
	private Boolean verificationEnabled = false;
	private Integer verificationOrder;
	private Boolean vulnerabilityEnabled = false;
	private Integer vulnerabilityOrder;
	private Boolean inventoryEnabled = false;
	private Boolean reportEnabled = false;
	private Integer inventoryOrder;
	private String base64JarFile;
	private String hash;
	private String configs;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getApproved() {
		return approved;
	}
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public String getBase64Logo() {
		return base64Logo;
	}
	public void setBase64Logo(String base64Logo) {
		this.base64Logo = base64Logo;
	}
	public Boolean getAssessmentEnabled() {
		return assessmentEnabled;
	}
	public void setAssessmentEnabled(Boolean assessmentEnabled) {
		this.assessmentEnabled = assessmentEnabled;
	}
	public Boolean getReportEnabled() {
		return reportEnabled;
	}
	public void setReportEnabled(Boolean reportEnabled) {
		this.reportEnabled = reportEnabled;
	}
	public Boolean getVerificationEnabled() {
		return verificationEnabled;
	}
	public void setVerificationEnabled(Boolean verificationEnabled) {
		this.verificationEnabled = verificationEnabled;
	}
	public Boolean getVulnerabilityEnabled() {
		return vulnerabilityEnabled;
	}
	public void setVulnerabilityEnabled(Boolean vulnerabilityEnabled) {
		this.vulnerabilityEnabled = vulnerabilityEnabled;
	}
	public Boolean getInventoryEnabled() {
		return inventoryEnabled;
	}
	public void setInventoryEnabled(Boolean inventoryEnabled) {
		this.inventoryEnabled = inventoryEnabled;
	}
	public String getBase64JarFile() {
		return base64JarFile;
	}
	public void setBase64JarFile(String base64JarFile) {
		this.base64JarFile = base64JarFile;
		this.hash = FSUtils.md5hash(base64JarFile);
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public Integer getAssessmentOrder() {
		return assessmentOrder == null? 0 : assessmentOrder;
	}
	public void setAssessmentOrder(Integer assessmentOrder) {
		this.assessmentOrder = assessmentOrder;
	}
	public Integer getVerificationOrder() {
		return verificationOrder == null ? 0 : verificationOrder;
	}
	public void setVerificationOrder(Integer verificationOrder) {
		this.verificationOrder = verificationOrder;
	}
	public Integer getVulnerabilityOrder() {
		return vulnerabilityOrder == null ? 0 : vulnerabilityOrder;
	}
	public void setVulnerabilityOrder(Integer vulnerabilityOrder) {
		this.vulnerabilityOrder = vulnerabilityOrder;
	}
	public Integer getInventoryOrder() {
		return inventoryOrder == null ? 0 : inventoryOrder;
	}
	public void setInventoryOrder(Integer inventoryOrder) {
		this.inventoryOrder = inventoryOrder;
	}
	public void setConfig(String configs) {
		this.configs=FSUtils.encryptPassword(configs);
	}
	public String getConfig(){
		return FSUtils.decryptPassword(configs);
	}
	@Transient
	public HashMap<String,String> getHashMapConfig(){
		try {
			if(this.getConfig() != null && !this.getConfig().trim().equals("")) {
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(this.getConfig());
				HashMap<String,String> hashMapConfig = new HashMap<>();
				for(Object key : json.keySet()) {
					hashMapConfig.put(key.toString(), ((JSONObject)json.get(key)).get("value").toString());
				}
				return hashMapConfig;
			}else {
				return new HashMap<>();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			return new HashMap<>();
		}
	}

	@Transient
	public JSONObject getJSONConfig(){
		try {
			if(this.getConfig() != null && !this.getConfig().trim().equals("")) {
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(this.getConfig());
				return json;
			}else {
				return new JSONObject();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			return new JSONObject();
		}
	}
	
	@Transient
	public void setJSONConfig( JSONObject config) {
		try {
			this.setConfig(config.toJSONString());
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Transient
	public void updateJSONConfig( String key, String value) {
		try {
			JSONObject config = this.getJSONConfig();
			if(config.containsKey(key)) {
				((JSONObject) config.get(key)).put("value", value);
				this.setJSONConfig(config);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Transient 
	public void setJarFile(byte [] jarFile) {
		this.hash = FSUtils.md5hash(jarFile);
		this.base64JarFile = Base64.getEncoder().encodeToString(jarFile);
	}
	
	@Transient 
	public void parseJar(FileInputStream fis) throws IOException, ParseException {
		
		JarInputStream jarStream = new JarInputStream(fis);
		Manifest manifest = jarStream.getManifest();
		Attributes attr = manifest.getMainAttributes();
		String title = attr.getValue("Title");
		String author = attr.getValue("Author");
		String version = attr.getValue("Version");
		String url = attr.getValue("URL");
		JarEntry entry;
		ByteArrayOutputStream logo = new ByteArrayOutputStream();
		ByteArrayOutputStream description = new ByteArrayOutputStream();
		ByteArrayOutputStream configs = new ByteArrayOutputStream();
		Boolean isAssessmentApp = false;
		Boolean isVerificationApp = false;
		Boolean isVulnerabilityApp = false;
		Boolean isInventoryApp = false;
		Boolean isReportingApp = false;
		while ((entry = jarStream.getNextJarEntry()) != null) {
			if (!entry.isDirectory() && entry.getName().endsWith("description.md")) {
				while(jarStream.available() == 1) {
					byte[] data = new byte[512];
					int size = jarStream.read(data);
					if(size == -1)
						break;
					description.write(data, 0, size);
				}
			}
			if (!entry.isDirectory() && entry.getName().endsWith("config.json")) {
				while(jarStream.available() == 1) {
					byte[] data = new byte[512];
					int size = jarStream.read(data);
					if(size == -1)
						break;
					configs.write(data, 0, size);
				}
			}
			if (!entry.isDirectory() && entry.getName().endsWith("logo.png")) {
				while(jarStream.available() == 1) {
					byte[] data = new byte[512];
					int size = jarStream.read(data);
					if(size == -1)
						break;
					logo.write(data, 0, size);
				}
			}
			if (!entry.isDirectory() && entry.getName().endsWith("com.faction.extender.ApplicationInventory")) {
				isInventoryApp = true;
			}
			if (!entry.isDirectory() && entry.getName().endsWith("com.faction.extender.AssessmentManager")) {
				isAssessmentApp = true;
			}
			if (!entry.isDirectory() && entry.getName().endsWith("com.faction.extender.ReportManager")) {
				isReportingApp=true;
			}
			if (!entry.isDirectory() && entry.getName().endsWith("com.faction.extender.VulnerabilityManager")) {
				isVulnerabilityApp = true;
			}
			if (!entry.isDirectory() && entry.getName().endsWith("com.faction.extender.VerificationManager")) {
				isVerificationApp = true;
			}
		}
		//reset file pointer
		FileChannel     fc = fis.getChannel();
		fc.position(0);
		byte[] jarBytes = IOUtils.toByteArray(fis);
		fis.close();
		String sanitizedDesc = FSUtils.sanitizeHTML(description.toString());
		String base64Description =URLEncoder.encode(Base64.getEncoder().encodeToString(sanitizedDesc.getBytes()),"UTF-8");
		String base64Logo = URLEncoder.encode(Base64.getEncoder().encodeToString(logo.toByteArray()),"UTF-8");
		
		String configsString = configs.toString();
		if(configsString != null && !configsString.trim().equals("")) {
			JSONParser parser = new JSONParser();
			JSONObject jsonConfigs = (JSONObject) parser.parse(configs.toString());
			this.setJSONConfig(jsonConfigs);
		}
		
		this.setJarFile(jarBytes); //adds has and B64 encodes
		this.name = FSUtils.sanitizeHTML(title);
		this.version = FSUtils.sanitizeHTML(version);
		this.author = FSUtils.sanitizeHTML(author);
		this.url = FSUtils.sanitizeHTML(url);
		this.base64Logo = base64Logo;
		this.assessmentEnabled = isAssessmentApp;
		this.verificationEnabled = isVerificationApp;
		this.vulnerabilityEnabled = isVulnerabilityApp;
		this.inventoryEnabled = isInventoryApp;
		this.reportEnabled = isReportingApp;
		this.description = base64Description;
		this.enabled = false;
		this.uuid = UUID.randomUUID().toString();
		
	}
	
	@Transient 
	public void updateApp(FileInputStream fis) throws IOException, ParseException {
		String originalUUID = this.uuid;
		JSONObject originalConfigs = this.getJSONConfig();
		this.parseJar(fis);
		this.uuid=originalUUID;
		for(Object key : originalConfigs.keySet()) {
			this.updateJSONConfig(key.toString(), ((JSONObject) originalConfigs.get(key)).get("value").toString());
		}
	}
	
	@Transient
	public String getMeta() {
		String json = "{";
		json += " \"extension_info\": { \"title\": \"" + this.getName() +"\", "
				+ "\"version\": \"" + this.getVersion() + "\", "
				+ "\"author\": \"" + this.getAuthor() + "\", "
				+ "\"url\": \"" + this.getUrl() + "\", "
				+ "\"logo\": \"" + this.getBase64Logo()+ "\", "
				+ "\"hash\": \"" + this.getHash()+ "\", "
				+ "\"assessment\": " + this.getAssessmentEnabled() + ", "
				+ "\"verification\": " + this.getVerificationEnabled() + ", "
				+ "\"vulnerability\": " + this.getVulnerabilityEnabled() + ", "
				+ "\"inventory\": " + this.getInventoryEnabled() + ", "
				+ "\"description\": \"" + this.getDescription() + "\"}"
				+ "}";
		return json;
	}
	
	

}
