package com.fuse.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class ReportMap {
	
	public static enum DataProperties { 
		VulnName(0l), VulnDescription(1l),Severity(2l), Recommendation(3l),ExploitStep(4l), None(5l);
		Long value;
		DataProperties(Long v){
			value = v;
		}
		public  Long getValue(){
			return value;
		}
		public static DataProperties getProp(Long value){
			int v = value.intValue();
			switch(v){
				case 0: return VulnName;
				case 1: return VulnDescription;
				case 2: return Severity;
				case 3: return Recommendation;
				case 4: return ExploitStep;
				case 5: return None;
				default : return null;
			}
		}
	
	};

	@Id
	@GeneratedValue
	private Long id;
	private String reportName;
	private String listname;
	@ElementCollection
	private Map<String, Long> mapRating = new HashMap();
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<MapItem> mapping = new ArrayList();
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
	private List<VulnMap> vulnMap = new ArrayList();
	@OneToOne
	private DefaultVulnerability defaultVuln;
	@ElementCollection
	private Map<String,String> customFields = new HashMap();
	
	
	public Long getId() {
		return id;
	}

	public String getListname() {
		return listname;
	}
	public List<MapItem> getMapping() {
		return mapping;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setListname(String listname) {
		this.listname = listname;
	}
	
	public void setMapping(List<MapItem> mapping) {
		this.mapping = mapping;
	}
	public Map<String, Long> getMapRating() {
		return mapRating;
	}
	
	public void setMapRating(Map<String, Long> mapRating) {
		this.mapRating = mapRating;
	}
	public List<VulnMap> getVulnMap() {
		return vulnMap;
	}
	public void setVulnMap(List<VulnMap>vulnMap) {
		this.vulnMap = vulnMap;
	}
	public DefaultVulnerability getDefaultVuln() {
		return defaultVuln;
	}
	public void setDefaultVuln(DefaultVulnerability defaultVuln) {
		this.defaultVuln = defaultVuln;
	}
	public Map<String, String> getCustomFields() {
		return customFields;
	}
	public void setCustomFields(Map<String, String> customFields) {
		this.customFields = customFields;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	
	
	
	
	
}
