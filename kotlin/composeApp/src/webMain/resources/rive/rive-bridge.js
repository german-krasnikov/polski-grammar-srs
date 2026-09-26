// Hand-written glue between the Kotlin/JS+Wasm web host and the vendored Rive runtime
// (rive.js + rive.wasm, from @rive-app/canvas-lite 2.43.1, THIRD_PARTY/credits.md).
//
// The host never calls into this file directly (no js()/dynamic Kotlin<->JS bindings, so the
// same Kotlin code works unmodified on both the JS and Wasm targets). Instead it toggles the
// `data-rive-effect="<kind>:<sequence>"` attribute on #polski-rive-overlay; this script watches
// that attribute with a MutationObserver and drives the actual Rive API.
(function () {
  var EFFECTS = {
    remembered: { src: 'rive/confetti.riv', stateMachine: 'State Machine 1', input: 'Trigger explosion' },
    again: { src: 'rive/again.riv', stateMachine: 'Swipe to delete', input: 'Trigger Delete' },
  };
  var wasmConfigured = false;
  var current = null; // { instance, hideTimer }

  function withRive(onReady) {
    if (window.rive && window.rive.Rive) { onReady(window.rive); return; }
    var script = document.createElement('script');
    script.src = 'rive/rive.js';
    script.onload = function () { if (window.rive && window.rive.Rive) onReady(window.rive); };
    script.onerror = function () {};
    document.head.appendChild(script);
  }

  function stopCurrent() {
    if (!current) return;
    if (current.hideTimer) clearTimeout(current.hideTimer);
    try { current.instance.cleanup(); } catch (e) {}
    current = null;
  }

  function play(overlay, canvas, kind) {
    var spec = EFFECTS[kind];
    if (!spec) return;
    withRive(function (rive) {
      try {
        if (!wasmConfigured) {
          rive.RuntimeLoader.setWasmUrl('rive/rive.wasm');
          wasmConfigured = true;
        }
        stopCurrent();
        var rect = overlay.getBoundingClientRect();
        canvas.width = Math.max(1, Math.round(rect.width));
        canvas.height = Math.max(1, Math.round(rect.height));
        var instance = new rive.Rive({
          src: spec.src,
          canvas: canvas,
          stateMachines: spec.stateMachine,
          autoplay: true,
          onLoad: function () {
            try {
              var inputs = instance.stateMachineInputs(spec.stateMachine) || [];
              for (var i = 0; i < inputs.length; i++) {
                if (inputs[i].name === spec.input) { inputs[i].fire(); break; }
              }
            } catch (e) {}
          },
        });
        var hideTimer = setTimeout(function () {
          overlay.style.display = 'none';
          stopCurrent();
        }, 2600);
        current = { instance: instance, hideTimer: hideTimer };
      } catch (e) {
        overlay.style.display = 'none';
      }
    });
  }

  function attach() {
    var overlay = document.getElementById('polski-rive-overlay');
    if (!overlay || overlay.__polskiObserved) return;
    overlay.__polskiObserved = true;
    var canvas = overlay.querySelector('canvas');
    var react = function () {
      var value = overlay.getAttribute('data-rive-effect') || '';
      var kind = value.split(':')[0];
      if (kind) play(overlay, canvas, kind);
    };
    var observer = new MutationObserver(react);
    observer.observe(overlay, { attributes: true, attributeFilter: ['data-rive-effect'] });
    // The attribute may already have been set before this (async-loaded) script attached.
    react();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', attach);
  } else {
    attach();
  }
  // The overlay <div> is created lazily by Kotlin after this script starts loading; retry once
  // shortly after in case attach() ran before it existed.
  setTimeout(attach, 0);
})();
