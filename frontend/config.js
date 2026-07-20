// ============================================================================
// CONFIGURACIÓN DEL FRONTEND — completar con los valores reales de su equipo
// ============================================================================
window.APP_CONFIG = {
  // URL pública del backend (instancia EC2). Ej: "http://34.201.10.55:8080"
  apiBaseUrl: "http://localhost:8080",

  // ---- Azure AD B2C ----
  // Tenant: el subdominio antes de .onmicrosoft.com (ej. "tutenantb2c")
  b2cTenantName: "despachoapp",
  // Nombre del User Flow de registro/inicio de sesión (ej. "B2C_1_signupsignin")
  b2cSignUpSignInPolicy: "B2C_1_signupsignin",
  // Application (client) ID del registro de aplicación SPA (frontend) en Azure AD B2C
  b2cClientId: "e1e4c5fc-6696-4bc8-99d2-00e0a3b83793",
  // URI del scope expuesto por el backend (Azure AD B2C → API → "Exponer una API")
  // Ej: "https://tutenantb2c.onmicrosoft.com/plataforma-cursos-api/access_as_user"
  apiScope: "https://despachoapp.onmicrosoft.com/9e20fdd6-fafe-4e45-a1bb-298ab8f960b8/access_as_user",
  // Debe coincidir EXACTO con la "URI de redirección" configurada en el registro SPA
  redirectUri: window.location.origin,
};
