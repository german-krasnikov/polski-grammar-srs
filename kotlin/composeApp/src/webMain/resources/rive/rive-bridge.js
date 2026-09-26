// Hand-written glue between the Kotlin/JS+Wasm web host and the vendored Rive runtime
// (rive.js + rive.wasm, from @rive-app/canvas-lite 2.43.1, THIRD_PARTY/credits.md).
//
// The host never calls into this file directly (no js()/dynamic Kotlin<->JS bindings, so the
// same Kotlin code works unmodified on both the JS and Wasm targets). Instead it toggles DOM
// attributes on three overlay elements; this script watches those attributes and drives the
// actual Rive API:
//   #polski-rive-overlay  data-rive-effect="<remembered|again>:<sequence>"   (rating cue)
//   #polski-rive-chain    data-rive-chain="<sequence>"                       (chain-complete "Tada")
//   .card-flip-rings      data-rive-rings="<0|1>"                           (flip/reveal-in-progress rings)
//   <body>                data-rive-prewarm="1"                              (v3/D: warm the runtime once, idly)
//   <body>                data-rive-dispose-all="<sequence>"                 (v3/C: Animations turned off — tear down everything now)
(function () {
  // v2 FC2-09: "again.riv" was replaced (it used to paint an opaque scene, RiveCatalog.md §0.1).
  // "Remembered" now plays confetti together with the new file's "Check" trigger (accepted
  // FlipCardRivePlan.md §12.7 recommendation); "Again" plays only its "Error" trigger.
  var RATING_EFFECTS = {
    remembered: [
      { src: 'rive/confetti.riv', stateMachine: 'State Machine 1', input: 'Trigger explosion' },
      { src: 'rive/again.riv', stateMachine: 'State Machine 1', input: 'Check' },
    ],
    again: [
      { src: 'rive/again.riv', stateMachine: 'State Machine 1', input: 'Error' },
    ],
  };
  var wasmConfigured = false;
  var currentRating = []; // [{instance, hideTimer}] for the active rating layer's canvases

  function withRive(onReady) {
    if (window.rive && window.rive.Rive) { onReady(window.rive); return; }
    var script = document.createElement('script');
    script.src = 'rive/rive.js';
    script.onload = function () { if (window.rive && window.rive.Rive) onReady(window.rive); };
    script.onerror = function () {};
    document.head.appendChild(script);
  }

  function configureWasm(rive) {
    if (wasmConfigured) return;
    rive.RuntimeLoader.setWasmUrl('rive/rive.wasm');
    wasmConfigured = true;
  }

  function fireInput(instance, stateMachine, name) {
    try {
      var inputs = instance.stateMachineInputs(stateMachine) || [];
      for (var i = 0; i < inputs.length; i++) { if (inputs[i].name === name) { inputs[i].fire(); break; } }
    } catch (e) {}
  }

  function stopRating() {
    currentRating.forEach(function (entry) {
      if (entry.hideTimer) clearTimeout(entry.hideTimer);
      try { entry.instance.cleanup(); } catch (e) {}
    });
    currentRating = [];
  }

  function playRating(overlay, canvases, kind) {
    var specs = RATING_EFFECTS[kind];
    if (!specs) return;
    withRive(function (rive) {
      try {
        configureWasm(rive);
        stopRating();
        var rect = overlay.getBoundingClientRect();
        var created = [];
        specs.forEach(function (spec, index) {
          var canvas = canvases[index];
          if (!canvas) return;
          canvas.width = Math.max(1, Math.round(rect.width));
          canvas.height = Math.max(1, Math.round(rect.height));
          var instance = new rive.Rive({
            src: spec.src,
            canvas: canvas,
            stateMachines: spec.stateMachine,
            autoplay: true,
            onLoad: function () { fireInput(instance, spec.stateMachine, spec.input); },
          });
          created.push({ instance: instance });
        });
        var hideTimer = setTimeout(function () { overlay.style.display = 'none'; stopRating(); }, 2600);
        created.forEach(function (entry) { entry.hideTimer = hideTimer; });
        currentRating = created;
      } catch (e) {
        overlay.style.display = 'none';
      }
    });
  }

  var currentChain = null;

  function playChain(overlay, canvas) {
    withRive(function (rive) {
      try {
        configureWasm(rive);
        if (currentChain) { try { currentChain.cleanup(); } catch (e) {} }
        var rect = overlay.getBoundingClientRect();
        canvas.width = Math.max(1, Math.round(rect.width));
        canvas.height = Math.max(1, Math.round(rect.height));
        currentChain = new rive.Rive({
          src: 'rive/chain-complete.riv',
          canvas: canvas,
          artboard: 'Tada',
          animations: ['Reveal'],
          autoplay: true,
        });
        setTimeout(function () {
          overlay.style.display = 'none';
          if (currentChain) { try { currentChain.cleanup(); } catch (e) {} currentChain = null; }
        }, 3200);
      } catch (e) {
        overlay.style.display = 'none';
      }
    });
  }

  // Rings toggle a boolean on a persistent-while-flipping instance, not a one-shot trigger; the
  // canvas element itself is recreated by every full card re-render (see RiveEffectOverlay.kt's
  // doc comment). The instance is cached on the canvas node (`__polskiRive`) so a re-render that
  // reuses the same node doesn't restart it; when the node itself is discarded instead, the
  // observer in attach() below calls `.cleanup()` on it (see disposeDetachedRings) — otherwise its
  // own requestAnimationFrame draw loop would keep running forever against a detached canvas.
  function playRings(canvas, expanded) {
    if (!canvas) return;
    withRive(function (rive) {
      configureWasm(rive);
      if (canvas.__polskiRive) {
        setRingsBool(canvas.__polskiRive, expanded);
        return;
      }
      try {
        var rect = canvas.getBoundingClientRect();
        canvas.width = Math.max(1, Math.round(rect.width));
        canvas.height = Math.max(1, Math.round(rect.height));
        var instance = new rive.Rive({
          src: 'rive/rings.riv',
          canvas: canvas,
          stateMachines: 'State Machine 1',
          autoplay: true,
          onLoad: function () { setRingsBool(instance, expanded); },
        });
        canvas.__polskiRive = instance;
      } catch (e) {}
    });
  }

  // v3/D: warms rive.js + rive.wasm + one throwaway instance well before the first real effect,
  // so that first trigger never pays the fetch/compile cost on the same frames as its own CSS
  // motion (the diagnosed cause of first-reveal/first-flip jank). `rings.riv` is reused rather
  // than fetching a 4th asset just for warmth. Runs at most once per page.
  var prewarmed = false;
  var prewarmScheduled = false;
  function prewarm() {
    if (prewarmed) return;
    prewarmed = true;
    withRive(function (rive) {
      try {
        configureWasm(rive);
        var canvas = document.createElement('canvas');
        canvas.width = 1; canvas.height = 1;
        var instance = new rive.Rive({
          src: 'rive/rings.riv',
          canvas: canvas,
          stateMachines: 'State Machine 1',
          autoplay: false,
          onLoad: function () { try { instance.cleanup(); } catch (e) {} },
        });
      } catch (e) {}
    });
  }
  function schedulePrewarm() {
    if (prewarmScheduled) return;
    prewarmScheduled = true;
    if (window.requestIdleCallback) window.requestIdleCallback(prewarm, { timeout: 2000 });
    else setTimeout(prewarm, 200);
  }

  // v3/C: tears down every live Rive instance immediately when Animations is turned off, however
  // it got started (rating/chain/rings), and hides any overlay left visible mid-effect.
  function disposeAllRive() {
    stopRating();
    if (currentChain) { try { currentChain.cleanup(); } catch (e) {} currentChain = null; }
    var canvases = document.querySelectorAll('canvas');
    for (var i = 0; i < canvases.length; i++) {
      var canvas = canvases[i];
      if (canvas.__polskiRive) {
        try { canvas.__polskiRive.cleanup(); } catch (e) {}
        canvas.__polskiRive = null;
        window.__polskiRiveRingDisposals = (window.__polskiRiveRingDisposals || 0) + 1;
      }
    }
    ['polski-rive-overlay', 'polski-rive-chain', 'polski-rive-reveal'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) el.style.display = 'none';
    });
  }

  function setRingsBool(instance, expanded) {
    try {
      var inputs = instance.stateMachineInputs('State Machine 1') || [];
      for (var i = 0; i < inputs.length; i++) { if (inputs[i].name === 'IsExpanded') { inputs[i].value = expanded; break; } }
    } catch (e) {}
  }

  // Called on every node removed from the document (see attach()'s childList observation). The
  // ring cue's canvas is the only kind tagged with `__polskiRive` (rating/chain instances are
  // tracked in module-level vars instead, disposed by stopRating()/playChain()'s own cleanup
  // calls), so an unconditional scan for that tag — on the removed node itself and its descendants
  // — is enough to find and stop any ring instance whose canvas just left the DOM.
  function disposeDetachedRings(node) {
    if (!node || node.nodeType !== 1) return;
    var canvases = node.tagName === 'CANVAS' ? [node] : (node.querySelectorAll ? node.querySelectorAll('canvas') : []);
    for (var i = 0; i < canvases.length; i++) {
      var canvas = canvases[i];
      if (canvas.__polskiRive) {
        try { canvas.__polskiRive.cleanup(); } catch (e) {}
        canvas.__polskiRive = null;
        // Test-only counter (mirrors the `?flipDebugScale`/`?riveDisabled=1` convention of
        // exposing narrow, harmless hooks for Playwright): lets a regression test observe that a
        // discarded ring canvas's instance actually got torn down, without reaching into rive.js
        // internals to prove its RAF loop stopped.
        window.__polskiRiveRingDisposals = (window.__polskiRiveRingDisposals || 0) + 1;
      }
    }
  }

  function react(el) {
    if (el.id === 'polski-rive-overlay') {
      var value = el.getAttribute('data-rive-effect');
      if (value) playRating(el, el.querySelectorAll('canvas'), value.split(':')[0]);
    } else if (el.id === 'polski-rive-chain') {
      if (el.getAttribute('data-rive-chain')) playChain(el, el.querySelector('canvas'));
    } else if (el.classList && el.classList.contains('card-flip-rings')) {
      playRings(el.querySelector('canvas'), el.getAttribute('data-rive-rings') === '1');
    }
  }

  function scan() {
    ['#polski-rive-overlay[data-rive-effect]', '#polski-rive-chain[data-rive-chain]', '.card-flip-rings[data-rive-rings]']
      .forEach(function (selector) {
        var nodes = document.querySelectorAll(selector);
        for (var i = 0; i < nodes.length; i++) react(nodes[i]);
      });
    if (document.body && document.body.hasAttribute('data-rive-prewarm')) schedulePrewarm();
  }

  // A single subtree observer, set up once for the page's lifetime, replaces per-element
  // observers entirely. This matters for correctness, not just simplicity: the rating/chain
  // overlays are created lazily by Kotlin *after* this script starts loading (the very first
  // `ensureBridgeLoaded()` call can come from the ring cue, on the very first flip, well before
  // any rating happens) — a per-element observer set up once at load time would never find an
  // overlay that does not exist yet, and (unlike the ring cue) nothing ever re-attempts it after
  // that. A subtree observer has no such ordering requirement: it reacts to the attribute the
  // moment any current or future matching element gets it, regardless of creation order. The ring
  // cue's own element is additionally recreated on every card re-render (it lives inside
  // `.card-flip`), which a subtree observer also handles for free.
  function attach() {
    if (!document.body || document.body.__polskiRiveObserved) return;
    document.body.__polskiRiveObserved = true;
    new MutationObserver(function (mutations) {
      for (var i = 0; i < mutations.length; i++) {
        var mutation = mutations[i];
        if (mutation.type === 'childList') {
          for (var j = 0; j < mutation.removedNodes.length; j++) disposeDetachedRings(mutation.removedNodes[j]);
        } else if (mutation.attributeName === 'data-rive-prewarm') {
          schedulePrewarm();
        } else if (mutation.attributeName === 'data-rive-dispose-all') {
          disposeAllRive();
        } else {
          react(mutation.target);
        }
      }
    }).observe(document.body, {
      attributes: true,
      attributeFilter: ['data-rive-effect', 'data-rive-chain', 'data-rive-rings', 'data-rive-prewarm', 'data-rive-dispose-all'],
      childList: true,
      subtree: true,
    });
    scan(); // catch anything set before this observer existed
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', attach);
  } else {
    attach();
  }
  // document.body can be briefly unavailable extremely early in <head>-script execution; retry
  // once shortly after in case attach() ran before it existed.
  setTimeout(attach, 0);
})();
