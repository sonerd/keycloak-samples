package de.keycloak.samples.favourites.api.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.security.oauth2.resource.JwtAccessTokenConverterConfigurer;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.util.CollectionUtils;

public class CustomJwtAccessTokenConverter extends DefaultAccessTokenConverter implements JwtAccessTokenConverterConfigurer {

    private static final String CLIENT_ACCESS_ROLES = "resource_access";
    private static final String REALM_ACCESS_ROLES = "realm_access";
    private static final String ROLE_ELEMENT_IN_JWT = "roles";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String USER_NAME_ATTRIBUTE = "preferred_username";

    private final ResourceServerProperties resourceServerProperties;

    public CustomJwtAccessTokenConverter(final ResourceServerProperties resourceServerProperties) {
        this.resourceServerProperties = resourceServerProperties;
    }

    @Override
    public void configure(final JwtAccessTokenConverter jwtAccessTokenConverter) {
        jwtAccessTokenConverter.setAccessTokenConverter(this);
    }

    /**
     * Spring oauth2 expects roles under authorities element in tokenMap, but keycloak provides it under
     * resource_access for client specific roles and under realm_access for realm specific roles.
     *
     * @return OAuth2Authentication with authorities for given application
     */
    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> tokenMap) {
        final OAuth2Authentication authentication = super.extractAuthentication(tokenMap);
        final OAuth2Request oAuth2Request = authentication.getOAuth2Request();

        /*
         *
         * OAuth2 expects the "aud" (audience) claim
         * inside the token (https://tools.ietf.org/id/draft-tschofenig-oauth-audience-00.html).
         *
         * You can configure the authorization-server (keycloak) to set the clientId of the resource-server
         * inside the "aud" claim (https://www.keycloak.org/docs/4.8/server_admin/index.html#_audience).
         * For this sample we don't have configured that as it is a more advanced topic.
         *
         * In case the audience setup was done, we had to set the resourceId
         * (which would be the clientId of this sample e.g. favourites-api: https://stackoverflow.com/questions/47996344/why-is-my-token-being-rejected-what-is-a-resource-id-invalid-token-does-not-c)
         * inside the ResourceServerSecurityConfigurer class which is initialized in our SecurityConfig class.
         * For that we would take the "security.oauth2.resource.id" property from our "application.yml"
         *
         * Additionally we had to get the "aud" from the token, put it into the below audiences set and then
         * pass it through the constructor of OAuth2Request.
         *
         * This would trigger the class org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
         * to check if audiences contains the clientId of your application.
         *
         * The audiences is empty for now as we don't have configured keycloak to set the clientId
         * of this application to the "aud" claim.
         */
        final Set<String> audiences = new HashSet<>();

        final List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(extractClientRoles(tokenMap, resourceServerProperties.getResourceId()));
        authorities.addAll(extractRealmRoles(tokenMap));

        final OAuth2Request request = new OAuth2Request(oAuth2Request.getRequestParameters(),
                                                        oAuth2Request.getClientId(),
                                                        authorities,
                                                        true,
                                                        oAuth2Request.getScope(),
                                                        audiences,
                                                        null,
                                                        null,
                                                        null);



        final Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(tokenMap.get(USER_NAME_ATTRIBUTE), "N/A", authorities);
        return new OAuth2Authentication(request, usernamePasswordAuthentication);
    }

    /**
     * Extracts the client specific roles which exists for the specified resourceId.
     * Keycloak puts the roles for clients into the token like this:
     *
     * <code>
     * "resource_access": {
     *      "favourites-api": { "roles": [ "USER" ] },
     *      "account": { "roles": [ "manage-account", "manage-account-links", "view-profile" ]
     *      }
     *  }
     * </code>
     *
     * @param tokenMap   token
     * @param resourceId the resource ID to get the roles for
     * @return list of roles
     */
    private List<GrantedAuthority> extractClientRoles(Map<String, ?> tokenMap, final String resourceId) {
        return Optional.ofNullable((Map<String, Object>) tokenMap.get(CLIENT_ACCESS_ROLES))
                       .map(resourceAccess -> (Map<String, Map<String, Object>>) resourceAccess.get(resourceId))
                       .map(cr -> (List<String>) cr.get(ROLE_ELEMENT_IN_JWT))
                       .map(roles -> createGrantedAuthorities(roles))
                       .orElse(Collections.emptyList());
    }

    /**
     * Extracts the realm specific roles from the given token.
     * Keycloak puts the realm roles into the token like this:
     * <code>
     * "realm_access": {
     *      "roles": [ "offline_access", "uma_authorization" ]
 *      }
     * </code>
     *
     * @param tokenMap token
     * @return realm specific roles
     */
    private List<GrantedAuthority> extractRealmRoles(Map<String, ?> tokenMap) {
        return Optional.ofNullable((Map<String, Object>) tokenMap.get(REALM_ACCESS_ROLES))
                       .map(cr -> (List<String>) cr.get(ROLE_ELEMENT_IN_JWT))
                       .map(roles -> createGrantedAuthorities(roles))
                       .orElse(Collections.emptyList());
    }

    private List<GrantedAuthority> createGrantedAuthorities(final List<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }

        return roles.stream().map(r -> new SimpleGrantedAuthority(ROLE_PREFIX + r)).collect(Collectors.toList());
    }
}
