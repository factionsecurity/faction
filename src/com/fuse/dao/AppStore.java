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
	private Boolean assessmentEnabled;
	private Integer assessmentOrder;
	private Boolean verificationEnabled;
	private Integer verificationOrder;
	private Boolean vulnerabilityEnabled;
	private Integer vulnerabilityOrder;
	private Boolean inventoryEnabled;
	private Integer inventoryOrder;
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
		return assessmentEnabled;
	}
	public void setAssessmentEnabled(Boolean assessmentEnabled) {
		this.assessmentEnabled = assessmentEnabled;
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
	
	

}
