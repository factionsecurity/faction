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

import java.util.Optional;

import javax.persistence.EntityManager;

public class SecurityConfigFactory implements ConfigFactory {
    private static final OidcClient oidcClient = new OidcClient();
    
	public static OidcClient getOidcClientInstance() {
		return oidcClient;
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
		em.close();
		oidcClient.setConfiguration(oidcConfiguration);
		oidcClient.setAuthorizationGenerator((ctx, profile) -> {
			profile.addRole("ROLE_USER");
			return Optional.of(profile);
		});

        final Clients clients = new Clients(System.getenv("FACTION_OAUTH_CALLBACK")+ "/oauth/callback",
                oidcClient,  new AnonymousClient());
        return new Config(clients);
    }
    
}