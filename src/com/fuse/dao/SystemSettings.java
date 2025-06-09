package com.fuse.dao;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.UrlResource;

import com.fuse.authentication.oauth.SecurityConfigFactory;
import com.fuse.authentication.oauth.SecurityFilterWrapper;
import com.fuse.utils.FSUtils;
import com.nimbusds.jose.JWSAlgorithm;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;


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
	private String saml2MetaUrl;
	private String keystore;
	private String keystorePassword;
	
	
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
	
	public String getSaml2MetaUrl() {
		return this.saml2MetaUrl;
	}
	
	public void setSaml2MetaUrl(String saml2MetaUrl) {
		this.saml2MetaUrl = saml2MetaUrl;
	}
	
	public void setKeystorePassword(String password) {
		this.keystorePassword = FSUtils.encryptPassword(password);
	}
	
	public String getKeystorePassword() {
		return FSUtils.decryptPassword(this.keystorePassword);
	}
	
	/*@Transient
	public void updateSSOFilters() {
		OidcClient oidcClient = new OidcClient();
		SAML2Client saml2Client = new SAML2Client();
		Clients clients = updateSSOClients(oidcClient, saml2Client);
		SecurityFilterWrapper.getInstance().setConfigOnly(new Config(clients));
		//SecurityConfigFactory.refreshConfig();
		
	}*/
	@Transient
	public Clients updateSSOClients( OidcClient oidcClient, SAML2Client saml2Client) {
		LinkedList<Client> clients = new LinkedList<>();
		try {
			oidcClient.setConfiguration(getOdicConfig());
			oidcClient.setAuthorizationGenerator((ctx, profile) -> {
				profile.addRole("ROLE_USER");
				return Optional.ofNullable(profile);
			});
			oidcClient.setCallbackUrl(System.getenv("FACTION_OAUTH_CALLBACK")+ "/oauth/callback");
			oidcClient.init();
			
			clients.add(oidcClient);
		}catch(Exception ex) {
			System.out.println(ex);
		}
		try {
			saml2Client.setConfiguration(getSAML2Config());
			saml2Client.setAuthorizationGenerator((ctx, profile) -> {
				profile.addRole("ROLE_USER");
				return Optional.ofNullable(profile);
			});
			
			saml2Client.setCallbackUrl(System.getenv("FACTION_OAUTH_CALLBACK")+ "/saml2/callback");
			//saml2Client.init();
			clients.add(saml2Client);
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		Clients configuredClients = new Clients();
		configuredClients.setClients(clients);
		configuredClients.setCallbackUrl(System.getenv("FACTION_OAUTH_CALLBACK")+ "/oauth/callback");
		return configuredClients;
		
		
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
        //config.init();
        return config;
	}
	
	
	
	@Transient
	public SAML2Configuration getSAML2Config() throws Exception {
		if(this.keystore == null) {
			createKeystoreIfNotExists();
		}
		SAML2Configuration config = new SAML2Configuration(
				new ByteArrayResource(this.getKeyStore()),
				this.getKeystorePassword(),
				this.getKeystorePassword(),
				new UrlResource(this.saml2MetaUrl==null?"":this.saml2MetaUrl));
		 config.setServiceProviderEntityId(System.getenv("FACTION_OAUTH_CALLBACK")+ "/saml2/callback");
		 config.setAuthnRequestSigned(true);  // Azure requires signed Authn requests
		 config.setWantsAssertionsSigned(true);
		 config.setForceAuth(true);
		 config.setAcceptedSkew(120);
		 config.setCallbackUrl(System.getenv("FACTION_OAUTH_CALLBACK")+ "/saml2/callback");
		 config.init();
		return config;
	}
	
	
	@Transient
	public List<String> getRatings() {
		return new ArrayList<>(Arrays.asList( new String[]{"Native", "CVSS 3.1"}));
	}
	
	@Transient
	public void createKeystoreIfNotExists(){
		if(this.keystore == null) {
			try {
				String randomPass = UUID.randomUUID().toString();
				this.setKeystorePassword(randomPass);
				
				KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
				keyPairGen.initialize(2048, new SecureRandom());
				KeyPair keyPair = keyPairGen.generateKeyPair();

				X509Certificate cert = generateSelfSignedCert(keyPair);

				char[] password = randomPass.toCharArray();
				KeyStore keyStore = KeyStore.getInstance("JKS");
				keyStore.load(null, null);
				keyStore.setKeyEntry("pac4j", keyPair.getPrivate(), password, new java.security.cert.Certificate[]{cert});

				ByteArrayOutputStream keystoreOutput = new ByteArrayOutputStream();
				keyStore.store(keystoreOutput, password);
				
				this.keystore = FSUtils.encryptBytes(keystoreOutput.toByteArray());
			}catch(Exception e) {
				System.out.println(e);
				this.keystore=null;
			}
		}
		
	}
	@Transient
	public byte [] getKeyStore() {
		return FSUtils.decryptBytes(this.keystore);
	}
	
	@Transient
	 private static X509Certificate generateSelfSignedCert(KeyPair keyPair) throws Exception {

	        
	        Security.addProvider(new BouncyCastleProvider());

	        // Certificate subject and issuer (same for self-signed)
	        X500Name dnName = new X500Name("CN=faction-saml, OU=faction, O=faction,  L=City, ST=CA, C=US");

	        // Validity period
	        Date startDate = new Date();
	        Date endDate = new Date(startDate.getTime() + (365L * 24 * 60 * 60 * 1000)); // 1 year

	        // Serial number
	        BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis()));

	        // Create certificate builder
	        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
	                dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

	        // Content signer
	        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
	        		.setProvider("BC")
	                .build(keyPair.getPrivate());

	        // Build the certificate
	        X509Certificate certificate = new JcaX509CertificateConverter()
	                .setProvider("BC")
	                .getCertificate(certBuilder.build(contentSigner));
 
	        
	        

	        return certificate;
	    }
	

}
