# Samples

## Favourites API
The sample favourite API (resource server in context of OAuth) shows how a backend application can be secured with _Bearer Tokens_ issued by keycloak (authorisation server in context of OAuth).
The sample has an `/favourites/{username}` endpoint which returns the favourites for the specified user.
To call this endpoint an access token which includes a specific role is required. 
The access token can be retrieved by calling the token endpoint provided by keycloak:

POST the `client_id`, `grant_type`, `client_secret`, `username` and `password` as form parameters to following endpoint:

`{{AUTH_URL}}/auth/realms/my-apps/protocol/openid-connect/token`

The response will include an `access_token` which can be copied and passed as `Authorization Bearer <access_token>` 
header parameter to call `/favourites/{username}`.

This project also includes an integration test `FavouritesResourceIntegrationTest` based on `testcontainers` (www.testcontainers.org). 
The integration test is preparing the keycloak with a given realm setting which includes the needed realm, clients, 
roles and users to test the authentication and authorization. You can run `FavouritesResourceIntegrationTest` on your local machine.

The security setup of the project is done by using `spring-security`.

## Favourites App
This app includes a frontend (public client) secured by keycloak. In case you open a secured resource in the browser you will redirected to keycloak to login.
After successful login you will be redirected back to the requested resource.

The security setup of the project is done by using the keycloak adapters for `spring-boot` and `spring-security`.

### How to run
To run the samples just execute the `run.sh` which will start keycloak with the needed realm settings, the `favourites-app` and the `favourites-api`. All applications are behind a `nginx` reverse proxy.
- To open keycloak admin console go to `http://auth.my-company.com/auth/admin/master/console/#/realms/my-apps` in your browser.
- To open the favourites-app go to `http://favourites-app.my-company.com` in your browser. When you click the `My restaurants` you will be redirected to keycloak for login.
- The user `peter/password` is allowed to access the requested page
- The user `tom/password` is not allowed to access the requested page which result in a redirection to a custom access denied page  
