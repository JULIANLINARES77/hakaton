(() => {
  "use strict";

  const STORAGE_KEY = "campusreporta-demo-reports-v1";
  const userDirectory = [
    { id: "u1", name: "Ana María Torres", role: "Estudiante", email: "ana.torres@uniminuto.edu.co" },
    { id: "u2", name: "Carlos Andrés Rojas", role: "Docente", email: "carlos.rojas@uniminuto.edu.co" },
    { id: "u3", name: "Marta Lucía Gómez", role: "Mantenimiento", email: "marta.gomez@uniminuto.edu.co" },
    { id: "u4", name: "Julián Esteban Pérez", role: "Estudiante", email: "julian.perez@uniminuto.edu.co" }
  ];
  const locationDirectory = [
    { id: "l1", block: "Bloque A", room: "101", label: "Bloque A · Salón 101" },
    { id: "l2", block: "Bloque A", room: "205", label: "Bloque A · Salón 205" },
    { id: "l3", block: "Bloque B", room: "Laboratorio 202", label: "Bloque B · Laboratorio 202" },
    { id: "l4", block: "Biblioteca", room: "Sala de lectura", label: "Biblioteca · Sala de lectura" },
    { id: "l5", block: "Cafetería", room: "Patio central", label: "Cafetería · Patio central" }
  ];
  const today = new Date();
  const ago = (days, hours = 10) => {
    const date = new Date();
    date.setDate(date.getDate() - days);
    date.setHours(hours, 20, 0, 0);
    return date.toISOString();
  };
  const seedReports = [
    { id: "CR-1048", description: "Una baldosa está suelta cerca de la entrada del salón.", locationId: "l1", reporterId: "u1", status: "Pendiente", createdAt: ago(0, 8), history: [{ previous: null, next: "Pendiente", date: ago(0, 8), userId: "u1", note: "Reporte creado." }] },
    { id: "CR-1047", description: "Dos computadores no encienden y se percibe olor a quemado.", locationId: "l3", reporterId: "u2", status: "En Proceso", createdAt: ago(0, 7), history: [{ previous: null, next: "Pendiente", date: ago(0, 7), userId: "u2", note: "Reporte creado." }, { previous: "Pendiente", next: "En Proceso", date: ago(0, 5, 14), userId: "u3", note: "Reporte asignado al equipo de mantenimiento." }] },
    { id: "CR-1046", description: "El grifo del baño de mujeres presenta una fuga constante.", locationId: "l2", reporterId: "u4", status: "Resuelto", createdAt: ago(1, 11), history: [{ previous: null, next: "Pendiente", date: ago(1, 11), userId: "u4", note: "Reporte creado." }, { previous: "Pendiente", next: "En Proceso", date: ago(1, 13), userId: "u3", note: "Se inició la revisión." }, { previous: "En Proceso", next: "Resuelto", date: ago(0, 9), userId: "u3", note: "Empaque reemplazado; fuga reparada." }] },
    { id: "CR-1045", description: "Una lámpara del pasillo permanece apagada.", locationId: "l4", reporterId: "u1", status: "En Proceso", createdAt: ago(2, 9), history: [{ previous: null, next: "Pendiente", date: ago(2, 9), userId: "u1", note: "Reporte creado." }, { previous: "Pendiente", next: "En Proceso", date: ago(1, 8), userId: "u3", note: "Técnico asignado para inspección." }] },
    { id: "CR-1044", description: "La mesa junto a la ventana tiene una pata floja.", locationId: "l5", reporterId: "u4", status: "Pendiente", createdAt: ago(3, 15), history: [{ previous: null, next: "Pendiente", date: ago(3, 15), userId: "u4", note: "Reporte creado." }] },
    { id: "CR-1043", description: "Hay humedad en el techo del aula después de la lluvia.", locationId: "l2", reporterId: "u2", status: "Resuelto", createdAt: ago(5, 10), history: [{ previous: null, next: "Pendiente", date: ago(5, 10), userId: "u2", note: "Reporte creado." }, { previous: "Pendiente", next: "En Proceso", date: ago(4, 9), userId: "u3", note: "Inspección del techo programada." }, { previous: "En Proceso", next: "Resuelto", date: ago(2, 16), userId: "u3", note: "Se selló la filtración y se verificó el área." }] }
  ];
  const titles = { dashboard: "Dashboard", new: "Nuevo reporte", mine: "Mis reportes", all: "Todos los reportes", history: "Historial", pdf: "Reportes PDF", detail: "Detalle del reporte" };
  const state = { view: "dashboard", role: "Estudiante", filter: "Todos", query: "", date: "", selectedId: null, chart: null, pdf: { from: "", to: "", status: "Todos", location: "Todas" } };
  const main = document.getElementById("mainContent");
  const $ = (selector, parent = document) => parent.querySelector(selector);
  const $$ = (selector, parent = document) => [...parent.querySelectorAll(selector)];
  const escapeHTML = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
  const reports = loadReports();
  const currentUser = () => userDirectory.find((user) => user.role === state.role) || userDirectory[0];
  const getUser = (id) => userDirectory.find((user) => user.id === id) || { name: "Usuario", role: "Usuario" };
  const getLocation = (id) => locationDirectory.find((location) => location.id === id) || { label: "Ubicación no disponible", block: "Sin ubicación" };
  const formatDate = (value, options = { day: "2-digit", month: "short", year: "numeric" }) => new Intl.DateTimeFormat("es-CO", options).format(new Date(value));
  const formatTime = (value) => new Intl.DateTimeFormat("es-CO", { hour: "2-digit", minute: "2-digit" }).format(new Date(value));
  const dateTime = (value) => `${formatDate(value)} · ${formatTime(value)}`;
  const sortedReports = () => [...reports].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  const statusClass = (status) => ({ "Pendiente": "pending", "En Proceso": "progress", "Resuelto": "resolved" })[status] || "pending";
  const statusBadge = (status) => `<span class="badge badge-${statusClass(status)}">${escapeHTML(status)}</span>`;
  const filteredReports = (source = sortedReports()) => source.filter((report) => {
    const matchesStatus = state.filter === "Todos" || report.status === state.filter;
    const search = state.query.trim().toLocaleLowerCase("es");
    const location = getLocation(report.locationId).label.toLocaleLowerCase("es");
    const matchesText = !search || `${report.id} ${report.description} ${location} ${getUser(report.reporterId).name}`.toLocaleLowerCase("es").includes(search);
    const matchesDate = !state.date || report.createdAt.slice(0, 10) === state.date;
    return matchesStatus && matchesText && matchesDate;
  });

  function loadReports() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : structuredClone(seedReports);
    } catch (error) {
      console.error("No fue posible leer los reportes de demostración almacenados.", error);
      return structuredClone(seedReports);
    }
  }

  function saveReports() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(reports));
    } catch (error) {
      console.error("No fue posible guardar los reportes de demostración.", error);
      toast("No se pudo guardar el cambio en este navegador.", "error");
    }
  }

  function toast(message, type = "success") {
    const node = document.createElement("div");
    node.className = "toast";
    node.innerHTML = `<i class="fa-solid ${type === "error" ? "fa-circle-exclamation" : "fa-circle-check"}"></i><span>${escapeHTML(message)}</span>`;
    $("#toastRegion").append(node);
    window.setTimeout(() => node.remove(), 3600);
  }

  function setView(view, options = {}) {
    if (view === "all" && state.role !== "Mantenimiento") {
      view = "mine";
      toast("La vista de todos los reportes es exclusiva de Mantenimiento.");
    }
    state.view = view;
    if (view !== "detail") state.selectedId = null;
    if (options.resetFilters) {
      state.filter = "Todos";
      state.query = "";
      state.date = "";
    }
    $$(".nav-link").forEach((link) => link.classList.toggle("active", link.dataset.view === view || (view === "detail" && link.dataset.view === "mine")));
    $("#breadcrumbCurrent").textContent = view === "detail" ? titles.detail : titles[view] || titles.dashboard;
    $("#sidebar").classList.remove("mobile-open");
    $("#sidebarBackdrop").classList.remove("visible");
    render();
    main.focus({ preventScroll: true });
    if (view === "dashboard") window.location.hash = "dashboard";
    else window.location.hash = view === "detail" ? `reporte/${encodeURIComponent(state.selectedId || "")}` : view;
  }

  function heading(eyebrow, title, subtitle, actions = "") {
    return `<div class="page-heading"><div><div class="eyebrow">${eyebrow}</div><h1>${title}</h1><p>${subtitle}</p></div><div class="heading-actions">${actions}</div></div>`;
  }

  function tableRows(list, includeActions = true) {
    if (!list.length) return `<tr><td colspan="6"><div class="empty-state"><i class="fa-regular fa-folder-open"></i>No encontramos reportes para estos filtros.</div></td></tr>`;
    return list.map((report) => {
      const location = getLocation(report.locationId);
      return `<tr>
        <td class="table-id">${escapeHTML(report.id)}</td>
        <td class="description-cell">${escapeHTML(report.description)}</td>
        <td class="location-cell">${escapeHTML(location.label)}</td>
        <td>${statusBadge(report.status)}</td>
        <td class="date-cell">${escapeHTML(formatDate(report.createdAt))}</td>
        ${includeActions ? `<td><button class="table-action" type="button" data-detail="${escapeHTML(report.id)}">Ver <i class="fa-solid fa-arrow-up-right-from-square"></i></button></td>` : ""}
      </tr>`;
    }).join("");
  }

  function reportsTable(list, includeActions = true) {
    return `<div class="table-wrap"><table><thead><tr><th>ID</th><th>Descripción</th><th>Ubicación</th><th>Estado</th><th>Fecha</th>${includeActions ? "<th>Acción</th>" : ""}</tr></thead><tbody>${tableRows(list, includeActions)}</tbody></table></div>`;
  }

  function dashboardView() {
    const pending = reports.filter((report) => report.status === "Pendiente").length;
    const progress = reports.filter((report) => report.status === "En Proceso").length;
    const resolved = reports.filter((report) => report.status === "Resuelto").length;
    const actions = `<span class="today-chip"><i class="fa-regular fa-calendar"></i>${formatDate(new Date())}</span><button class="button button-primary" type="button" data-go="new"><i class="fa-solid fa-plus"></i> Nuevo reporte</button>`;
    return `${heading("RESUMEN DEL CAMPUS", "Panel de control", "Seguimiento de novedades y solicitudes de mantenimiento.", actions)}
      <section class="kpi-grid" aria-label="Indicadores de reportes">
        <article class="kpi-card tone-yellow"><div class="kpi-top"><span class="kpi-label">Reportes pendientes</span><span class="kpi-icon"><i class="fa-regular fa-clock"></i></span></div><strong class="kpi-value">${pending}<span class="kpi-meta">por atender</span></strong></article>
        <article class="kpi-card tone-blue"><div class="kpi-top"><span class="kpi-label">En proceso</span><span class="kpi-icon"><i class="fa-solid fa-spinner"></i></span></div><strong class="kpi-value">${progress}<span class="kpi-meta">en atención</span></strong></article>
        <article class="kpi-card tone-green"><div class="kpi-top"><span class="kpi-label">Reportes resueltos</span><span class="kpi-icon"><i class="fa-regular fa-circle-check"></i></span></div><strong class="kpi-value">${resolved}<span class="kpi-meta">solucionados</span></strong></article>
        <article class="kpi-card tone-gray"><div class="kpi-top"><span class="kpi-label">Total reportes</span><span class="kpi-icon"><i class="fa-regular fa-file-lines"></i></span></div><strong class="kpi-value">${reports.length}<span class="kpi-meta">registrados</span></strong></article>
      </section>
      <section class="dashboard-grid">
        <article class="panel"><div class="panel-header"><div><h2 class="panel-title">Reportes por estado</h2><p class="panel-subtitle">Distribución de solicitudes registradas</p></div><i class="fa-solid fa-chart-pie" style="color:#8aa3c5"></i></div><div class="chart-wrap"><canvas id="statusChart" aria-label="Gráfico de reportes por estado"></canvas></div></article>
        <article class="panel"><div class="panel-header"><div><h2 class="panel-title">Reportes recientes</h2><p class="panel-subtitle">Últimas novedades del campus</p></div><button class="text-link" type="button" data-go="${state.role === "Mantenimiento" ? "all" : "mine"}">Ver todos <i class="fa-solid fa-arrow-right"></i></button></div>${reportsTable(sortedReports().slice(0, 5))}</article>
      </section>`;
  }

  function newReportView() {
    const users = userDirectory.filter((user) => state.role === "Mantenimiento" ? user.role !== "Mantenimiento" : user.role === state.role);
    return `${heading("NUEVA SOLICITUD", "Reporta un daño", "Cuéntanos qué ocurre y dónde. Nos encargamos del resto.")}
      <div class="content-narrow"><form class="form-card" id="reportForm" novalidate>
        <div class="form-intro"><i class="fa-regular fa-lightbulb"></i><div><strong>Tu reporte ayuda a cuidar el campus</strong><p>Incluye detalles concretos para que el equipo pueda identificar y atender el problema más rápido.</p></div></div>
        <div class="form-grid">
          <div class="field field-full"><label for="description">Descripción del daño <span class="required">*</span></label><textarea id="description" name="description" maxlength="500" placeholder="Ej. La llave del lavamanos del baño del segundo piso no cierra y está goteando..." required aria-describedby="descriptionError descriptionCount"></textarea><span class="field-error" id="descriptionError"></span><span class="char-count" id="descriptionCount">0 / 500</span></div>
          <div class="field"><label for="location">Ubicación <span class="required">*</span></label><select id="location" name="location" required><option value="">Selecciona bloque y salón</option>${locationDirectory.map((location) => `<option value="${location.id}">${escapeHTML(location.label)}</option>`).join("")}</select><span class="field-error" id="locationError"></span></div>
          <div class="field"><label for="reporter">¿Quién reporta? <span class="required">*</span></label><select id="reporter" name="reporter" required><option value="">Selecciona una persona</option>${users.map((user) => `<option value="${user.id}" ${user.id === currentUser().id ? "selected" : ""}>${escapeHTML(user.name)} · ${escapeHTML(user.role)}</option>`).join("")}</select><span class="field-error" id="reporterError"></span></div>
        </div>
        <div class="form-footer"><span class="form-footer-note"><i class="fa-solid fa-lock"></i> La información se utilizará solo para atender este reporte.</span><button class="button button-success" type="submit"><i class="fa-solid fa-paper-plane"></i> Enviar reporte</button></div>
      </form></div>`;
  }

  function listView(isAll) {
    const shownReports = isAll ? sortedReports() : sortedReports().filter((report) => report.reporterId === currentUser().id);
    const viewReports = filteredReports(shownReports);
    const action = isAll ? `<span class="today-chip"><i class="fa-solid fa-screwdriver-wrench"></i>Vista de mantenimiento</span>` : `<button class="button button-primary" type="button" data-go="new"><i class="fa-solid fa-plus"></i> Nuevo reporte</button>`;
    return `${heading(isAll ? "GESTIÓN DEL CAMPUS" : "SEGUIMIENTO PERSONAL", isAll ? "Todos los reportes" : "Mis reportes", isAll ? "Revisa, filtra y gestiona las solicitudes del campus." : "Consulta el avance de los daños que has reportado.", action)}
      <section class="filter-panel">
        <label class="search-box"><i class="fa-solid fa-magnifying-glass"></i><input class="filter-input" id="reportSearch" type="search" placeholder="Buscar por descripción, ubicación o ID..." value="${escapeHTML(state.query)}" aria-label="Buscar reportes"></label>
        <span class="filter-label">Estado:</span><div class="filter-chips" role="group" aria-label="Filtrar por estado">${["Todos", "Pendiente", "En Proceso", "Resuelto"].map((filter) => `<button class="filter-chip ${filter === state.filter ? "selected" : ""}" type="button" data-filter="${escapeHTML(filter)}">${escapeHTML(filter)}</button>`).join("")}</div>
        <input class="date-filter" type="date" id="reportDate" value="${escapeHTML(state.date)}" aria-label="Filtrar por fecha">
      </section>
      <section class="report-list-panel"><div class="panel-header"><div><h2 class="panel-title">Listado de solicitudes</h2><p class="panel-subtitle">Información actualizada de reportes</p></div><div class="table-tools"><span class="results-count">${viewReports.length} de ${shownReports.length} reportes</span><button class="icon-action" type="button" data-refresh aria-label="Limpiar filtros"><i class="fa-solid fa-rotate"></i></button></div></div>${reportsTable(viewReports)}</section>`;
  }

  function detailView() {
    const report = reports.find((item) => item.id === state.selectedId);
    if (!report) {
      state.view = "mine";
      return listView(false);
    }
    const reporter = getUser(report.reporterId);
    const location = getLocation(report.locationId);
    const history = [...(report.history || [])].sort((a, b) => new Date(b.date) - new Date(a.date));
    return `${heading("SEGUIMIENTO", `Reporte ${escapeHTML(report.id)}`, "Detalle de la solicitud y registro de actividad.", `<button class="button button-outline" type="button" data-go="${state.role === "Mantenimiento" ? "all" : "mine"}"><i class="fa-solid fa-arrow-left"></i> Volver</button>`)}
      <div class="detail-grid">
        <section class="panel detail-card"><span class="detail-id">${escapeHTML(report.id)}</span><p class="detail-description">${escapeHTML(report.description)}</p>
          <div class="detail-info-grid">
            <div><span class="detail-info-label">Ubicación</span><span class="detail-info-value"><i class="fa-solid fa-location-dot"></i> ${escapeHTML(location.label)}</span></div>
            <div><span class="detail-info-label">Reportado por</span><span class="detail-info-value">${escapeHTML(reporter.name)} · ${escapeHTML(reporter.role)}</span></div>
            <div><span class="detail-info-label">Fecha de reporte</span><span class="detail-info-value">${escapeHTML(dateTime(report.createdAt))}</span></div>
            <div><span class="detail-info-label">Estado actual</span><span class="detail-info-value">${statusBadge(report.status)}</span></div>
          </div>
          <div class="detail-status-row"><span class="detail-info-label" style="margin:0">Gracias por ayudarnos a cuidar el campus.</span>${state.role === "Mantenimiento" ? `<button class="button button-primary button-small" type="button" data-change-status="${escapeHTML(report.id)}"><i class="fa-solid fa-pen"></i> Cambiar estado</button>` : ""}</div>
        </section>
        <section class="panel"><div class="panel-header"><div><h2 class="panel-title">Historial de actividad</h2><p class="panel-subtitle">${history.length} ${history.length === 1 ? "actualización" : "actualizaciones"}</p></div><i class="fa-solid fa-clock-rotate-left" style="color:#8aa3c5"></i></div>
          <div class="timeline">${history.length ? history.map((entry) => {
            const actor = getUser(entry.userId);
            const change = entry.previous ? `${entry.previous} → ${entry.next}` : `Reporte creado · ${entry.next}`;
            return `<article class="timeline-item"><span class="timeline-dot"></span><div><h3 class="timeline-title">${escapeHTML(change)}</h3><p class="timeline-meta">${escapeHTML(dateTime(entry.date))} · ${escapeHTML(actor.name)}</p>${entry.note ? `<p class="timeline-note">${escapeHTML(entry.note)}</p>` : ""}</div></article>`;
          }).join("") : `<div class="empty-state">Aún no hay actividad registrada.</div>`}</div>
        </section>
      </div>`;
  }

  function historyView() {
    const entries = sortedReports().flatMap((report) => (report.history || []).map((item) => ({ ...item, report })))
      .sort((a, b) => new Date(b.date) - new Date(a.date));
    return `${heading("ACTIVIDAD RECIENTE", "Historial", "Sigue los cambios de estado registrados en los reportes del campus.")}
      <section class="history-list">${entries.length ? entries.map((entry) => {
        const actor = getUser(entry.userId);
        const change = entry.previous ? `${entry.previous} → ${entry.next}` : `Reporte creado · ${entry.next}`;
        return `<article class="history-card"><span class="history-icon"><i class="fa-solid ${entry.previous ? "fa-arrows-rotate" : "fa-file-circle-plus"}"></i></span><div><h2 class="history-title">${escapeHTML(entry.report.id)} · ${escapeHTML(change)}</h2><p class="history-meta">${escapeHTML(actor.name)} · ${escapeHTML(getLocation(entry.report.locationId).label)}</p></div><time class="history-date">${escapeHTML(dateTime(entry.date))}</time></article>`;
      }).join("") : `<div class="panel empty-state"><i class="fa-regular fa-clock"></i>Aún no hay actividad.</div>`}</section>`;
  }

  function pdfView() {
    const locationOptions = locationDirectory.map((location) => `<option value="${escapeHTML(location.id)}" ${state.pdf.location === location.id ? "selected" : ""}>${escapeHTML(location.label)}</option>`).join("");
    const list = sortedReports().filter((report) => {
      const created = report.createdAt.slice(0, 10);
      return (!state.pdf.from || created >= state.pdf.from) && (!state.pdf.to || created <= state.pdf.to)
        && (state.pdf.status === "Todos" || report.status === state.pdf.status)
        && (state.pdf.location === "Todas" || report.locationId === state.pdf.location);
    });
    const actions = `<span class="today-chip"><i class="fa-regular fa-file-pdf"></i>Exportación de reportes</span>`;
    return `${heading("DOCUMENTACIÓN", "Reportes PDF", "Filtra los registros del campus y prepara una versión para compartir.", actions)}
      <section class="filter-panel"><div class="report-cover"><span class="pdf-cover-icon"><i class="fa-regular fa-file-pdf"></i></span><div><strong>Resumen de reportes del campus</strong><span>Configura los filtros y genera una vista lista para guardar como PDF.</span></div></div>
        <div class="pdf-filters">
          <div class="field"><label for="pdfFrom">Desde</label><input type="date" id="pdfFrom" value="${escapeHTML(state.pdf.from)}"></div>
          <div class="field"><label for="pdfTo">Hasta</label><input type="date" id="pdfTo" value="${escapeHTML(state.pdf.to)}"></div>
          <div class="field"><label for="pdfStatus">Estado</label><select id="pdfStatus"><option>Todos</option>${["Pendiente", "En Proceso", "Resuelto"].map((value) => `<option ${state.pdf.status === value ? "selected" : ""}>${value}</option>`).join("")}</select></div>
          <div class="field"><label for="pdfLocation">Ubicación</label><select id="pdfLocation"><option value="Todas">Todas las ubicaciones</option>${locationOptions}</select></div>
        </div>
        <div class="pdf-actions"><span class="results-count" style="margin-right:auto;align-self:center">${list.length} reportes incluidos</span><button class="button button-outline" type="button" data-pdf-action="share"><i class="fa-solid fa-share-nodes"></i> Compartir</button><button class="button button-outline" type="button" data-pdf-action="download"><i class="fa-solid fa-download"></i> Descargar</button><button class="button button-primary" type="button" data-pdf-action="generate"><i class="fa-regular fa-file-pdf"></i> Generar PDF</button></div>
      </section>
      <section class="report-list-panel"><div class="panel-header"><div><h2 class="panel-title">Vista previa</h2><p class="panel-subtitle">Estos registros aparecerán en el documento</p></div><span class="results-count">${list.length} registros</span></div>${reportsTable(list, false)}</section>`;
  }

  function render() {
    $("#roleSelect").value = state.role;
    $(".maintenance-only").hidden = state.role !== "Mantenimiento";
    const user = currentUser();
    $("#userAvatar").textContent = user.name.split(/\s+/).slice(0, 2).map((word) => word[0]).join("").toUpperCase();
    const screens = { dashboard: dashboardView, new: newReportView, mine: () => listView(false), all: () => listView(true), detail: detailView, history: historyView, pdf: pdfView };
    main.innerHTML = (screens[state.view] || dashboardView)();
    if (state.view === "dashboard") drawChart();
  }

  function drawChart() {
    const canvas = $("#statusChart");
    if (!canvas || !window.Chart) return;
    if (state.chart) state.chart.destroy();
    const counts = ["Pendiente", "En Proceso", "Resuelto"].map((status) => reports.filter((report) => report.status === status).length);
    state.chart = new Chart(canvas, {
      type: "bar",
      data: {
        labels: ["Pendiente", "En proceso", "Resuelto"],
        datasets: [{ data: counts, backgroundColor: ["#ffc107", "#1680ec", "#28a745"], borderRadius: 7, borderSkipped: false, maxBarThickness: 45 }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { displayColors: false, backgroundColor: "#18365f", padding: 10, titleFont: { family: "Inter", size: 10 }, bodyFont: { family: "Inter", size: 10 } } },
        scales: {
          x: { grid: { display: false }, border: { display: false }, ticks: { color: "#8490a2", font: { family: "Inter", size: 9 } } },
          y: { beginAtZero: true, grid: { color: "#eef1f6", drawTicks: false }, border: { display: false }, ticks: { precision: 0, stepSize: 1, padding: 9, color: "#9aa3b1", font: { family: "Inter", size: 9 } } }
        }
      }
    });
  }

  function openDetails(id) {
    if (!reports.some((report) => report.id === id)) return;
    state.selectedId = id;
    setView("detail");
  }

  function validateField(field, message) {
    const valid = Boolean(field.value.trim());
    field.classList.toggle("invalid", !valid);
    field.setAttribute("aria-invalid", String(!valid));
    const error = $(`#${field.id}Error`);
    if (error) error.textContent = valid ? "" : message;
    return valid;
  }

  function submitReport(form) {
    const description = $("#description", form);
    const location = $("#location", form);
    const reporter = $("#reporter", form);
    const validDescription = validateField(description, "Cuéntanos brevemente qué daño encontraste.");
    const validLocation = validateField(location, "Selecciona el lugar del daño.");
    const validReporter = validateField(reporter, "Selecciona quién realiza el reporte.");
    if (!validDescription || !validLocation || !validReporter) {
      (validDescription ? !validLocation ? location : reporter : description).focus();
      return;
    }
    const now = new Date().toISOString();
    const report = {
      id: `CR-${1049 + reports.length}`,
      description: description.value.trim(),
      locationId: location.value,
      reporterId: reporter.value,
      status: "Pendiente",
      createdAt: now,
      history: [{ previous: null, next: "Pendiente", date: now, userId: reporter.value, note: "Reporte creado." }]
    };
    reports.unshift(report);
    saveReports();
    toast(`Reporte ${report.id} registrado correctamente.`);
    openDetails(report.id);
  }

  function openStatusModal(id) {
    const report = reports.find((item) => item.id === id);
    if (!report) return;
    $("#modalContent").innerHTML = `<div class="eyebrow">GESTIÓN DEL REPORTE · ${escapeHTML(report.id)}</div><h2 id="modalTitle">Actualizar estado</h2><p class="modal-description">El cambio quedará registrado en el historial del reporte.</p>
      <form class="modal-form" id="statusForm" data-report-id="${escapeHTML(id)}">
        <div class="field"><label for="nextStatus">Nuevo estado <span class="required">*</span></label><select id="nextStatus">${["Pendiente", "En Proceso", "Resuelto"].map((status) => `<option ${status === report.status ? "selected" : ""}>${status}</option>`).join("")}</select></div>
        <div class="field"><label for="statusNote">Observación</label><textarea id="statusNote" maxlength="300" placeholder="Agrega un comentario sobre el cambio (opcional)..."></textarea></div>
        <div class="modal-actions"><button class="button button-outline" type="button" data-close-modal>Cancelar</button><button class="button button-primary" type="submit"><i class="fa-solid fa-check"></i> Guardar cambio</button></div>
      </form>`;
    $("#modalBackdrop").hidden = false;
    $("#nextStatus").focus();
  }

  function submitStatus(form) {
    const report = reports.find((item) => item.id === form.dataset.reportId);
    const next = $("#nextStatus", form).value;
    if (!report || report.status === next) {
      closeModal();
      toast("El reporte conserva el mismo estado.");
      return;
    }
    const previous = report.status;
    const now = new Date().toISOString();
    report.status = next;
    report.history = report.history || [];
    report.history.push({ previous, next, date: now, userId: currentUser().id, note: $("#statusNote", form).value.trim() || "Estado actualizado." });
    saveReports();
    closeModal();
    toast(`Estado actualizado: ${previous} → ${next}.`);
    render();
  }

  function closeModal() {
    $("#modalBackdrop").hidden = true;
    $("#modalContent").innerHTML = "";
  }

  function readPdfFilters() {
    state.pdf.from = $("#pdfFrom")?.value || "";
    state.pdf.to = $("#pdfTo")?.value || "";
    state.pdf.status = $("#pdfStatus")?.value || "Todos";
    state.pdf.location = $("#pdfLocation")?.value || "Todas";
    render();
  }

  function runPdfAction(action) {
    const content = $(".report-list-panel")?.outerHTML || "";
    const title = "Reportes CampusReporta";
    if (action === "generate" || action === "download") {
      const printWindow = window.open("", "_blank", "noopener,noreferrer");
      if (!printWindow) {
        toast("Permite las ventanas emergentes para generar el PDF.", "error");
        return;
      }
      printWindow.document.write(`<!doctype html><html lang="es"><head><meta charset="utf-8"><title>${title}</title><style>body{font-family:Arial,sans-serif;padding:30px;color:#27364b}h1{font-size:22px}.meta{color:#69778c;font-size:12px;margin-bottom:20px}table{width:100%;border-collapse:collapse;text-align:left;font-size:11px}th,td{padding:9px;border:1px solid #dce2ea}th{background:#f1f5fa}</style></head><body><h1>CampusReporta · ${title}</h1><p class="meta">Generado el ${escapeHTML(formatDate(new Date()))} · ${reports.length} reportes registrados</p>${content.replace(/<button[\s\S]*?<\/button>/g, "")}</body></html>`);
      printWindow.document.close();
      printWindow.focus();
      window.setTimeout(() => printWindow.print(), 350);
      if (action === "download") toast("En el diálogo de impresión selecciona «Guardar como PDF».");
      return;
    }
    const shareData = { title, text: `Consulta ${reports.length} reportes de CampusReporta.` };
    if (navigator.share) {
      navigator.share(shareData).catch((error) => {
        if (error.name !== "AbortError") toast("No fue posible compartir el reporte.", "error");
      });
    } else if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(`${shareData.text} ${window.location.href}`)
        .then(() => toast("Enlace copiado para compartir."))
        .catch(() => toast("No se pudo copiar el enlace.", "error"));
    } else {
      toast("La opción de compartir no está disponible en este navegador.", "error");
    }
  }

  document.addEventListener("click", (event) => {
    const target = event.target.closest("button, a");
    if (!target) return;
    if (target.matches("[data-view]")) {
      event.preventDefault();
      setView(target.dataset.view, { resetFilters: true });
    } else if (target.matches("[data-go]")) {
      setView(target.dataset.go, { resetFilters: true });
    } else if (target.matches("[data-detail]")) {
      openDetails(target.dataset.detail);
    } else if (target.matches("[data-filter]")) {
      state.filter = target.dataset.filter;
      render();
    } else if (target.matches("[data-change-status]")) {
      openStatusModal(target.dataset.changeStatus);
    } else if (target.matches("[data-close-modal]")) {
      closeModal();
    } else if (target.matches("[data-refresh]")) {
      state.filter = "Todos"; state.query = ""; state.date = "";
      render();
      toast("Filtros limpiados.");
    } else if (target.matches("[data-pdf-action]")) {
      runPdfAction(target.dataset.pdfAction);
    } else if (target.id === "mobileMenu") {
      $("#sidebar").classList.add("mobile-open");
      $("#sidebarBackdrop").classList.add("visible");
    } else if (target.id === "collapseSidebar") {
      document.body.classList.toggle("sidebar-collapsed");
      localStorage.setItem("campusreporta-sidebar-collapsed", String(document.body.classList.contains("sidebar-collapsed")));
    }
  });

  document.addEventListener("input", (event) => {
    if (event.target.id === "reportSearch") {
      state.query = event.target.value;
      const cursor = event.target.selectionStart;
      render();
      const search = $("#reportSearch");
      search?.focus();
      search?.setSelectionRange(cursor, cursor);
    }
    if (event.target.id === "reportDate") {
      state.date = event.target.value;
      render();
    }
    if (event.target.id === "description") {
      $("#descriptionCount").textContent = `${event.target.value.length} / 500`;
      if (event.target.value.trim()) validateField(event.target, "");
    }
  });

  document.addEventListener("change", (event) => {
    if (["pdfFrom", "pdfTo", "pdfStatus", "pdfLocation"].includes(event.target.id)) readPdfFilters();
    if (event.target.id === "roleSelect") {
      state.role = event.target.value;
      if (state.view === "all" && state.role !== "Mantenimiento") state.view = "mine";
      render();
    }
  });

  document.addEventListener("blur", (event) => {
    if (event.target.id === "description") validateField(event.target, "Cuéntanos brevemente qué daño encontraste.");
    if (event.target.id === "location") validateField(event.target, "Selecciona el lugar del daño.");
    if (event.target.id === "reporter") validateField(event.target, "Selecciona quién realiza el reporte.");
  }, true);

  document.addEventListener("submit", (event) => {
    if (event.target.id === "reportForm") {
      event.preventDefault();
      submitReport(event.target);
    } else if (event.target.id === "statusForm") {
      event.preventDefault();
      submitStatus(event.target);
    }
  });

  $("#sidebarBackdrop").addEventListener("click", () => {
    $("#sidebar").classList.remove("mobile-open");
    $("#sidebarBackdrop").classList.remove("visible");
  });
  $("#modalBackdrop").addEventListener("click", (event) => {
    if (event.target.id === "modalBackdrop") closeModal();
  });
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeModal();
      $("#sidebar").classList.remove("mobile-open");
      $("#sidebarBackdrop").classList.remove("visible");
    }
  });
  $("#roleSelect").addEventListener("change", (event) => {
    state.role = event.target.value;
    if (state.view === "all" && state.role !== "Mantenimiento") state.view = "mine";
    render();
  });

  try {
    if (localStorage.getItem("campusreporta-sidebar-collapsed") === "true") document.body.classList.add("sidebar-collapsed");
  } catch (error) {
    console.warn("La preferencia de navegación no se guardará en este navegador.", error);
  }

  render();
})();
