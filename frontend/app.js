// ============================================================================
// APP — orquesta login, tabs, formularios y el log de actividad
// ============================================================================

function logActivity(line1, body, isError) {
  const log = document.getElementById("activityLog");
  const entry = document.createElement("div");
  entry.className = "activity-entry";
  const ts = new Date().toLocaleTimeString();
  entry.innerHTML = `<span class="ts">${ts}</span><span class="line1${isError ? " err" : ""}">${escapeHtml(line1)}</span>` +
    (body ? `<pre>${escapeHtml(body)}</pre>` : "");
  log.appendChild(entry);
  log.scrollTop = log.scrollHeight;
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function showResult(elId, data) {
  document.getElementById(elId).textContent =
    typeof data === "string" ? data : JSON.stringify(data, null, 2);
}

function showError(elId, err) {
  document.getElementById(elId).textContent = "Error: " + err.message;
}

function formToObject(form) {
  const data = {};
  new FormData(form).forEach((v, k) => (data[k] = v));
  return data;
}

// ---------------- Tabs ----------------
document.querySelectorAll(".sidenav .tab").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".sidenav .tab").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".panel").forEach((p) => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById("panel-" + btn.dataset.tab).classList.add("active");
  });
});

// ---------------- Login/logout ----------------
document.getElementById("btnLogin").addEventListener("click", () => login());

function renderAuthBox(account) {
  const box = document.getElementById("authBox");
  if (!account) {
    box.innerHTML = `<button class="btn-primary" id="btnLogin">Iniciar sesión (Azure AD B2C)</button>`;
    document.getElementById("btnLogin").addEventListener("click", () => login());
    document.getElementById("loginScreen").style.display = "flex";
    document.getElementById("appLayout").style.display = "none";
    return;
  }
  const claims = account.idTokenClaims || {};
  const rol = claims["extension_rolCurso"] || "sin-rol";
  const nombre = claims["name"] || account.username || "Usuario";
  box.innerHTML = `
    <span class="role-badge ${rol}">${rol}</span>
    <span style="font-size:13px;color:#dbe2f5;">${nombre}</span>
    <button class="btn-ghost" id="btnLogout">Cerrar sesión</button>
  `;
  document.getElementById("btnLogout").addEventListener("click", () => logout());
  document.getElementById("loginScreen").style.display = "none";
  document.getElementById("appLayout").style.display = "grid";
}

// ---------------- Formularios: Materiales ----------------
document.getElementById("formCrearMaterial").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const data = await apiCall("POST", "/cursos/crear", { body: formToObject(e.target) });
    showResult("res-crear", data);
  } catch (err) { showError("res-crear", err); }
});

document.getElementById("formSubirMaterial").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const params = formToObject(e.target);
    const data = await apiCall("POST", "/cursos/subir", { params });
    showResult("res-subir", data);
  } catch (err) { showError("res-subir", err); }
});

document.getElementById("formDescargarMaterial").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const params = formToObject(e.target);
    const blob = await apiCall("GET", "/cursos/descargar", { params, isBinary: true });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = params.tituloMaterial + ".txt";
    a.click();
    showResult("res-descargar", "Descarga iniciada: " + params.tituloMaterial + ".txt");
  } catch (err) { showError("res-descargar", err); }
});

document.getElementById("formActualizarMaterial").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const { codigoCurso, fecha, tituloMaterial, descripcion } = formToObject(e.target);
    const data = await apiCall("PUT", "/cursos/actualizar", {
      params: { codigoCurso, fecha, tituloMaterial },
      body: { codigoCurso, fecha, tituloMaterial, descripcion, nombreCurso: "", instructor: "", tipoContenido: "PDF", estado: "PUBLICADO" },
    });
    showResult("res-actualizar", data);
  } catch (err) { showError("res-actualizar", err); }
});

document.getElementById("btnEliminarMaterial").addEventListener("click", async () => {
  try {
    const form = document.getElementById("formActualizarMaterial");
    const { codigoCurso, fecha, tituloMaterial } = formToObject(form);
    const data = await apiCall("DELETE", "/cursos/eliminar", { params: { codigoCurso, fecha, tituloMaterial } });
    showResult("res-actualizar", data);
  } catch (err) { showError("res-actualizar", err); }
});

document.getElementById("formConsultarMaterial").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const params = formToObject(e.target);
    const data = await apiCall("GET", "/cursos/consultar", { params });
    showResult("res-consultar", data);
  } catch (err) { showError("res-consultar", err); }
});

// ---------------- Formularios: Inscripciones ----------------
document.getElementById("formInscribir").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const body = formToObject(e.target);
    body.estado = "PENDIENTE";
    const data = await apiCall("POST", "/inscripciones/inscribir", { body });
    showResult("res-inscribir", data);
  } catch (err) { showError("res-inscribir", err); }
});

document.getElementById("btnProcesarCola").addEventListener("click", async () => {
  try {
    const data = await apiCall("POST", "/inscripciones/procesar-cola");
    showResult("res-procesar-cola", data);
  } catch (err) { showError("res-procesar-cola", err); }
});

// ---------------- Formularios: Calificaciones ----------------
document.getElementById("formRegistrarNota").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const body = formToObject(e.target);
    body.nota = parseFloat(body.nota);
    const data = await apiCall("POST", "/calificaciones/registrar", { body });
    showResult("res-registrar-nota", data);
  } catch (err) { showError("res-registrar-nota", err); }
});

document.getElementById("formConsultarNotas").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const params = formToObject(e.target);
    const data = await apiCall("GET", "/calificaciones/consultar", { params });
    showResult("res-consultar-notas", data);
  } catch (err) { showError("res-consultar-notas", err); }
});

// ---------------- Init ----------------
(async () => {
  logActivity("Inicializando sesión con Azure AD B2C…");
  const account = await initAuth();
  renderAuthBox(account);
  if (account) logActivity("Sesión activa", JSON.stringify(decodeJwtClaims(account.idToken || ""), null, 2));
})();
