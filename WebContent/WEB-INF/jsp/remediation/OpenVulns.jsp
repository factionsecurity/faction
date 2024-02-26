<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

  <div class="row">
            <div class="col-xs-12">
              <div class="box box-primary">
                <div class="box-body">
                  <bs:row>      
                  <bs:inputgroup name="" id="appid" colsize="3" placeholder="Application Id (Exact Match)"></bs:inputgroup>
		 		  <bs:inputgroup name="" id="appname" colsize="3" placeholder="Application Name (Partial Match)"></bs:inputgroup>
		 		  <bs:inputgroup name="" id="tracking" colsize="3" placeholder="Tracking ID"></bs:inputgroup>      
           		  <bs:button color="info" size="md" colsize="3" text="Search" id="search"></bs:button>
           		 </bs:row>
           		 <br>
           		 <bs:row>
           		 	<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
			   	 		<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
					        <bs:mco colsize="1">
			                    <label>
			                      <input type="checkbox" class="minimal" checked id="levelbx${riskId}"> ${risk }
			                    </label>
		             		</bs:mco>
						</s:if>
					</s:iterator>
		             <bs:mco colsize="1">
		                    <label>
		                      <input type="checkbox" class="minimal" id="closedcbx"> Closed
		                    </label>
		             </bs:mco>
		              <bs:mco colsize="1">
		                    <label>
		                      <input type="checkbox" class="minimal" id="opencbx" checked > Open
		                    </label>
		             </bs:mco>
		             
		             

                    
                  </bs:row>
           		 <br>
                  <hr>
                  <table id="vulntable" class="table table-striped table-hover dataTable">
                    <thead class="theader">
                      <tr>
                      	<th></th>
                      	<th>App Name</th>
                      	<th>Assessor</th>
                      	<th>Tracking</th>
                      	<th>Status</th>
                        <th>Name</th>
                        <th>Severity</th>
                        <th>Opened</th>
                        <th>Closed in Dev</th>
                        <th>Closed in Prod</th>
                      </tr>
                    </thead>
                    <tbody>
					
					</tbody>
                    <tfoot>
                    </tfoot>
                  </table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
       </div>
 </div>
  