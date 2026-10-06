(() => {
  "use strict";

  const HISTORY_KEY = "campusreporta-ai-chat-v1";
  const SPEAKING_KEY = "campusreporta-ai-speaking";
  const API_URL = "/api/ia/chat";
  const launcher = document.getElementById("aiLauncher");
  const panel = document.getElementById("aiPanel");
  const messages = document.getElementById("aiMessages");
  const form = document.getElementById("aiForm");
  const input = document.getElementById("aiInput");
  const sendButton = document.getElementById("aiSend");
  const micButton = document.getElementById("aiMic");
  const speakToggle = document.getElementById("aiSpeakToggle");
  const speechAvailable = "speechSynthesis" in window && "SpeechSynthesisUtterance" in window;
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  let recognition;
  let listening = false;
  let speakingEnabled = loadSpeakingPreference();
  let sending = false;
  let history = loadHistory();

  function loadSpeakingPreference() {
    if (!speechAvailable) return false;
    try {
      return localStorage.getItem(SPEAKING_KEY) !== "false";
    } catch (error) {
      console.warn("No fue posible leer la preferencia de voz; se usará lectura activada.", error);
      return true;
    }
  }

  function loadHistory() {
    try {
      const saved = JSON.parse(localStorage.getItem(HISTORY_KEY) || "[]");
      return Array.isArray(saved) ? saved.filter((item) =>
        item && ["user", "assistant"].includes(item.role) && typeof item.text === "string"
        && !(item.role === "assistant" && /Falta configurar GEMINI_API_KEY/i.test(item.text))
      ).slice(-40) : [];
    } catch (error) {
      console.warn("No fue posible restaurar la conversación guardada.", error);
      return [];
    }
  }

  function saveHistory() {
    try {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(history.slice(-40)));
    } catch (error) {
      console.warn("No fue posible guardar esta conversación.", error);
    }
  }

  function timestamp() {
    return new Intl.DateTimeFormat("es-CO", { hour: "2-digit", minute: "2-digit" }).format(new Date());
  }

  function scrollToLatest() {
    messages.scrollTop = messages.scrollHeight;
  }

  function addMessage(role, text, documents = [], save = true) {
    const wrapper = document.createElement("article");
    wrapper.className = `ai-message ${role}`;
    const avatar = document.createElement("span");
    avatar.className = "ai-message-avatar";
    avatar.setAttribute("aria-hidden", "true");
    avatar.textContent = role === "assistant" ? "✦" : "Tú";
    const bubble = document.createElement("div");
    bubble.className = "ai-bubble";
    const body = document.createElement("span");
    body.textContent = text;
    bubble.append(body);

    if (role === "assistant" && documents.length) {
      const sources = document.createElement("div");
      sources.className = "ai-message-sources";
      const reportIds = documents.filter((document) => document.type === "reporte").map((document) => `#${document.id}`);
      sources.textContent = reportIds.length
        ? `Basado en ${reportIds.length} ${reportIds.length === 1 ? "reporte" : "reportes"}: ${reportIds.join(", ")}`
        : "Basado en indicadores actuales del campus.";
      bubble.append(sources);
    }

    const time = document.createElement("time");
    time.className = "ai-message-time";
    time.textContent = timestamp();
    bubble.append(time);
    wrapper.append(avatar, bubble);
    messages.append(wrapper);

    if (save) {
      history.push({ role, text });
      history = history.slice(-40);
      saveHistory();
    }
    scrollToLatest();
    return wrapper;
  }

  function renderHistory() {
    messages.replaceChildren();
    if (!history.length) {
      addMessage("assistant", "¡Hola! Soy Laura, tu asistente de CampusReporta. Puedo ayudarte a consultar los reportes y el estado de mantenimiento del campus.", [], false);
      return;
    }
    history.forEach((item) => addMessage(item.role, item.text, [], false));
  }

  function setPanelOpen(open) {
    panel.hidden = !open;
    launcher.setAttribute("aria-expanded", String(open));
    launcher.classList.toggle("is-open", open);
    if (open) {
      renderHistory();
      input.focus();
    } else {
      stopRecognition();
      launcher.focus();
    }
  }

  function showTyping() {
    const wrapper = document.createElement("article");
    wrapper.className = "ai-message assistant";
    wrapper.id = "aiTyping";
    const avatar = document.createElement("span");
    avatar.className = "ai-message-avatar";
    avatar.setAttribute("aria-hidden", "true");
    avatar.textContent = "✦";
    const bubble = document.createElement("div");
    bubble.className = "ai-bubble";
    bubble.setAttribute("aria-label", "Laura está escribiendo");
    bubble.innerHTML = '<span class="ai-typing"><i></i><i></i><i></i></span>';
    wrapper.append(avatar, bubble);
    messages.append(wrapper);
    scrollToLatest();
  }

  function removeTyping() {
    document.getElementById("aiTyping")?.remove();
  }

  async function ask(question) {
    const cleanQuestion = question.trim();
    if (!cleanQuestion || sending) return;
    sending = true;
    sendButton.disabled = true;
    input.disabled = true;
    document.getElementById("aiSuggestions").hidden = true;
    addMessage("user", cleanQuestion);
    showTyping();
    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({ pregunta: cleanQuestion })
      });
      const payload = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(payload.message || `No se pudo completar la consulta (HTTP ${response.status}).`);
      }
      if (typeof payload.respuesta !== "string" || !payload.respuesta.trim()) {
        throw new Error("El asistente no devolvió una respuesta. Intenta de nuevo.");
      }
      removeTyping();
      addMessage("assistant", payload.respuesta, Array.isArray(payload.documentosUsados) ? payload.documentosUsados : []);
      if (speakingEnabled) speak(payload.respuesta);
    } catch (error) {
      removeTyping();
      const message = error instanceof TypeError
        ? "No pude conectarme con el servidor. Confirma que CampusReporta esté iniciado."
        : error.message;
      addMessage("assistant", message);
    } finally {
      sending = false;
      sendButton.disabled = false;
      input.disabled = false;
      input.value = "";
      input.focus();
    }
  }

  function speak(text) {
    if (!speechAvailable || !speakingEnabled) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "es-CO";
    utterance.rate = 0.95;
    utterance.pitch = 1.05;
    utterance.voice = selectSpanishVoice();
    window.speechSynthesis.speak(utterance);
  }

  function selectSpanishVoice() {
    const voices = window.speechSynthesis.getVoices();
    return voices.find((voice) => voice.lang.toLowerCase() === "es-co")
      || voices.find((voice) => voice.lang.toLowerCase().startsWith("es"))
      || null;
  }

  function updateSpeakToggle() {
    speakToggle.classList.toggle("active", speakingEnabled);
    speakToggle.setAttribute("aria-pressed", String(speakingEnabled));
    speakToggle.setAttribute(
      "aria-label",
      speakingEnabled ? "Silenciar respuestas de Laura" : "Activar lectura de respuestas"
    );
    speakToggle.title = speakingEnabled ? "Silenciar respuestas" : "Leer respuestas en voz alta";
    speakToggle.innerHTML = `<i class="fa-solid ${speakingEnabled ? "fa-volume-high" : "fa-volume-xmark"}"></i>`;
    speakToggle.disabled = !speechAvailable;
    if (!speechAvailable) {
      speakToggle.title = "La lectura en voz alta no está disponible en este navegador";
    }
  }

  function setListening(active) {
    listening = active;
    micButton.classList.toggle("listening", active);
    micButton.setAttribute("aria-pressed", String(active));
    micButton.setAttribute("aria-label", active ? "Detener dictado" : "Dictar pregunta");
    if (active) {
      micButton.title = "Escuchando…";
    } else {
      micButton.title = "Dictar pregunta";
    }
  }

  function stopRecognition() {
    if (!recognition || !listening) return;
    recognition.stop();
  }

  function startRecognition() {
    if (!SpeechRecognition) {
      addMessage("assistant", "El dictado por voz no está disponible en este navegador. Prueba con Chrome o Edge y permite el acceso al micrófono.");
      return;
    }
    if (listening) {
      stopRecognition();
      return;
    }

    recognition = new SpeechRecognition();
    recognition.lang = "es-CO";
    recognition.continuous = false;
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;
    recognition.onstart = () => setListening(true);
    recognition.onresult = (event) => {
      const transcript = event.results?.[0]?.[0]?.transcript?.trim();
      if (!transcript) {
        addMessage("assistant", "No alcancé a entender. Intenta de nuevo o escribe tu pregunta.");
        return;
      }
      input.value = transcript.replace(/^laura[\s,:-]*/i, "");
      ask(input.value || transcript);
    };
    recognition.onerror = (event) => {
      if (event.error !== "no-speech" && event.error !== "aborted") {
        addMessage("assistant", event.error === "not-allowed"
          ? "El navegador no tiene permiso para usar el micrófono."
          : "No pude procesar el audio. Puedes intentarlo otra vez.");
      }
    };
    recognition.onend = () => setListening(false);
    try {
      recognition.start();
    } catch (error) {
      setListening(false);
      addMessage("assistant", "El micrófono ya está activo. Espera un momento e inténtalo de nuevo.");
    }
  }

  launcher.addEventListener("click", () => setPanelOpen(panel.hidden));
  document.getElementById("aiClose").addEventListener("click", () => setPanelOpen(false));
  document.getElementById("aiMic").addEventListener("click", startRecognition);

  speakToggle.addEventListener("click", () => {
    speakingEnabled = !speakingEnabled;
    try {
      localStorage.setItem(SPEAKING_KEY, String(speakingEnabled));
    } catch (error) {
      console.warn("No fue posible guardar la preferencia de voz.", error);
    }
    updateSpeakToggle();
    if (!speakingEnabled && speechAvailable) window.speechSynthesis.cancel();
  });

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    ask(input.value);
  });

  input.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      form.requestSubmit();
    }
  });

  input.addEventListener("input", () => {
    input.style.height = "auto";
    input.style.height = `${Math.min(input.scrollHeight, 95)}px`;
  });

  document.getElementById("aiSuggestions").addEventListener("click", (event) => {
    const suggestion = event.target.closest("[data-ai-prompt]");
    if (suggestion) ask(suggestion.dataset.aiPrompt);
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !panel.hidden) setPanelOpen(false);
  });

  updateSpeakToggle();
  renderHistory();
})();
