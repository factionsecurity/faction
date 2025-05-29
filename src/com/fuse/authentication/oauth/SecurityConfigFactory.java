package com.fuse.authentication.oauth;

import com.fuse.dao.HibHelper;
import com.fuse.dao.SystemSettings;

import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.net.MalformedURLException;
import java.util.Optional;

import javax.persistence.EntityManager;

public class SecurityConfigFactory implements ConfigFactory {
    private static volatile OidcClient oidcClient = new OidcClient();
    private static volatile SAML2Client saml2Client = new SAML2Client();
    private static volatile Config currentConfig;
    
	public static OidcClient getOidcClientInstance() {
		return oidcClient;
	}
	public static SAML2Client getSAML2ClientInstance() {
		return saml2Client;
	}
	
	public static Config getCurrentConfig() {
        if (currentConfig == null) {
            synchronized (SecurityConfigFactory.class) {
                if (currentConfig == null) {
                    currentConfig = new SecurityConfigFactory().build();
                }
            }
        }
        return currentConfig;
    }
	
	public static void refreshConfig() {
		currentConfig = new SecurityConfigFactory().build();
	}

    @Override
    public Config build(final Object... parameters) {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		SystemSettings ss = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
					.findFirst().orElse(null);
		if(ss == null) {
			ss = new SystemSettings();
		}
		
		Clients clients = ss.updateSSOClients(oidcClient, saml2Client);
		currentConfig = new Config(clients);
		em.close();
        
        return currentConfig;
    }
    
}