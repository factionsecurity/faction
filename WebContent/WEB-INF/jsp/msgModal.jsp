<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<bs:modal modalId="errorModal" saveId="" title="Error" color="red"><div id="errorModalMsg"></div></bs:modal>
<bs:modal modalId="successModal" saveId="" title="Success" color="red"><div id="successModalMsg"></div></bs:modal>
<script>
function showError(Message){
	$("#errorModalMsg").html(Message);
	$("#errorModal").modal('show');
}
function showSuccess(Message){
	$("#successModalMsg").html(Message);
	$("#successModal").modal('show');
}
</script>