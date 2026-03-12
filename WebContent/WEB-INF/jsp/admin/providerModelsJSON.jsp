<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="s" uri="/struts-tags" %>
        <%@ page trimDirectiveWhitespaces="true" %>
            <s:if test="availableModels != null && availableModels.size() > 0">
                [
                <s:iterator value="availableModels" status="status">
                    {
                    "value": "<s:property value="value" />",
                    "label": "<s:property value="label" />"
                    }<s:if test="!#status.last">,</s:if>
                </s:iterator>
                ]
            </s:if>
            <s:else>
                []
            </s:else>