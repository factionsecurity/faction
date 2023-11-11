package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class License {
	
	@Id
	@GeneratedValue
	private Long id;
	private String data;
	private String product;
	private String signature;
	public Long getId() {
		return id;
	}
	public String getData() {
		return data;
	}
	public String getProduct() {
		return product;
	}
	public String getSignature() {
		return signature;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setData(String data) {
		this.data = data;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	

}
