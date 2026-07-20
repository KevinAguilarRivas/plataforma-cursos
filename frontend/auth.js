// ============================================================================
// AUTENTICACIÓN — Azure AD B2C via MSAL Browser
// ============================================================================
const cfg = window.APP_CONFIG;

const authority = `https://${cfg.b2cTenantName}.b2clogin.com/${cfg.b2cTenantName}.onmicrosoft.com/${cfg.b2cSignUpSignInPolicy}`;
const knownAuthority = `${cfg.b2cTenantName}.b2clogin.com`;

const msalConfig = {
  auth: {
    clientId: cfg.b2cClientId,
    authority: authority,
    knownAuthorities: [knownAuthority],
    redirectUri: cfg.redirectUri,
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false,
  },
};

const msalInstance = new msal.PublicClientApplication(msalConfig);
let activeAccount = null;

async function initAuth() {
  await msalInstance.initialize();
  const response = await msalInstance.handleRedirectPromise();
  if (response) {
    activeAccount = response.account;
  } else {
    const accounts = msalInstance.getAllAccounts();
    if (accounts.length > 0) activeAccount = accounts[0];
  }
  return activeAccount;
}

function login() {
  msalInstance.loginRedirect({ scopes: [cfg.apiScope] });
}

function logout() {
  msalInstance.logoutRedirect();
}

async function getAccessToken() {
  if (!activeAccount) throw new Error("No hay sesión activa");
  try {
    const result = await msalInstance.acquireTokenSilent({
      scopes: [cfg.apiScope],
      account: activeAccount,
    });
    return result.accessToken;
  } catch (e) {
    // Si el token silencioso falla (expiró la sesión), se relanza el login
    await msalInstance.acquireTokenRedirect({ scopes: [cfg.apiScope] });
    throw e;
  }
}

// Decodifica el JWT (sin validar firma, solo para MOSTRAR el rol en el frontend;
// la validación real de firma/emisor/expiración ocurre en el backend)
function decodeJwtClaims(token) {
  try {
    const payload = token.split(".")[1];
    const json = decodeURIComponent(
      atob(payload.replace(/-/g, "+").replace(/_/g, "/"))
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(json);
  } catch (e) {
    return {};
  }
}
