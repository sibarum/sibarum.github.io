// Counts page views and a few actions with GoatCounter (https://www.goatcounter.com): no cookies, nothing stored on the
// visitor's computer, so no consent banner. The dashboard is https://sibarum.goatcounter.com. Any page can load it:
//   <script src="https://sibarum.github.io/analytics.js" defer></script>
// A page on another host is counted under that host's name (the path reads "otherhost.github.io/docs/..."), so pages
// elsewhere, such as the framework docs, can share the one dashboard.
//
// The actions, each an event in the dashboard:
//   copy/<id>         a Copy button pressed, or the text of a command selected and copied by hand; <id> is the
//                     element the command is in, such as copy/install-command or copy/linux-only
//   platform/<name>   the Windows or Linux switch on the install box clicked
//   open/<id>         one of the <details> sections opened, such as open/uninstall
//   out/<url>         a link to another site followed, such as out/github.com/sibarum/vexelray-dist
//   file/<path>       a file on this site opened from a link, such as file/install.sh
//
// GoatCounter itself counts nothing from localhost or a file: URL, so testing a page locally adds nothing.
(function () {
  "use strict";
  var SITE = "sibarum"; // the GoatCounter site code, the "sibarum" of sibarum.goatcounter.com. Empty counts nothing.
  if (!SITE || window.__vexelrayAnalytics) return;
  window.__vexelrayAnalytics = true;

  var HOME = "sibarum.github.io";
  var queue = [];
  var gc = window.goatcounter = window.goatcounter || {};
  if (location.host !== HOME) gc.path = function (p) { return location.host + p; };

  function count(name, title) {
    var event = { path: name, title: (title || "").replace(/\s+/g, " ").trim().slice(0, 200), event: true };
    if (typeof gc.count === "function") gc.count(event); else queue.push(event);
  }

  var script = document.createElement("script");
  script.async = true;
  script.src = "https://gc.zgo.at/count.js";
  script.setAttribute("data-goatcounter", "https://" + SITE + ".goatcounter.com/count");
  script.onload = function () { while (queue.length && typeof gc.count === "function") gc.count(queue.shift()); };
  document.head.appendChild(script);

  function closest(el, selector) {
    for (; el && el.nodeType === 1; el = el.parentNode) if (el.matches && el.matches(selector)) return el;
    return null;
  }

  // A Copy button names the element it copies in data-copy (the home page and the VexelRay page) or data-for (Nanobuf golf). Its own copy
  // may go through document.execCommand("copy"), which fires the copy event below as well: that one is not counted
  // twice.
  var buttonAt = 0;
  document.addEventListener("click", function (e) {
    var el = closest(e.target, "button[data-copy], button[data-for]");
    if (el) {
      var id = el.getAttribute("data-copy") || el.getAttribute("data-for");
      var source = document.getElementById(id);
      buttonAt = Date.now();
      count("copy/" + id, source && source.textContent);
      return;
    }
    el = closest(e.target, "[data-platform]");
    if (el) { count("platform/" + el.getAttribute("data-platform"), el.textContent); return; }
    el = closest(e.target, "a[href]");
    if (!el || !/^https?:$/.test(el.protocol)) return;
    var path = el.pathname.replace(/\/$/, "");
    if (el.host !== location.host) count("out/" + el.host + path, el.textContent);
    else if (path && !/\.html?$/.test(path)) count("file" + path, el.textContent);
  }, true);

  document.addEventListener("copy", function () {
    if (Date.now() - buttonAt < 1000) return;
    var sel = window.getSelection();
    var el = sel && sel.rangeCount && closest(sel.getRangeAt(0).commonAncestorContainer, "code[id], pre[id]");
    if (el) count("copy/" + el.id, el.textContent);
  }, true);

  // A <details> opened by the visitor, or by a link to its id. "toggle" does not bubble, so it is caught on the way
  // down.
  document.addEventListener("toggle", function (e) {
    var d = e.target;
    if (d.tagName !== "DETAILS" || !d.open) return;
    var summary = d.querySelector("summary");
    count("open/" + (d.id || "details"), summary && summary.textContent);
  }, true);
})();
