/* Lógica de la landing: i18n, menú móvil y buscador. Sin dependencias. */
(function () {
  "use strict";

  var DICT = window.HOTELMATCH_I18N;
  var CFG = window.HOTELMATCH_CONFIG;
  var DEFAULT_LANG = "es-419";
  var STORAGE_KEY = "hotelmatch.lang";

  function detectLang() {
    var saved = null;
    try { saved = localStorage.getItem(STORAGE_KEY); } catch (e) { /* almacenamiento bloqueado */ }
    if (saved && DICT[saved]) return saved;
    var nav = (navigator.language || "").toLowerCase();
    return nav.indexOf("en") === 0 ? "en-US" : DEFAULT_LANG;
  }

  var lang = detectLang();

  function t(key) {
    return (DICT[lang] && DICT[lang][key]) || DICT[DEFAULT_LANG][key] || key;
  }

  function applyLang(next) {
    lang = DICT[next] ? next : DEFAULT_LANG;
    try { localStorage.setItem(STORAGE_KEY, lang); } catch (e) { /* ignorar */ }
    document.documentElement.lang = lang;

    document.querySelectorAll("[data-i18n]").forEach(function (el) {
      el.textContent = t(el.getAttribute("data-i18n"));
    });
    document.querySelectorAll("[data-i18n-attr]").forEach(function (el) {
      // formato: "atributo:clave;atributo2:clave2"
      el.getAttribute("data-i18n-attr").split(";").forEach(function (pair) {
        var parts = pair.split(":");
        if (parts.length === 2) el.setAttribute(parts[0].trim(), t(parts[1].trim()));
      });
    });
    var title = t("meta.title");
    document.title = document.body.getAttribute("data-title-key")
      ? t(document.body.getAttribute("data-title-key"))
      : title;
    var metaDesc = document.querySelector('meta[name="description"]');
    if (metaDesc) metaDesc.setAttribute("content", t("meta.description"));

    document.querySelectorAll(".lang-btn").forEach(function (btn) {
      btn.setAttribute("aria-pressed", String(btn.getAttribute("data-lang") === lang));
    });
  }

  function bindLang() {
    document.querySelectorAll(".lang-btn").forEach(function (btn) {
      btn.addEventListener("click", function () { applyLang(btn.getAttribute("data-lang")); });
    });
  }

  function bindMenu() {
    var toggle = document.getElementById("menu-toggle");
    var menu = document.getElementById("site-menu");
    if (!toggle || !menu) return;
    toggle.addEventListener("click", function () {
      var open = menu.classList.toggle("is-open");
      toggle.setAttribute("aria-expanded", String(open));
    });
    menu.addEventListener("click", function (e) {
      if (e.target.tagName === "A") {
        menu.classList.remove("is-open");
        toggle.setAttribute("aria-expanded", "false");
      }
    });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && menu.classList.contains("is-open")) {
        menu.classList.remove("is-open");
        toggle.setAttribute("aria-expanded", "false");
        toggle.focus();
      }
    });
  }

  function isoToday() {
    var d = new Date();
    var m = String(d.getMonth() + 1).padStart(2, "0");
    var day = String(d.getDate()).padStart(2, "0");
    return d.getFullYear() + "-" + m + "-" + day;
  }

  function bindSearch() {
    var form = document.getElementById("search-form");
    if (!form) return;
    var checkIn = form.elements.checkIn;
    var checkOut = form.elements.checkOut;
    var error = document.getElementById("search-error");
    var today = isoToday();
    checkIn.min = today;
    checkOut.min = today;

    checkIn.addEventListener("change", function () {
      if (checkIn.value) checkOut.min = checkIn.value;
    });

    function fail(key, field) {
      error.textContent = t(key);
      error.hidden = false;
      field.setAttribute("aria-invalid", "true");
      field.focus();
    }

    form.addEventListener("submit", function (e) {
      e.preventDefault();
      error.hidden = true;
      checkIn.removeAttribute("aria-invalid");
      checkOut.removeAttribute("aria-invalid");

      if (!checkIn.value) return fail("search.err.required", checkIn);
      if (!checkOut.value) return fail("search.err.required", checkOut);
      if (checkIn.value < today) return fail("search.err.past", checkIn);
      if (checkOut.value <= checkIn.value) return fail("search.err.dates", checkOut);

      // Solo viajan parámetros del viaje (nunca datos personales) hacia la app Angular.
      var params = new URLSearchParams({
        checkIn: checkIn.value,
        checkOut: checkOut.value,
        guests: form.elements.guests.value,
        purpose: form.elements.purpose.value
      });
      window.location.href = CFG.APP_URL + "/buscar?" + params.toString();
    });
  }

  function bindLinks() {
    document.querySelectorAll("[data-href]").forEach(function (a) {
      var key = a.getAttribute("data-href");
      var map = {
        app: CFG.APP_URL + "/",
        register: CFG.APP_URL + "/registro",
        login: CFG.APP_URL + "/ingresar",
        registerHotel: CFG.APP_URL + "/registro?rol=hotel",
        api: CFG.API_DOCS_URL,
        repo: CFG.REPO_URL
      };
      if (map[key]) a.setAttribute("href", map[key]);
    });
    var year = document.getElementById("year");
    if (year) year.textContent = String(new Date().getFullYear());
  }

  document.addEventListener("DOMContentLoaded", function () {
    bindLang();
    bindMenu();
    bindSearch();
    bindLinks();
    applyLang(lang);
  });
})();
