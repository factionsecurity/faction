package com.fuse.authentication.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.dao.HibHelper;
import com.fuse.dao.SystemSettings;
import com.nimbusds.jose.JWSAlgorithm;

import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.config.signature.RSASignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.util.JWKHelper;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.OidcProfile;

import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class SecurityConfigFactory implements ConfigFactory {
    private final JwtAuthenticator jwtAuthenticator = new JwtAuthenticator();
    private final ObjectMapper mapper = new ObjectMapper();
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
			return profile;
		});

        final Clients clients = new Clients(System.getenv("FACTION_OAUTH_CALLBACK")+ "/oauth/callback",
                oidcClient,  new AnonymousClient());
        return new Config(clients);
    }
    
}