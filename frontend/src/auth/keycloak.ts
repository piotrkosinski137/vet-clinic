import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8180',
  realm: 'vetclinic',
  clientId: 'vetclinic-frontend',
});

export default keycloak;
