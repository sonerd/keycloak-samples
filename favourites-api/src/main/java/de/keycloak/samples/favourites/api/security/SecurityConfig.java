package de.keycloak.samples.favourites.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

@Configuration
@EnableResourceServer
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private ResourceServerProperties resourceServerProperties;

    @Value("${security.oauth2.resource.jwk.key-set-uri}")
    private String jwkUrl;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        DefaultTokenServices dts = new DefaultTokenServices();
        dts.setTokenStore(tokenStore());
        dts.setSupportRefreshToken(true);

        // resources.resourceId(resourceServerProperties.getResourceId());
        // setting this causes spring to check for the "aud" claim of the token
        // in the setup of keycloak for this sample we don't have configured the audience to be set for the client (resource-server)

        resources.tokenServices(dts);
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http.sessionManagement()
            // as it is a backend service it should have stateless session
            .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()).and()
            // we will have authorization on method level
            .authorizeRequests().anyRequest().permitAll();
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwkTokenStore(jwkUrl, customJwtAccessTokenConverter(), null);
    }

    @Bean
    public CustomJwtAccessTokenConverter customJwtAccessTokenConverter() {
        return new CustomJwtAccessTokenConverter(resourceServerProperties);
    }
}
