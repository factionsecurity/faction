package com.fuse.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.core.client.direct.AnonymousClient;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

import com.fuse.authentication.oauth.SecurityFilterWrapper;
import com.fuse.utils.FSUtils;
import com.nimbusds.jose.JWSAlgorithm;

@Entity
public class SystemSettings {

	private String server;
	private String port;
	private String uname;
	private String fromAddress;
	private String password;
	private String type;
	private Boolean ssl;
	private Integer critical;
	private Integer high;
	private Integer medium;
	private Integer low;
	private Boolean trackMedium;
	private Integer critAlert;
	private Integer highAlert;
	private Integer mediumAlert;
	private Integer lowAlert;
	private String prefix = "Faction: ";
	private String signature = "Thanks,<br>FACTION";
	private Boolean imported;
	private String tmp;
	private String wkhtmltopdf;
	private String webport;
	private Boolean emailSSL;
	private Boolean tls;
	private Boolean emailAuth;
	private Boolean peerreview;
	private Boolean selfPeerReview;
	private Boolean enablefeed;
	private String boldTitle = "community";
	private String otherTitle = "FACTION";
	private Long verificationOption;
	private Boolean enableRandAppId;
	private Boolean ssoEnabled;
	private String oauthClientId;
	private String oauthClientSecret;
	private String oauthDiscoveryURI;
	private Integer inactiveDays;
	@ElementCollection
	private List<String> status = new ArrayList<String>();
	private String defaultStatus;
	private String defaultRating;
	private String ldapURL;
	private String ldapBaseDn;
	private String ldapBindDn;
	private String ldapPassword;
	private String ldapSecurity;
	private String ldapSearchDn;
	private Boolean ldapInsecureSSL=false;
	private String ldapObjectClass;
	private String features;
	
	
	public void initSMTPSettings() {
		if(this.server == null || this.server == "") {
			this.setServer(FSUtils.getEnv("FACTION_SMTP_SERVER"));
			this.setPort(FSUtils.getEnv("FACTION_SMTP_PORT"));
			this.setTls(true);
			this.setUname(FSUtils.getEnv("FACTION_SMTP_USER"));
			this.setFromAddress(FSUtils.getEnv("FACTION_SMTP_FROM_ADDRESS"));
			this.setPassword(FSUtils.encryptPassword(FSUtils.getEnv("FACTION_SMTP_PASSWORD")));
			this.setEmailSSL(false);
			this.setEmailAuth(true);
			this.setType("smtp");
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "emsGen")
	@TableGenerator(name = "emsGen", table = "emsGenseq", pkColumnValue = "ems", valueColumnName = "nextEms", initialValue = 1, allocationSize = 1)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServer() {
		return server == null? "" : server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public Integer getCritical() {
		if (critical == null)
			return 50;
		else
			return critical;
	}

	public void setCritical(Integer critical) {
		this.critical = critical;
	}

	public Integer getHigh() {
		if (high == null)
			return 100;
		else
			return high;
	}

	public void setHigh(Integer high) {
		this.high = high;
	}

	public Integer getMedium() {
		if (medium == null)
			return Integer.MAX_VALUE;
		else
			return medium;
	}

	public void setMedium(Integer medium) {
		this.medium = medium;
	}

	public Integer getLow() {
		if (low == null) {
			return Integer.MAX_VALUE;
		} else
			return low;
	}

	public void setLow(Integer low) {
		this.low = low;
	}

	public Boolean isTrackMedium() {
		return trackMedium;
	}

	public void setTrackMedium(Boolean trackMedium) {
		this.trackMedium = trackMedium;
	}

	public Integer getCritAlert() {
		if (critAlert == null)
			return 30;
		else
			return critAlert;
	}

	public void setCritAlert(Integer critAlert) {
		this.critAlert = critAlert;
	}

	public Integer getHighAlert() {
		if (highAlert == null)
			return 70;
		else
			return highAlert;
	}

	public void setHighAlert(Integer highAlert) {
		this.highAlert = highAlert;
	}

	public Integer getMediumAlert() {
		if (mediumAlert == null)
			return Integer.MAX_VALUE;
		else
			return mediumAlert;
	}

	public void setMediumAlert(Integer mediumAlert) {
		this.mediumAlert = mediumAlert;
	}

	public Integer getLowAlert() {
		if (lowAlert == null)
			return Integer.MAX_VALUE;
		else
			return lowAlert;
	}

	public void setLowAlert(Integer lowAlert) {
		this.lowAlert = lowAlert;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Boolean isImported() {
		return imported;
	}

	public void setImported(Boolean imported) {
		this.imported = imported;
	}

	public String getTmp() {
		return tmp;
	}

	public void setTmp(String tmp) {
		this.tmp = tmp;
	}

	public String getWkhtmltopdf() {
		return wkhtmltopdf;
	}

	public void setWkhtmltopdf(String wkhtmltopdf) {
		this.wkhtmltopdf = wkhtmltopdf;
	}

	public String getWebport() {
		return webport;
	}

	public void setWebport(String webport) {
		this.webport = webport;
	}

	public Boolean getTrackMedium() {
		return trackMedium;
	}

	public Boolean getEmailSSL() {
		return emailSSL;
	}

	public void setEmailSSL(Boolean emailSSL) {
		this.emailSSL = emailSSL;
	}

	public Boolean getTls() {
		return tls;
	}

	public void setTls(Boolean tls) {
		this.tls = tls;
	}

	public Boolean getEmailAuth() {
		return emailAuth;
	}

	public void setEmailAuth(Boolean emailAuth) {
		this.emailAuth = emailAuth;
	}

	public Boolean getPeerreview() {
		return peerreview;
	}

	public void setPeerreview(Boolean peerreview) {
		this.peerreview = peerreview;
	}

	public Boolean getEnablefeed() {
		return enablefeed;
	}

	public void setEnablefeed(Boolean enablefeed) {
		this.enablefeed = enablefeed;
	}

	public String getBoldTitle() {
		return boldTitle;
	}

	public String getOtherTitle() {
		return otherTitle;
	}

	public void setBoldTitle(String boldTitle) {
		this.boldTitle = boldTitle;
	}

	public void setOtherTitle(String otherTitle) {
		this.otherTitle = otherTitle;
	}

	public Long getVerificationOption() {
		return verificationOption;
	}

	public void setVerificationOption(Long verificationOption) {
		this.verificationOption = verificationOption;
	}

	public Boolean getEnableRandAppId() {
		return enableRandAppId;
	}

	public void setEnableRandAppId(Boolean enableRandAppId) {
		this.enableRandAppId = enableRandAppId;
	}

	public String getOauthClientId() {
		return oauthClientId;
	}

	public String getOauthClientSecret() {
		return oauthClientSecret;
	}

	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}

	public void setOauthClientSecret(String oauthClientSecret) {
		this.oauthClientSecret = oauthClientSecret;
	}

	public Boolean getSsoEnabled() {
		return ssoEnabled == null ? false : ssoEnabled;
	}

	public void setSsoEnabled(Boolean ssoEnabled) {
		this.ssoEnabled = ssoEnabled;
	}

	public Integer getInactiveDays() {
		return inactiveDays == null ? 0 : inactiveDays;
	}

	public void setInactiveDays(Integer inactiveDays) {
		this.inactiveDays = inactiveDays;
	}

	public Boolean getImported() {
		return imported;
	}

	public List<String> getStatus() {
		return status;
	}

	public void setStatus(List<String> status) {
		this.status = status;
	}

	public String getDefaultStatus() {
		return defaultStatus;
	}

	public void setDefaultStatus(String defaultStatus) {
		this.defaultStatus = defaultStatus;
	}
	public String getDefaultRating() {
		return defaultRating;
	}

	public void setDefaultRating(String defaultRating) {
		this.defaultRating = defaultRating;
	}

	public String getLdapURL() {
		return ldapURL;
	}

	public void setLdapURL(String ldapURL) {
		this.ldapURL = ldapURL;
	}

	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
	}

	public String getLdapBindDn() {
		return ldapBindDn;
	}

	public void setLdapBindDn(String ldapBindDn) {
		this.ldapBindDn = ldapBindDn;
	}

	public String getLdapPassword() {
		return ldapPassword;
	}

	public void setLdapSecurity(String ldapSecurity) {
		this.ldapSecurity = ldapSecurity;
	}

	public String getLdapSecurity() {
		return ldapSecurity;
	}

	public void setLdapSearchDn(String ldapSearchDn) {
		this.ldapSearchDn = ldapSearchDn;
	}
	public String getLdapSearchDn() {
		return ldapSearchDn;
	}

	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}

	public Boolean getLdapInsecureSSL() {
		return ldapInsecureSSL;
	}

	public void setLdapInsecureSSL(Boolean ldapInsecureSSL) {
		this.ldapInsecureSSL = ldapInsecureSSL;
	}

	public String getLdapObjectClass() {
		return ldapObjectClass;
	}

	public void setLdapObjectClass(String ldapObjectClass) {
		this.ldapObjectClass = ldapObjectClass;
	}
	public void setOauthDiscoveryURI(String uri) {
		this.oauthDiscoveryURI = uri;
	}
	
	public String getOauthDiscoveryURI() {
		return this.oauthDiscoveryURI;
	}
	
	public void setSelfPeerReview(Boolean selfPeerReview) {
		this.selfPeerReview = selfPeerReview;
	}
	
	public Boolean getSelfPeerReview() {
		return this.selfPeerReview;
	}
	
	public String getFeatures() {
		return this.features;
	}
	
	public void setFeatures(String features) {
		this.features = features;
	}


	@Transient
	public OidcConfiguration getOdicConfig() {
		OidcConfiguration config = new OidcConfiguration();
		config.setClientId(this.oauthClientId==null?"":this.oauthClientId);
		config.setSecret(this.oauthClientSecret==null?"":FSUtils.decryptPassword(oauthClientSecret));
		config.setDiscoveryURI(this.oauthDiscoveryURI==null?"":this.oauthDiscoveryURI);
        config.setUseNonce(true);
        config.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
        //config.setMaxAge(10);
        config.addCustomParam("display", "popup");
        //config.addCustomParam("prompt", "select_account");
				
        
        return config;
	}
	
	@Transient
	public void updateOdicFilter() {
		OidcClient oidcClient = new OidcClient();
		
        oidcClient.setConfiguration(getOdicConfig());
        oidcClient.setAuthorizationGenerator((ctx, profile) -> {
            profile.addRole("ROLE_USER");
            return Optional.ofNullable(profile);
        });
        Clients clients = new Clients(System.getenv("FACTION_OAUTH_CALLBACK")+ "/oauth/callback",
                oidcClient, new AnonymousClient());
		SecurityFilterWrapper.getInstance().setConfigOnly(new Config(clients));
	}
	
	@Transient
	public SAML2Configuration getSAML2Config() throws MalformedURLException {
		SAML2Configuration config = new SAML2Configuration(
				new FileSystemResource(new File("/Users/joshsummitt/Code/faction-all/free/faction/samlKeystore.jks").getAbsolutePath()),
			    "pac4j-demo-passwd",
			    "pac4j-demo-passwd",
			    new UrlResource("https://login.microsoftonline.com/edc3de58-7bfc-4c22-ba4f-1d3c13e92aa5/federationmetadata/2007-06/federationmetadata.xml?appid=94b55130-89a1-4450-93aa-b997d4666a1d"));
		 config.setAuthnRequestSigned(true);  // Azure requires signed Authn requests
	     config.setWantsAssertionsSigned(true);
	     config.setForceAuth(true);
	     config.setAcceptedSkew(120);
        return config;
	}
	
	@Transient
	public void updateSAML2Filter() {
		try {
			SAML2Client samlClient = new SAML2Client(getSAML2Config());
			
			samlClient.setAuthorizationGenerator((ctx, profile) -> {
				profile.addRole("ROLE_USER");
				return Optional.ofNullable(profile);
			});
			Clients clients = new Clients(System.getenv("FACTION_OAUTH_CALLBACK")+ "/saml/callback",
					samlClient, new AnonymousClient());
			SecurityFilterWrapper.getInstance().setConfigOnly(new Config(clients));
		}catch(Exception ex) {
			System.out.println(ex);
		}
	}
	
	@Transient
	public List<String> getRatings() {
		return new ArrayList<>(Arrays.asList( new String[]{"Native", "CVSS 3.1"}));
	}
	

}
