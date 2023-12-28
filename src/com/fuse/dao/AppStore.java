package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fuse.utils.FSUtils;

@Entity
public class AppStore {
	
	@Id
	@GeneratedValue
	private Long id;
	private Integer order;
	private String name;
	private String author;
	private String url;
	private String description;
	private String version;
	private Boolean approved;
	private Boolean enabled;
	private String base64Logo;
	private Boolean AssessmentEnabled;
	private Boolean VerificationEnabled;
	private Boolean VulnerabilityEnabled;
	private Boolean InventoryEnabled;
	private String base64JarFile;
	private String hash;
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
		return AssessmentEnabled;
	}
	public void setAssessmentEnabled(Boolean assessmentEnabled) {
		AssessmentEnabled = assessmentEnabled;
	}
	public Boolean getVerificationEnabled() {
		return VerificationEnabled;
	}
	public void setVerificationEnabled(Boolean verificationEnabled) {
		VerificationEnabled = verificationEnabled;
	}
	public Boolean getVulnerabilityEnabled() {
		return VulnerabilityEnabled;
	}
	public void setVulnerabilityEnabled(Boolean vulnerabilityEnabled) {
		VulnerabilityEnabled = vulnerabilityEnabled;
	}
	public Boolean getInventoryEnabled() {
		return InventoryEnabled;
	}
	public void setInventoryEnabled(Boolean inventoryEnabled) {
		InventoryEnabled = inventoryEnabled;
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
	

}
