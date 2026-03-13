<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="s" uri="/struts-tags" %>
        <%@ page trimDirectiveWhitespaces="true" %>
            {
            "id": "<s:property value="selectedConfig.id" />",
            "name": "<s:property value="selectedConfig.name" />",
            "provider": "<s:property value="selectedConfig.provider" />",
            "apiKey": "<s:property value="selectedConfig.maskedApiKey" />",
            "baseUrl": "<s:property value="selectedConfig.baseUrl"  />",
            "endpoint": "<s:property value="selectedConfig.endpoint"  />",
            "deployment": "<s:property value="selectedConfig.deployment"/>",
            "accessKey": "<s:property value="selectedConfig.maskedAccessKey" />",
            "secretKey": "<s:property value="selectedConfig.maskedSecretKey" />",
            "region": "<s:property value="selectedConfig.region"/>",
            "model": "<s:property value="selectedConfig.model"/>",
            "apiVersion": "<s:property value="selectedConfig.apiVersion"/>",
            "active": "<s:property value="selectedConfig.active" />",
            "createdDate": "<s:date name="selectedConfig.createdDate" format="yyyy-MM-dd HH:mm:ss" />",
            "modifiedDate": "<s:date name="selectedConfig.modifiedDate" format="yyyy-MM-dd HH:mm:ss" />"
            }