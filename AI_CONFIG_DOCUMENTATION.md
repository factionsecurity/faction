# AI Configuration Feature Documentation

## Overview

The AI Configuration feature allows administrators to securely manage Large Language Model (LLM) provider settings within the Faction platform. This feature supports multiple LLM providers with encrypted storage of API keys and credentials.

## Supported Providers

### 1. OpenAI
- **Required Fields:** API Key
- **Optional Fields:** Base URL, Model Selection
- **Models:** GPT-4, GPT-4 Turbo, GPT-3.5 Turbo, GPT-3.5 Turbo 16K

### 2. Azure OpenAI
- **Required Fields:** API Key, Endpoint, Deployment Name
- **Optional Fields:** API Version, Model Selection
- **Models:** GPT-3.5 Turbo, GPT-4, GPT-4 Turbo, GPT-4 32K

### 3. AWS Bedrock
- **Required Fields:** Access Key ID, Secret Access Key, Region
- **Optional Fields:** Model Selection
- **Models:** Claude 3 Opus, Claude 3 Sonnet, Claude 3 Haiku, Claude Instant, Claude 2

### 4. Claude (Direct)
- **Required Fields:** API Key
- **Optional Fields:** Model Selection
- **Models:** Claude 3 Opus, Claude 3 Sonnet, Claude 3 Haiku

## Architecture

### Backend Components

#### 1. LLMConfig DAO Entity (`src/com/fuse/dao/LLMConfig.java`)
- JPA entity with automatic encryption for sensitive fields
- Provider-specific validation methods
- Secure handling of API keys, access keys, and secret keys
- Audit trail with creation and modification timestamps

**Key Security Features:**
- API keys encrypted using `FSUtils.encryptPassword()`
- Masked display methods for UI security
- Configuration validation per provider requirements

#### 2. AIConfig Action Class (`src/com/fuse/actions/admin/AIConfig.java`)
- Admin-only access control via `isAcadmin()` checks
- CSRF protection with token validation
- Complete CRUD operations for LLM configurations
- Connection testing functionality
- Provider-specific validation and configuration management

**Action Methods:**
- `execute()` - Load configurations page
- `SaveLLMConfig()` - Save/update LLM configuration
- `DeleteLLMConfig()` - Remove LLM configuration
- `TestLLMConnection()` - Validate LLM connection
- `GetLLMConfig()` - Retrieve configuration for editing
- `GetProviderModels()` - Return available models for provider

### Frontend Components

#### 1. AI Config JSP (`WebContent/WEB-INF/jsp/admin/AIConfig.jsp`)
- Responsive admin interface with provider-specific forms
- Dynamic field switching based on provider selection
- DataTable for configuration management
- Modal-based configuration editing
- Real-time connection testing

#### 2. JavaScript Handler (`WebContent/dist/js/aiconfig.js`)
- Dynamic form field management
- AJAX-based CRUD operations
- Real-time validation
- Provider-specific field handling
- Secure form data collection

#### 3. JSON Response Templates
- `llmConfigJSON.jsp` - Configuration data serialization
- `providerModelsJSON.jsp` - Model selection data

## Security Implementation

### 1. Authentication & Authorization
```java
if (!(this.isAcadmin())) {
    return LOGIN;
}
```
- Restricts access to administrators only
- Consistent with existing admin patterns

### 2. CSRF Protection
```java
if (!this.testToken(false)) {
    return this.ERRORJSON;
}
```
- All state-changing operations require valid CSRF tokens

### 3. Data Encryption
```java
// Encrypted storage
public void setApiKey(String apiKey) {
    this.apiKey = FSUtils.encryptPassword(apiKey.trim());
}

// Secure retrieval
public String getApiKey() {
    return FSUtils.decryptPassword(apiKey);
}

// Masked display
public String getMaskedApiKey() {
    return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
}
```

### 4. Audit Logging
```java
AuditLog.audit(this, "LLM Configuration created: " + config.getName(), 
               AuditLog.UserAction, false);
```

## Database Schema

The LLMConfig entity creates the following table structure:

```sql
CREATE TABLE llm_config (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    api_key TEXT, -- encrypted
    base_url VARCHAR(500),
    endpoint VARCHAR(500),
    deployment VARCHAR(255),
    access_key TEXT, -- encrypted  
    secret_key TEXT, -- encrypted
    region VARCHAR(100),
    model VARCHAR(255),
    api_version VARCHAR(50),
    active BOOLEAN DEFAULT true,
    created_date TIMESTAMP,
    modified_date TIMESTAMP
);
```

## User Interface

### Navigation
- Located under **Admin** → **AI Config** (admin users only)
- Uses robot icon for clear visual identification

### Configuration Management
- **Add Configuration:** Modal form with provider selection
- **Edit Configuration:** Pre-populated form with masked credentials
- **Test Connection:** Real-time connectivity validation
- **Delete Configuration:** Confirmation-protected deletion

### Dynamic Forms
- Provider selection automatically shows/hides relevant fields
- Model dropdowns populated based on provider selection
- Real-time form validation with user feedback

## Usage Workflow

### 1. Adding New Configuration
1. Navigate to Admin → AI Config
2. Click "Add LLM Config"
3. Enter configuration name
4. Select provider (OpenAI, Azure OpenAI, AWS Bedrock, or Claude)
5. Fill in provider-specific required fields
6. Optionally select a model
7. Test connection (recommended)
8. Save configuration

### 2. Testing Configurations
1. Use "Test Connection" button during configuration
2. Click plug icon on existing configurations for quick tests
3. Receive immediate feedback on connectivity status

### 3. Managing Configurations
- **Edit:** Click edit icon to modify existing configurations
- **Delete:** Click trash icon with confirmation prompt
- **Status:** Toggle active/inactive status
- **Audit:** All changes are logged for compliance

## Integration Points

### 1. Authentication System
- Leverages existing `FSActionSupport` base class
- Uses established admin role checking (`isAcadmin()`)
- Integrates with session management

### 2. Security Framework
- Utilizes existing CSRF protection mechanisms
- Leverages `FSUtils` encryption utilities
- Follows established audit logging patterns

### 3. Frontend Framework
- Uses existing Bootstrap/AdminLTE styling
- Integrates with established modal patterns
- Leverages DataTables for consistent UX

### 4. Navigation System
- Added to existing admin sidebar structure
- Follows established navigation patterns
- Conditional visibility based on admin role

## Future Enhancements

### 1. Connection Health Monitoring
- Periodic health checks for active configurations
- Dashboard indicators for connection status
- Automated alerts for connection failures

### 2. Configuration Templates
- Pre-configured templates for common providers
- Quick setup wizards for standard configurations

### 3. Usage Analytics
- Track configuration usage patterns
- Performance metrics for different providers
- Cost analysis and optimization recommendations

### 4. Advanced Security Features
- Configuration access logging
- Role-based configuration management
- Integration with external secret management systems

## Troubleshooting

### Common Issues

#### 1. Connection Test Failures
- **OpenAI:** Verify API key and base URL
- **Azure:** Check endpoint format and deployment name
- **AWS:** Confirm access keys and region
- **Claude:** Validate API key format

#### 2. Configuration Not Saving
- Check admin permissions
- Verify CSRF token is present
- Ensure required fields are populated

#### 3. Provider Fields Not Showing
- Check JavaScript console for errors
- Verify provider selection is working
- Ensure all CSS/JS files are loaded

## Files Created/Modified

### New Files
- `src/com/fuse/dao/LLMConfig.java` - Main DAO entity
- `src/com/fuse/actions/admin/AIConfig.java` - Action controller
- `WebContent/WEB-INF/jsp/admin/AIConfig.jsp` - Main UI page
- `WebContent/WEB-INF/jsp/admin/llmConfigJSON.jsp` - JSON response template
- `WebContent/WEB-INF/jsp/admin/providerModelsJSON.jsp` - Models JSON template
- `WebContent/dist/js/aiconfig.js` - Frontend JavaScript
- `AI_CONFIG_DOCUMENTATION.md` - This documentation

### Modified Files
- `WebContent/WEB-INF/jsp/header.jsp` - Added sidebar navigation item

## Security Considerations

### 1. Credential Management
- All API keys and secrets are encrypted at rest
- Credentials are never displayed in full in the UI
- Password fields are not pre-populated during edits

### 2. Access Control
- Feature restricted to administrators only
- All operations require valid authentication
- CSRF protection on all state-changing operations

### 3. Audit Trail
- All configuration changes are logged
- User attribution for all operations
- Timestamp tracking for compliance

This comprehensive AI Configuration feature provides secure, user-friendly management of LLM provider settings while maintaining the security and usability standards of the Faction platform.