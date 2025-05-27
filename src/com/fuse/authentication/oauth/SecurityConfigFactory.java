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
    private static final OidcClient oidcClient = new OidcClient();
    private static final SAML2Client saml2Client = new SAML2Client();
    
	public static OidcClient getOidcClientInstance() {
		return oidcClient;
	}
	public static SAML2Client getSAML2ClientInstance() {
		return saml2Client;
	}
    

    @Override
    public Config build(final Object... parameters) {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		SystemSettings ss = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
					.findFirst().orElse(null);
		if(ss == null) {
			ss = new SystemSettings();
		}
		final OidcConfiguration oidcConfiguration = ss.getOdicConfig();
		SAML2Configuration saml2Configuration;
		try {
			saml2Configuration = ss.getSAML2Config();
			saml2Client.setConfiguration(saml2Configuration);
			saml2Client.setAuthorizationGenerator((ctx, profile) -> {
				profile.addRole("ROLE_USER");
				return Optional.of(profile);
			});
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		em.close();
		oidcClient.setConfiguration(oidcConfiguration);
		oidcClient.setAuthorizationGenerator((ctx, profile) -> {
			profile.addRole("ROLE_USER");
			return Optional.of(profile);
		});

        final Clients clientsbk = new Clients(System.getenv("FACTION_OAUTH_CALLBACK")+ "/oauth/callback",
                oidcClient,  new AnonymousClient());
        
        final Clients clients = new Clients(System.getenv("FACTION_OAUTH_CALLBACK")+ "/saml2/callback",
                saml2Client,  new AnonymousClient());
        return new Config(clients);
    }
    
}