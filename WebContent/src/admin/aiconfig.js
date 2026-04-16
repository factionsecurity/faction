/**
 * AI Configuration JavaScript
 * Handles dynamic form switching, validation, and AJAX operations
 */

// Import required dependencies
import 'bootstrap';
import 'datatables.net';
import 'datatables.net-bs';
import 'jquery';
import 'jquery-confirm';
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
    $(document).on('blur', '#openai_apiKey, #claude_apiKey, #github_copilot_apiKey', function () {
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

    // Load models for OpenAI Compatible when base URL is entered
    $(document).on('blur', '#openai_compatible_baseUrl', function () {
        var provider = $('#provider').val();
        if (provider === 'OPENAI_COMPATIBLE' && $(this).val().trim() !== '') {
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
    $('input[id^="openai_"], input[id^="azure_"], input[id^="bedrock_"], input[id^="claude_"], input[id^="github_copilot_"]').val('');
    $('select[id^="openai_"], select[id^="azure_"], select[id^="bedrock_"], select[id^="claude_"], select[id^="github_copilot_"]').val('');

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
            case 'OPENAI_COMPATIBLE':
                fieldId = 'openai-compatible-fields';
                break;
            case 'GITHUB_COPILOT':
                fieldId = 'github-copilot-fields';
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

    // OpenAI Compatible servers (LM Studio, Ollama, etc.) run locally in the user's browser context,
    // so fetch their models directly from the browser instead of routing through the Faction server.
    if (provider === 'OPENAI_COMPATIBLE') {
        loadOpenAICompatibleModelsDirect();
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
 * Fetch models directly from an OpenAI-compatible server (e.g. LM Studio) via the browser.
 * Server-side fetching won't work because the local server URL is only reachable from
 * the user's browser, not from the Faction application server.
 */
function loadOpenAICompatibleModelsDirect() {
    var baseUrl = $('#openai_compatible_baseUrl').val().trim();
    if (!baseUrl) {
        return;
    }

    var modelsUrl = baseUrl.replace(/\/+$/, '') + '/models';
    var apiKey = $('#openai_compatible_apiKey').val().trim();

    var ajaxHeaders = {};
    if (apiKey) {
        ajaxHeaders['Authorization'] = 'Bearer ' + apiKey;
    }

    $.ajax({
        url: modelsUrl,
        method: 'GET',
        headers: ajaxHeaders
    })
        .done(function (data) {
            var models = [];
            if (data && data.data && Array.isArray(data.data)) {
                // Standard OpenAI format: { data: [{ id: "..." }] }
                models = $.map(data.data, function (m) {
                    return { value: m.id, label: m.id };
                });
            } else if (data && data.models && Array.isArray(data.models)) {
                // LM Studio format: { models: [{ key: "...", display_name: "...", type: "llm" }] }
                models = $.map(data.models, function (m) {
                    if (m.type === 'embedding') return null; // skip embedding models
                    var value = m.key || m.id;
                    var label = m.display_name ? m.display_name + ' (' + value + ')' : value;
                    return { value: value, label: label };
                });
            }
            populateModelSelect('OPENAI_COMPATIBLE', models);
            if (models.length > 0) {
                $('#testResult').text('Loaded ' + models.length + ' models from server').addClass('text-success').removeClass('text-danger');
                setTimeout(function () {
                    $('#testResult').text('').removeClass('text-success text-danger');
                }, 3000);
            } else {
                $('#testResult').text('Connected but no models found').addClass('text-danger').removeClass('text-success');
            }
        })
        .fail(function () {
            $('#testResult').text('Could not reach server at ' + baseUrl).addClass('text-danger').removeClass('text-success');
            console.log('Failed to load models from: ' + modelsUrl);
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

        case 'OPENAI_COMPATIBLE':
            var compatBaseUrl = $('#openai_compatible_baseUrl').val();
            if (!compatBaseUrl || compatBaseUrl.trim() === '') return null;
            credentials.baseUrl = compatBaseUrl;
            var compatApiKey = $('#openai_compatible_apiKey').val();
            if (compatApiKey && compatApiKey.trim() !== '') {
                credentials.apiKey = compatApiKey;
            }
            break;

        case 'GITHUB_COPILOT':
            var copilotApiKey = $('#github_copilot_apiKey').val();
            if (!copilotApiKey || copilotApiKey.trim() === '') return null;
            credentials.apiKey = copilotApiKey;
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
    $.post('GetLLMConfig', {
        configId: configId,
        '_token': $('[name="_token"]').val()
    })
        .done(function (data) {
            if (data) {
                // Open the modal first so clearForm() fires before we populate,
                // then restore the ID that openConfigModal() resets to null.
                openConfigModal(true);
                currentConfigId = configId;
                populateForm(data);
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

    // Set provider and show fields WITHOUT triggering the change event, which would
    // fire loadProviderModels() asynchronously and race against setting the model value.
    $('#provider').val(config.provider || '');
    showProviderFields();

    if (config.provider) {
        var savedModel = config.model || '';

        switch (config.provider) {
            case 'OPENAI':
                $('#openai_apiKey').val(''); // Never populate password fields
                $('#openai_baseUrl').val(config.baseUrl || '');
                loadModelsAndSelect(config.id, config.provider, savedModel);
                break;

            case 'AZURE_OPENAI':
                $('#azure_apiKey').val(''); // Never populate password fields
                $('#azure_endpoint').val(config.endpoint || '');
                $('#azure_deployment').val(config.deployment || '');
                $('#azure_apiVersion').val(config.apiVersion || '');
                loadModelsAndSelect(config.id, config.provider, savedModel);
                break;

            case 'AWS_BEDROCK':
                $('#bedrock_accessKey').val(''); // Never populate password fields
                $('#bedrock_secretKey').val(''); // Never populate password fields
                $('#bedrock_region').val(config.region || '');
                loadModelsAndSelect(config.id, config.provider, savedModel);
                break;

            case 'CLAUDE':
                $('#claude_apiKey').val(''); // Never populate password fields
                loadModelsAndSelect(config.id, config.provider, savedModel);
                break;

            case 'OPENAI_COMPATIBLE':
                $('#openai_compatible_baseUrl').val(config.baseUrl || '');
                $('#openai_compatible_apiKey').val(''); // Never populate password fields
                // Inject the saved model as a selected option; user can re-load from the server
                if (savedModel) {
                    var sel = $('#openai_compatible_model');
                    sel.empty().append('<option value="' + savedModel + '">' + savedModel + '</option>');
                }
                break;

            case 'GITHUB_COPILOT':
                $('#github_copilot_apiKey').val(''); // Never populate password fields
                loadModelsAndSelect(config.id, config.provider, savedModel);
                break;
        }
    }

    $('#configActive').prop('checked', config.active === "true");
}

/**
 * Load models for a provider and select a specific value once loaded
 */
function loadModelsAndSelectbk(provider, modelToSelect) {
    $.post('GetProviderModels', {
        provider: provider,
        '_token': $('[name="_token"]').val()
    }).done(function (data) {
        populateModelSelect(provider, data);
        if (modelToSelect) {
            $('#' + provider.toLowerCase() + '_model').val(modelToSelect);
        }
    });
}
/**
 * Load models for a provider and select a specific value once loaded
 */
function loadModelsAndSelect(configId, provider, modelToSelect) {
    $.post('GetProviderModels', {
		configId: configId,
        provider: provider,
        '_token': $('[name="_token"]').val()
    }).done(function (data) {
        populateModelSelect(provider, data);
        if (modelToSelect) {
            $('#' + provider.toLowerCase() + '_model').val(modelToSelect);
        }
    });
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
        formData.configId = currentConfigId;
    }

    formData._token = $('[name="_token"]').val();

    $('#saveConfig').prop('disabled', true).html('<i class="fa fa-spinner fa-spin"></i> Saving...');

    $.post('SaveLLMConfig', formData)
        .done(function (data) {
            if (data.result === 'success') {
                $('#configModal').modal('hide');
                window.location.reload();
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
                    // errors.push('API Key is required for OpenAI');
                }
                break;

            case 'AZURE_OPENAI':
                if (!$('#azure_apiKey').val().trim()) {
                    //errors.push('API Key is required for Azure OpenAI');
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
                    //errors.push('Access Key is required for AWS Bedrock');
                }
                if (!$('#bedrock_secretKey').val().trim()) {
                    //errors.push('Secret Key is required for AWS Bedrock');
                }
                if (!$('#bedrock_region').val().trim()) {
                    errors.push('Region is required for AWS Bedrock');
                }
                break;

            case 'CLAUDE':
                if (!$('#claude_apiKey').val().trim()) {
                    //errors.push('API Key is required for Claude');
                }
                break;

            case 'OPENAI_COMPATIBLE':
                if (!$('#openai_compatible_baseUrl').val().trim()) {
                    errors.push('Base URL is required for OpenAI Compatible');
                }
                if (!$('#openai_compatible_model').val()) {
                    errors.push('Model is required — enter the Base URL to load available models');
                }
                break;

            case 'GITHUB_COPILOT':
                if (!$('#github_copilot_model').val()) {
                    errors.push('Model is required for GitHub Copilot');
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

        case 'OPENAI_COMPATIBLE':
            data.baseUrl = $('#openai_compatible_baseUrl').val();
            data.apiKey = $('#openai_compatible_apiKey').val();
            data.model = $('#openai_compatible_model').val();
            break;

        case 'GITHUB_COPILOT':
            data.apiKey = $('#github_copilot_apiKey').val();
            data.model = $('#github_copilot_model').val();
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
                $.alert({ type: 'green', title: 'Connection Successful', content: 'The connection test passed successfully.', columnClass: 'small' });
            } else {
                $.alert({ type: 'red', title: 'Connection Failed', content: data.message || 'Unable to connect. Please check your settings.', columnClass: 'small' });
            }
        })
        .fail(function () {
            $.alert({ type: 'red', title: 'Connection Failed', content: 'Request error. Please check your settings and try again.', columnClass: 'small' });
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
        configId: configId,
        '_token': $('[name="_token"]').val()
    })
        .done(function (data) {
            if (data.result === 'success') {
                $.alert({ type: 'green', title: 'Connection Successful', content: 'The connection test passed successfully.', columnClass: 'small' });
            } else {
                $.alert({ type: 'red', title: 'Connection Failed', content: data.message || 'Unable to connect. Please check your settings.', columnClass: 'small' });
            }
        })
        .fail(function () {
            $.alert({ type: 'red', title: 'Connection Failed', content: 'Request error. Please check your settings and try again.', columnClass: 'small' });
        });
}

/**
 * Delete configuration with confirmation
 */
global.deleteConfig = function deleteConfig(configId, configName) {
    $.confirm({
        title: 'Delete Configuration',
        content: 'Are you sure you want to delete <b>' + configName + '</b>?',
        type: 'red',
        columnClass: 'small',
        buttons: {
            confirm: {
                text: 'Delete',
                btnClass: 'btn-danger',
                action: function () {
                    $.post('DeleteLLMConfig', {
                        configId: configId,
                        '_token': $('[name="_token"]').val()
                    })
                        .done(function (data) {
                            if (data.result === 'success') {
                                window.location.reload();
                            } else {
                                $.alert({ type: 'red', title: 'Error', content: data.message || 'Failed to delete configuration', columnClass: 'small' });
                            }
                        })
                        .fail(function () {
                            $.alert({ type: 'red', title: 'Error', content: 'Error deleting configuration', columnClass: 'small' });
                        });
                }
            },
            cancel: function () {}
        }
    });
}

function showErrorMessage(message) {
    $.alert({ type: 'red', title: 'Error', content: message, columnClass: 'small' });
}