<%@page import="org.apache.struts2.components.Include" %>
    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
        <%@ taglib prefix="s" uri="/struts-tags" %>
            <%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld" %>
                <jsp:include page="../header.jsp" />
                <link href="../dist/css/jquery.autocomplete.css" media="all" rel="stylesheet" type="text/css" />
                <style>
                    .disabled {
                        opacity: 0.3;
                    }

                    .provider-fields {
                        display: none;
                    }

                    .provider-fields.active {
                        display: block;
                    }

                    .test-connection-btn {
                        margin-top: 15px;
                    }

                    .masked-key {
                        font-family: monospace;
                        color: #666;
                    }
                    .modal-content {
                    	background-color: #030D1C;
                    }
                </style>
                <link rel="stylesheet" href="../plugins/iCheck/all.css">

                <!-- Content Wrapper. Contains page content -->
                <div class="content-wrapper">
                    <!-- Content Header (Page header) -->
                    <section class="content-header">
                        <h1>
                            <i class="fa fa-robot"></i> AI Configuration <small>
                                Manage LLM Provider Settings</small>
                        </h1>
                    </section>

                    <!-- Main content -->
                    <section class="content">
                        <div class="row">
                            <div class="col-xs-12">
                                <div class="box box-primary">
                                    <div class="box-body">
                                        <div class="row">
                                            <div class="col-xs-6">
                                                <div class="col-sm-3" style="margin-bottom: -30px; z-index: 1">
                                                    <button class="btn btn-block btn-primary btn-sm" id="addConfig">Add
                                                        LLM Config</button>
                                                </div>
                                            </div>
                                        </div>
                                        <table id="configTable" class="table table-striped table-hover dataTable">
                                            <thead class="theader">
                                                <tr>
                                                    <th>Name</th>
                                                    <th>Provider</th>
                                                    <th>Model</th>
                                                    <th>Status</th>
                                                    <th>Created</th>
                                                    <th>Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <s:iterator value="llmConfigs">
                                                    <tr>
                                                        <td>
                                                            <s:property value="name" />
                                                        </td>
                                                        <td>
                                                            <span class="label label-info">
                                                                <s:if test="provider == 'OPENAI'">OpenAI</s:if>
                                                                <s:elseif test="provider == 'OPENAI_COMPATIBLE'">OpenAI Compatible</s:elseif>
                                                                <s:elseif test="provider == 'AZURE_OPENAI'">Azure OpenAI
                                                                </s:elseif>
                                                                <s:elseif test="provider == 'AWS_BEDROCK'">AWS Bedrock
                                                                </s:elseif>
                                                                <s:elseif test="provider == 'CLAUDE'">Claude</s:elseif>
                                                                <s:else>
                                                                    <s:property value="provider" />
                                                                </s:else>
                                                            </span>
                                                        </td>
                                                        <td>
                                                            <s:property value="model" />
                                                        </td>
                                                        <td>
                                                            <s:if test="active">
                                                                <span class="label label-success">Active</span>
                                                            </s:if>
                                                            <s:else>
                                                                <span class="label label-warning">Inactive</span>
                                                            </s:else>
                                                        </td>
                                                        <td>
                                                            <s:date name="createdDate" format="yyyy-MM-dd" />
                                                        </td>
                                                        <td>
                                                            <span class="vulnControl editConfig"
                                                                data-config-id="<s:property value='id'/>"
                                                                onclick="editConfig(this.getAttribute('data-config-id'))">
                                                                <i class="fa fa-edit"></i>
                                                            </span>
                                                            <span class="vulnControl testConfig"
                                                                data-config-id="<s:property value='id'/>"
                                                                onclick="testConfig(this.getAttribute('data-config-id'))"
                                                                title="Test Connection">
                                                                <i class="fa fa-plug"></i>
                                                            </span>
                                                            <span class="vulnControl vulnControl-delete deleteConfig"
                                                                data-config-id="<s:property value='id'/>"
                                                                data-config-name="<s:property value='name'/>"
                                                                onclick="deleteConfig(this.getAttribute('data-config-id'), this.getAttribute('data-config-name'))">
                                                                <i class="fa fa-trash"></i>
                                                            </span>
                                                        </td>
                                                    </tr>
                                                </s:iterator>
                                            </tbody>
                                            <tfoot>
                                            </tfoot>
                                        </table>
                                    </div>
                                    <!-- /.box-body -->
                                </div>
                                <!-- /.box -->
                            </div>
                        </div>

                        <!-- LLM Config Modal -->
                        <div class="modal" id="configModal">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header bg-primary">
                                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                            <span aria-hidden="true">&times;</span>
                                        </button>
                                        <h4 class="modal-title">
                                            <b><i class="fa fa-robot"></i> LLM Configuration</b>
                                        </h4>
                                    </div>
                                    <div class="modal-body">
                                        <form class="form-horizontal" autocomplete="off">
                                            <div class="box-body">
                                                <!-- Basic Configuration -->
                                                <div class="form-group">
                                                    <label>Configuration Name: *</label>
                                                    <input class="form-control" id="configName"
                                                        placeholder="e.g. Production OpenAI">
                                                </div>

                                                <div class="form-group">
                                                    <label>Provider: *</label>
                                                    <select class="form-control" style="width: 100%;"
                                                        id="provider">
                                                        <option value="">Select Provider</option>
                                                        <option value="OPENAI">OpenAI</option>
                                                        <option value="OPENAI_COMPATIBLE">OpenAI Compatible (LM Studio)</option>
                                                        <!-- <option value="AZURE_OPENAI">Azure OpenAI</option>
                                                        <option value="AWS_BEDROCK">AWS Bedrock</option> -->
                                                        <option value="CLAUDE">Claude</option>
                                                    </select>
                                                </div>

                                                <!-- OpenAI Fields -->
                                                <div id="openai-fields" class="provider-fields">
                                                    <h4 class="text-primary">OpenAI Configuration</h4>
                                                    <div class="form-group">
                                                        <label>API Key: *</label>
                                                        <input type="password" class="form-control" id="openai_apiKey"
                                                            placeholder="sk-...">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Base URL:</label>
                                                        <input class="form-control" id="openai_baseUrl"
                                                            placeholder="https://api.openai.com/v1 (leave empty for default)">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Model:</label>
                                                        <select class="form-control" id="openai_model">
                                                            <option value="">Select Model</option>
                                                            <option value="gpt-4">GPT-4</option>
                                                            <option value="gpt-4-turbo">GPT-4 Turbo</option>
                                                            <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
                                                            <option value="gpt-3.5-turbo-16k">GPT-3.5 Turbo 16K</option>
                                                        </select>
                                                    </div>
                                                </div>

                                                <!-- Azure OpenAI Fields -->
                                                <div id="azure-fields" class="provider-fields">
                                                    <h4 class="text-primary">Azure OpenAI Configuration</h4>
                                                    <div class="form-group">
                                                        <label>API Key: *</label>
                                                        <input type="password" class="form-control" id="azure_apiKey"
                                                            placeholder="Your Azure OpenAI API Key">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Endpoint: *</label>
                                                        <input class="form-control" id="azure_endpoint"
                                                            placeholder="https://your-resource.openai.azure.com/">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Deployment Name: *</label>
                                                        <input class="form-control" id="azure_deployment"
                                                            placeholder="your-gpt-4-deployment">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>API Version:</label>
                                                        <input class="form-control" id="azure_apiVersion"
                                                            placeholder="2023-12-01-preview" value="2023-12-01-preview">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Model:</label>
                                                        <select class="form-control" id="azure_model">
                                                            <option value="">Select Model</option>
                                                            <option value="gpt-35-turbo">GPT-3.5 Turbo</option>
                                                            <option value="gpt-4">GPT-4</option>
                                                            <option value="gpt-4-turbo">GPT-4 Turbo</option>
                                                            <option value="gpt-4-32k">GPT-4 32K</option>
                                                        </select>
                                                    </div>
                                                </div>

                                                <!-- AWS Bedrock Fields -->
                                                <div id="bedrock-fields" class="provider-fields">
                                                    <h4 class="text-primary">AWS Bedrock Configuration</h4>
                                                    <div class="form-group">
                                                        <label>Access Key ID: *</label>
                                                        <input type="password" class="form-control"
                                                            id="bedrock_accessKey" placeholder="AKIA...">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Secret Access Key: *</label>
                                                        <input type="password" class="form-control"
                                                            id="bedrock_secretKey" placeholder="Your AWS Secret Key">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Region: *</label>
                                                        <select class="form-control" id="bedrock_region">
                                                            <option value="">Select Region</option>
                                                            <option value="us-east-1">US East (N. Virginia)</option>
                                                            <option value="us-west-2">US West (Oregon)</option>
                                                            <option value="eu-west-1">Europe (Ireland)</option>
                                                            <option value="ap-southeast-1">Asia Pacific (Singapore)
                                                            </option>
                                                        </select>
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Model:</label>
                                                        <select class="form-control" id="bedrock_model">
                                                            <option value="">Select Model</option>
                                                            <option value="claude-3-opus-20240229">Claude 3 Opus
                                                            </option>
                                                            <option value="claude-3-sonnet-20240229">Claude 3 Sonnet
                                                            </option>
                                                            <option value="claude-3-haiku-20240307">Claude 3 Haiku
                                                            </option>
                                                            <option value="claude-instant-v1">Claude Instant</option>
                                                            <option value="claude-v2">Claude 2</option>
                                                        </select>
                                                    </div>
                                                </div>

                                                <!-- Claude Fields -->
                                                <div id="claude-fields" class="provider-fields">
                                                    <h4 class="text-primary">Claude Configuration</h4>
                                                    <div class="form-group">
                                                        <label>API Key: *</label>
                                                        <input type="password" class="form-control" id="claude_apiKey"
                                                            placeholder="sk-ant-api...">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Model:</label>
                                                        <select class="form-control" id="claude_model">
                                                            <option value="">Select Model</option>
                                                            <option value="claude-3-opus-20240229">Claude 3 Opus
                                                            </option>
                                                            <option value="claude-3-sonnet-20240229">Claude 3 Sonnet
                                                            </option>
                                                            <option value="claude-3-haiku-20240307">Claude 3 Haiku
                                                            </option>
                                                        </select>
                                                    </div>
                                                </div>

                                                <!-- OpenAI Compatible Fields (LM Studio) -->
                                                <div id="openai-compatible-fields" class="provider-fields">
                                                    <h4 class="text-primary">OpenAI Compatible Configuration
                                                        <small>(LM Studio, Ollama, etc.)</small>
                                                    </h4>
                                                    <div class="form-group">
                                                        <label>Base URL: *</label>
                                                        <input class="form-control" id="openai_compatible_baseUrl"
                                                            placeholder="http://localhost:1234/v1">
                                                        <p class="help-block">The base URL of your OpenAI-compatible server. Models will load automatically when you enter the URL.</p>
                                                    </div>
                                                    <div class="form-group">
                                                        <label>API Key: <small class="text-muted">(optional)</small></label>
                                                        <input type="password" class="form-control" id="openai_compatible_apiKey"
                                                            placeholder="Leave empty if not required">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Model: *</label>
                                                        <select class="form-control" id="openai_compatible_model">
                                                            <option value="">Enter Base URL above to load models</option>
                                                        </select>
                                                    </div>
                                                </div>

                                                <!-- GitHub Copilot Fields -->
                                                <div id="github-copilot-fields" class="provider-fields">
                                                    <h4 class="text-primary">GitHub Copilot Configuration</h4>
                                                    <div class="alert alert-info">
                                                        <i class="fa fa-info-circle"></i>
                                                        GitHub Copilot is <strong>per-user</strong>. Each user must add
                                                        their own GitHub token under <strong>Profile Settings</strong>.
                                                        No shared API key is stored here.
                                                    </div>
                                                    <div class="form-group">
                                                        <label>Model: *</label>
                                                        <select class="form-control" id="github_copilot_model">
                                                            <option value="">Select Model</option>
                                                            <option value="gpt-4o">GPT-4o</option>
                                                            <option value="gpt-4o-mini">GPT-4o Mini</option>
                                                            <option value="gpt-4">GPT-4</option>
                                                            <option value="o1-mini">o1 Mini</option>
                                                            <option value="o1-preview">o1 Preview</option>
                                                        </select>
                                                        <p class="help-block">Enter a GitHub token below to reload models available on your Copilot plan.</p>
                                                    </div>
                                                    <div class="form-group">
                                                        <label>GitHub Token: <small class="text-muted">(used to load models only — not saved here)</small></label>
                                                        <input type="password" class="form-control" id="github_copilot_apiKey"
                                                            placeholder="github_pat_...">
                                                    </div>
                                                </div>

                                                <!-- Status -->
                                                <div class="form-group">
                                                    <label>
                                                        <input type="checkbox" class="minimal" id="configActive"
                                                            checked> Active
                                                    </label>
                                                </div>

                                                <!-- Test Connection Button -->
                                                <div class="test-connection-btn">
                                                    <button type="button" class="btn btn-success"
                                                        id="testConnectionBtn">
                                                        <i class="fa fa-plug"></i> Test Connection
                                                    </button>
                                                    <span id="testResult" class="text-success"
                                                        style="margin-left: 10px;"></span>
                                                </div>
                                            </div>
                                        </form>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-default pull-left"
                                            data-dismiss="modal">Close</button>
                                        <button type="button" class="btn btn-primary" id="saveConfig">
                                            <i class="fa fa-save"></i> Save Configuration
                                        </button>
                                    </div>
                                </div>
                                <!-- /.modal-content -->
                            </div>
                            <!-- /.modal-dialog -->
                        </div>
                        <!-- /.modal -->

                    </section>
                </div>

                <jsp:include page="../footer.jsp" />
                <script src="../dist/js/aiconfig.js" charset="utf-8"></script>
                </body>

                </html>