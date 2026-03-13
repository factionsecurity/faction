/**
 * AI Configuration JavaScript
 * Handles dynamic form switching, validation, and AJAX operations
 */

// Import required dependencies
import 'bootstrap';
import 'datatables.net';
import 'datatables.net-bs';
import 'jquery';
import 'select2';

var currentConfigId = null;
var isEditMode = false;

$(document).ready(function () {
    // Initialize DataTable
    $('#configTable').DataTable({
        "paging": true,
        "lengthChange": false,
        "searching": true,
        "ordering": true,
        "info": true,
        "autoWidth": false,
        "pageLength": 25
    });

    // Initialize form elements
    $('.select2').select2();

    // Event handlers
    $('#addConfig').click(function () {
        openConfigModal(false);
    });

    $('#saveConfig').click(function () {
        saveConfiguration();
    });

    $('#testConnectionBtn').click(function () {
        testConnection();
    });

    $('#provider').change(function () {
        showProviderFields();
        loadProviderModels();
    });

    // Add event handlers for API key fields to reload models when credentials are entered
    $(document).on('blur', '#openai_apiKey, #claude_apiKey', function () {
        var provider = $('#provider').val();
        if (provider && $(this).val().trim() !== '') {
            loadProviderModelsWithCredentials();
        }
    });

    // Add base URL change handler for OpenAI
    $(document).on('blur', '#openai_baseUrl', function () {
        var provider = $('#provider').val();
        if (provider === 'OPENAI' && $('#openai_apiKey').val().trim() !== '') {
            loadProviderModelsWithCredentials();
        }
    });

    // Clear form when modal is closed
    $('#configModal').on('hidden.bs.modal', function () {
        clearForm();
    });
});

/**
 * Open configuration modal in add or edit mode
 */
function openConfigModal(editMode) {
    isEditMode = editMode;
    currentConfigId = null;

    if (editMode) {
        $('.modal-title').html('<b><i class="fa fa-robot"></i> Edit LLM Configuration</b>');
    } else {
        $('.modal-title').html('<b><i class="fa fa-robot"></i> Add LLM Configuration</b>');
    }
	

    clearForm();
    $('#configModal').modal('show');
}

/**
 * Clear all form fields
 */
function clearForm() {
    $('#configName').val('');
    $('#provider').val('').trigger('change');
    $('.provider-fields').removeClass('active');

    // Clear all provider-specific fields
    $('input[id^="openai_"], input[id^="azure_"], input[id^="bedrock_"], input[id^="claude_"]').val('');
    $('select[id^="openai_"], select[id^="azure_"], select[id^="bedrock_"], select[id^="claude_"]').val('');

    $('#configActive').prop('checked', true);
}

/**
 * Show/hide provider-specific fields based on selection
 */
function showProviderFields() {
    var selectedProvider = $('#provider').val();

    // Hide all provider fields
    $('.provider-fields').removeClass('active');

    // Show selected provider fields
    if (selectedProvider) {
        var fieldId = '';
        switch (selectedProvider) {
            case 'OPENAI':
                fieldId = 'openai-fields';
                break;
            case 'AZURE_OPENAI':
                fieldId = 'azure-fields';
                break;
            case 'AWS_BEDROCK':
                fieldId = 'bedrock-fields';
                break;
            case 'CLAUDE':
                fieldId = 'claude-fields';
                break;
        }

        if (fieldId) {
            $('#' + fieldId).addClass('active');
        }
    }
}

/**
 * Load available models for the selected provider
 */
function loadProviderModels() {
    var provider = $('#provider').val();

    if (!provider) {
        return;
    }

    $.post('GetProviderModels', {
        provider: provider,
        '_token': $('[name="_token"]').val()
    })
        .done(function (data) {
            populateModelSelect(provider, data);
        })
        .fail(function () {
            console.log('Failed to load models for provider: ' + provider);
        });
}

/**
 * Load available models with API credentials to get real models from API
 */
function loadProviderModelsWithCredentials() {
    var provider = $('#provider').val();
    if (!provider) {
        return;
    }

    var credentials = collectProviderCredentials(provider);
    if (!credentials) {
        return;
    }

    var requestData = {
        provider: provider,
        '_token': $('[name="_token"]').val()
    };

    // Add credentials to request
    Object.assign(requestData, credentials);

    $.post('GetProviderModels', requestData)
        .done(function (data) {
            populateModelSelect(provider, data);

            // Show feedback that models were loaded from API
            if (data && data.length > 0) {
                $('#testResult').text('Models loaded from API!').addClass('text-success').removeClass('text-danger');
                setTimeout(function () {
                    $('#testResult').text('').removeClass('text-success text-danger');
                }, 3000);
            }
        })
        .fail(function () {
            console.log('Failed to load models with credentials for provider: ' + provider);
            // Fall back to loading static models
            loadProviderModels();
        });
}

/**
 * Collect credentials for the given provider
 */
function collectProviderCredentials(provider) {
    var credentials = {};

    switch (provider) {
        case 'OPENAI':
            var apiKey = $('#openai_apiKey').val();
            var baseUrl = $('#openai_baseUrl').val();
            if (!apiKey || apiKey.trim() === '') return null;

            credentials.apiKey = apiKey;
            if (baseUrl && baseUrl.trim() !== '') {
                credentials.baseUrl = baseUrl;
            }
            break;

        case 'CLAUDE':
            var apiKey = $('#claude_apiKey').val();
            if (!apiKey || apiKey.trim() === '') return null;

            credentials.apiKey = apiKey;
            break;

        case 'AZURE_OPENAI':
        case 'AWS_BEDROCK':
            // These providers don't support dynamic model fetching via simple API calls
            return null;

        default:
            return null;
    }

    return credentials;
}

/**
 * Populate the model select dropdown
 */
function populateModelSelect(provider, data) {
    var modelSelect = $('#' + provider.toLowerCase().replace('_', '_') + '_model');
	console.log(provider);
	console.log(modelSelect);

    // Clear existing options
    modelSelect.empty();
    modelSelect.append('<option value="">Select Model</option>');

    // Add new options
    if (data && data.length > 0) {
        $.each(data, function (index, model) {
            modelSelect.append('<option value="' + model.value + '">' + model.label + '</option>');
        });
    }
}

/**
 * Edit configuration - load data from server
 */
global.editConfig = function editConfig(configId) {
    currentConfigId = configId;

    $.post('GetLLMConfig', {
        configId: configId,
        '_token': $('[name="_token"]').val()
    })
        .done(function (data) {
            if (data) {
                populateForm(data);
                openConfigModal(true);
            } else {
                showErrorMessage('Failed to load configuration data');
            }
        })
        .fail(function () {
            showErrorMessage('Error loading configuration');
        });
}

/**
 * Populate form with configuration data
 */
function populateForm(config) {
    $('#configName').val(config.name || '');
    $('#provider').val(config.provider || '').trigger('change');

    // Set provider-specific fields based on provider type
    if (config.provider) {
        var prefix = config.provider.toLowerCase().replace('_', '_');

        // Common fields
        $('#' + prefix + '_model').val(config.model || '');

        switch (config.provider) {
            case 'OPENAI':
                $('#openai_apiKey').val(''); // Never populate password fields
                $('#openai_baseUrl').val(config.baseUrl || '');
                break;

            case 'AZURE_OPENAI':
                $('#azure_apiKey').val(''); // Never populate password fields
                $('#azure_endpoint').val(config.endpoint || '');
                $('#azure_deployment').val(config.deployment || '');
                $('#azure_apiVersion').val(config.apiVersion || '');
                break;

            case 'AWS_BEDROCK':
                $('#bedrock_accessKey').val(''); // Never populate password fields
                $('#bedrock_secretKey').val(''); // Never populate password fields
                $('#bedrock_region').val(config.region || '');
                break;

            case 'CLAUDE':
                $('#claude_apiKey').val(''); // Never populate password fields
                break;
        }
    }

    $('#configActive').prop('checked', config.active === true);
}

/**
 * Save configuration via AJAX
 */
function saveConfiguration() {
    if (!validateForm()) {
        return;
    }

    var formData = collectFormData();

    if (currentConfigId) {
        formData.configId = 'config' + currentConfigId;
    }

    formData._token = $('[name="_token"]').val();

    $('#saveConfig').prop('disabled', true).html('<i class="fa fa-spinner fa-spin"></i> Saving...');

    $.post('SaveLLMConfig', formData)
        .done(function (data) {
            if (data.result === 'success') {
                showSuccessMessage('Configuration saved successfully');
                $('#configModal').modal('hide');
                // Reload page to refresh the table
                setTimeout(function () {
                    window.location.reload();
                }, 1000);
            } else {
                showErrorMessage(data.message || 'Failed to save configuration');
            }
        })
        .fail(function () {
            showErrorMessage('Error saving configuration');
        })
        .always(function () {
            $('#saveConfig').prop('disabled', false).html('<i class="fa fa-save"></i> Save Configuration');
        });
}

/**
 * Validate form data
 */
function validateForm() {
    var errors = [];

    // Basic validation
    if (!$('#configName').val().trim()) {
        errors.push('Configuration name is required');
    }

    var provider = $('#provider').val();
    if (!provider) {
        errors.push('Provider is required');
    }

    // Provider-specific validation
    if (provider) {
        switch (provider) {
            case 'OPENAI':
                if (!$('#openai_apiKey').val().trim()) {
                    errors.push('API Key is required for OpenAI');
                }
                break;

            case 'AZURE_OPENAI':
                if (!$('#azure_apiKey').val().trim()) {
                    errors.push('API Key is required for Azure OpenAI');
                }
                if (!$('#azure_endpoint').val().trim()) {
                    errors.push('Endpoint is required for Azure OpenAI');
                }
                if (!$('#azure_deployment').val().trim()) {
                    errors.push('Deployment is required for Azure OpenAI');
                }
                break;

            case 'AWS_BEDROCK':
                if (!$('#bedrock_accessKey').val().trim()) {
                    errors.push('Access Key is required for AWS Bedrock');
                }
                if (!$('#bedrock_secretKey').val().trim()) {
                    errors.push('Secret Key is required for AWS Bedrock');
                }
                if (!$('#bedrock_region').val().trim()) {
                    errors.push('Region is required for AWS Bedrock');
                }
                break;

            case 'CLAUDE':
                if (!$('#claude_apiKey').val().trim()) {
                    errors.push('API Key is required for Claude');
                }
                break;
        }
    }

    if (errors.length > 0) {
        showErrorMessage(errors.join('<br>'));
        return false;
    }

    return true;
}

/**
 * Collect form data into object
 */
function collectFormData() {
    var provider = $('#provider').val();
    var data = {
        name: $('#configName').val().trim(),
        provider: provider,
        active: $('#configActive').is(':checked')
    };

    // Add provider-specific fields
    switch (provider) {
        case 'OPENAI':
            data.apiKey = $('#openai_apiKey').val();
            data.baseUrl = $('#openai_baseUrl').val();
            data.model = $('#openai_model').val();
            break;

        case 'AZURE_OPENAI':
            data.apiKey = $('#azure_apiKey').val();
            data.endpoint = $('#azure_endpoint').val();
            data.deployment = $('#azure_deployment').val();
            data.apiVersion = $('#azure_apiVersion').val();
            data.model = $('#azure_model').val();
            break;

        case 'AWS_BEDROCK':
            data.accessKey = $('#bedrock_accessKey').val();
            data.secretKey = $('#bedrock_secretKey').val();
            data.region = $('#bedrock_region').val();
            data.model = $('#bedrock_model').val();
            break;

        case 'CLAUDE':
            data.apiKey = $('#claude_apiKey').val();
            data.model = $('#claude_model').val();
            break;
    }

    return data;
}

/**
 * Test connection with current form data
 */
function testConnection() {
    if (!validateForm()) {
        return;
    }

    var formData = collectFormData();
    formData._token = $('[name="_token"]').val();

    $('#testConnectionBtn').prop('disabled', true).html('<i class="fa fa-spinner fa-spin"></i> Testing...');
    $('#testResult').text('').removeClass('text-success text-danger');

    $.post('TestLLMConnection', formData)
        .done(function (data) {
            if (data.result === 'success') {
                $('#testResult').text('Connection successful!').addClass('text-success');
            } else {
                $('#testResult').text('Connection failed: ' + (data.message || 'Unknown error')).addClass('text-danger');
            }
        })
        .fail(function () {
            $('#testResult').text('Connection test failed').addClass('text-danger');
        })
        .always(function () {
            $('#testConnectionBtn').prop('disabled', false).html('<i class="fa fa-plug"></i> Test Connection');
        });
}

/**
 * Test existing configuration
 */
global.testConfig = function testConfig(configId) {
    $.post('TestLLMConnection', {
        configId: 'config' + configId,
        '_token': $('[name="_token"]').val()
    })
        .done(function (data) {
            if (data.result === 'success') {
                showSuccessMessage('Connection test successful!');
            } else {
                showErrorMessage('Connection test failed: ' + (data.message || 'Unknown error'));
            }
        })
        .fail(function () {
            showErrorMessage('Connection test failed');
        });
}

/**
 * Delete configuration with confirmation
 */
global.deleteConfig = function deleteConfig(configId, configName) {
    if (confirm('Are you sure you want to delete the configuration "' + configName + '"?')) {
        $.post('DeleteLLMConfig', {
            configId: configId,
            '_token': $('[name="_token"]').val()
        })
            .done(function (data) {
                if (data.result === 'success') {
                    showSuccessMessage('Configuration deleted successfully');
                    // Reload page to refresh the table
                    setTimeout(function () {
                        window.location.reload();
                    }, 1000);
                } else {
                    showErrorMessage(data.message || 'Failed to delete configuration');
                }
            })
            .fail(function () {
                showErrorMessage('Error deleting configuration');
            });
    }
}

/**
 * Show success message
 */
function showSuccessMessage(message) {
    // Use existing notification system if available, otherwise use alert
    if (typeof showMessage === 'function') {
        showMessage(message, 'success');
    } else {
        alert(message);
    }
}

/**
 * Show error message
 */
function showErrorMessage(message) {
    // Use existing notification system if available, otherwise use alert
    if (typeof showMessage === 'function') {
        showMessage(message, 'error');
    } else {
        alert(message);
    }
}