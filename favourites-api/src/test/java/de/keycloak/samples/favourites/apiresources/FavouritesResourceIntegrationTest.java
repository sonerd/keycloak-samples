package de.keycloak.samples.favourites.apiresources;

import java.util.Collections;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.shaded.org.apache.commons.lang.SystemUtils;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FavouritesResourceIntegrationTest {

    private static final String CLIENT_SECRET = "489c2011-1884-4473-9ee1-23836e25625e";
    private static final String CLIENT_ID = "favourites-api";
    private static GenericContainer container = new GenericContainer(new ImageFromDockerfile().withFileFromClasspath("test-realm.json", "test-realm.json")
                                                                                              .withFileFromClasspath("test-standalone.xml", "test-standalone.xml")
                                                                                              .withFileFromClasspath("Dockerfile", "integration-test.docker"))
                                                                                              .waitingFor(Wait.forHttp("/auth").forStatusCode(200));

    @Value("${sso.token-uri}")
    String tokenEndpoint;

    @Autowired
    MockMvc mockMvc;

    private RestTemplate rest = new RestTemplate();

    @BeforeClass
    public static void setUpClass() {
        container.setPortBindings(Collections.singletonList("8080:8080"));
        container.start();
        System.setProperty("sso.host", getHost());
    }

    @AfterClass
    public static void tearDownClass() {
        container.stop();
    }

    private static String getHost() {
        return SystemUtils.IS_OS_LINUX ? "http://" + container.getContainerIpAddress() + ":" + container.getMappedPort(8080) : "http://localhost:8080";
    }

    @Test
    public void shouldReturnFavouritesForAuthorizedUserWithValidToken() throws Exception {

        String username = "tom";
        String token = this.getTokenByResourceOwnerGrant(CLIENT_ID, CLIENT_SECRET, username, "password");
        this.mockMvc.perform(get("/favourites/" + username).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[" +
                                              "    {" +
                                              "        \"id\": \"123456\"," +
                                              "        \"rating\": 5," +
                                              "        \"comment\": \"Nice restaurant\"," +
                                              "        \"userName\": \"tom\"" +
                                              "    }" +
                                              "]"));

        username = "tim";
        token = this.getTokenByResourceOwnerGrant(CLIENT_ID, CLIENT_SECRET, username, "password");

        this.mockMvc.perform(get("/favourites/" + username).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[" +
                                              "    {" +
                                              "        \"id\": \"225588\"," +
                                              "        \"rating\": 2," +
                                              "        \"comment\": \"Bad restaurant\"," +
                                              "        \"userName\": \"tim\"" +
                                              "    }" +
                                              "]"));

    }

    @Test
    public void shouldReturn403WhenUsernameAndTokenDoesNotMatch() throws Exception {
        final String token = this.getTokenByResourceOwnerGrant(CLIENT_ID, CLIENT_SECRET, "tom", "password");
        this.mockMvc.perform(get("/favourites/tim").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403ForNotAuthorizedUser() throws Exception {
        final String username = "john";
        final String token = this.getTokenByResourceOwnerGrant(CLIENT_ID, CLIENT_SECRET, username, "password");

        this.mockMvc.perform(get("/favourites/" + username).header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn401ForMissingAuthorizationHeader() throws Exception {
        this.mockMvc.perform(get("/favourites/tom")).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnAllFavouritesOnlyWhenUserHasAdminRole() throws Exception {
        final String username = "admin";
        final String token = this.getTokenByResourceOwnerGrant(CLIENT_ID, CLIENT_SECRET, username, "password");

        this.mockMvc.perform(get("/favourites").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[" +
                                              "    {" +
                                              "        \"id\": \"225588\"," +
                                              "        \"rating\": 2," +
                                              "        \"comment\": \"Bad restaurant\"," +
                                              "        \"userName\": \"tim\"" +
                                              "    }" +
                                              "," +
                                              "    {" +
                                              "         \"id\": \"123456\"," +
                                              "         \"rating\": 5, " +
                                              "          \"comment\": \"Nice restaurant\"," +
                                              "          \"userName\": \"tom\"" +
                                              "     }" +
                                              "]"));
    }

    @Test
    public void shouldReturn403WhenUserWithoutAdminRoleTriesToGetAllFavourites() throws Exception {
        final String username = "john";
        final String token = this.getTokenByResourceOwnerGrant(CLIENT_ID, CLIENT_SECRET, username, "password");

        this.mockMvc.perform(get("/favourites").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    private String getTokenByResourceOwnerGrant(String clientId, String clientSecret, String userName, String password) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", userName);
        map.add("password", password);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = this.rest.postForEntity(tokenEndpoint, request, Map.class);

        return (String) response.getBody().get("access_token");
    }
}