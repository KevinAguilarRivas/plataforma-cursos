// ============================================================================
// CLIENTE API — llama al backend Spring Boot con el token de Azure AD B2C
// ============================================================================

async function apiCall(method, path, { params, body, isBinary } = {}) {
  const token = await getAccessToken();
  const url = new URL(window.APP_CONFIG.apiBaseUrl.replace(/\/$/, "") + path);
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== "") url.searchParams.set(k, v);
    });
  }

  const headers = { Authorization: `Bearer ${token}` };
  if (body !== undefined) headers["Content-Type"] = "application/json";

  logActivity(`→ ${method} ${url.pathname}${url.search}`, body ? JSON.stringify(body, null, 2) : null);

  const resp = await fetch(url.toString(), {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (isBinary) {
    if (!resp.ok) {
      const errText = await resp.text();
      logActivity(`← ${resp.status} ${resp.statusText}`, errText);
      throw new Error(errText);
    }
    logActivity(`← ${resp.status} ${resp.statusText}`, "(archivo binario recibido)");
    return resp.blob();
  }

  const text = await resp.text();
  let data;
  try {
    data = JSON.parse(text);
  } catch {
    data = text;
  }

  logActivity(`← ${resp.status} ${resp.statusText}`, JSON.stringify(data, null, 2));

  if (!resp.ok) {
    throw new Error(typeof data === "string" ? data : JSON.stringify(data));
  }
  return data;
}
