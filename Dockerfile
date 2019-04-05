FROM jboss/keycloak:4.8.2.Final
ENV KEYCLOAK_HOME /opt/jboss/keycloak

COPY etc/themes/my $KEYCLOAK_HOME/themes/my/
COPY ./etc/realm-with-all-samples.json /tmp/sample-realm.json
COPY ./etc/test-standalone.xml $KEYCLOAK_HOME/standalone/configuration/standalone.xml


EXPOSE 8080 8787

CMD ["--debug", "-c", "standalone.xml"]
