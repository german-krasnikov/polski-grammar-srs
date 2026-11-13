// DOM bridge shared by the Kotlin/JS and Kotlin/Wasm card hosts. Effect names and assets remain
// in the generated DSL table; this file owns only browser resources and cue cancellation.
(function () {
  var effectsData = null;
  var effectsWaiters = [];
  var effectsLoading = false;
  var riveWaiters = [];
  var riveLoading = false;
  var wasmConfigured = false;
  var ratingGeneration = 0;
  var chainGeneration = 0;
  var ratingTimer = null;
  var chainTimer = null;
  var currentRating = [];
  var currentChain = null;
  var lastRatingValue = '';
  var lastChainValue = '';

  function gated() {
    var root = document.documentElement;
    return document.hidden || root.getAttribute('data-motion') === 'reduced' ||
      root.getAttribute('data-animations') === 'off' || location.search.indexOf('riveDisabled=1') >= 0;
  }

  function loadScript(src, finished) {
    var script = document.createElement('script');
    script.src = src;
    script.onload = function () { finished(true); };
    script.onerror = function () { script.remove(); finished(false); };
    document.head.appendChild(script);
  }

  function withEffects(ready) {
    if (effectsData) { ready(effectsData); return; }
    effectsWaiters.push(ready);
    if (effectsLoading) return;
    effectsLoading = true;
    loadScript('rive/rive-effects.generated.js', function (loaded) {
      effectsLoading = false;
      if (loaded && window.RATING_EFFECTS && window.CHAIN_COMPLETE) {
        effectsData = { RATING_EFFECTS: window.RATING_EFFECTS, CHAIN_COMPLETE: window.CHAIN_COMPLETE };
      }
      var waiters = effectsWaiters.splice(0);
      waiters.forEach(function (callback) { callback(effectsData); });
    });
  }

  function withRive(ready) {
    if (window.rive && window.rive.Rive) { ready(window.rive); return; }
    riveWaiters.push(ready);
    if (riveLoading) return;
    riveLoading = true;
    loadScript('rive/rive.js', function (loaded) {
      riveLoading = false;
      var runtime = loaded && window.rive && window.rive.Rive ? window.rive : null;
      var waiters = riveWaiters.splice(0);
      waiters.forEach(function (callback) { callback(runtime); });
    });
  }

  function configureWasm(rive) {
    if (wasmConfigured) return;
    rive.RuntimeLoader.setWasmUrl('rive/rive.wasm');
    wasmConfigured = true;
  }

  function fireInput(instance, stateMachine, name) {
    try {
      var inputs = instance.stateMachineInputs(stateMachine) || [];
      for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].name === name) { inputs[i].fire(); break; }
      }
    } catch (e) {}
  }

  function clean(instance) { try { instance.cleanup(); } catch (e) {} }
  function stopRating() {
    currentRating.forEach(clean);
    currentRating = [];
  }
  function stopChain() {
    if (currentChain) clean(currentChain);
    currentChain = null;
  }
  function cancelRating() {
    ratingGeneration += 1;
    if (ratingTimer !== null) clearTimeout(ratingTimer);
    ratingTimer = null;
    stopRating();
    var overlay = document.getElementById('polski-rive-overlay');
    if (overlay) overlay.style.display = 'none';
  }
  function cancelChain() {
    chainGeneration += 1;
    if (chainTimer !== null) clearTimeout(chainTimer);
    chainTimer = null;
    stopChain();
    var overlay = document.getElementById('polski-rive-chain');
    if (overlay) overlay.style.display = 'none';
  }
  function current(overlay, attribute, value, generation, chain) {
    return (chain ? chainGeneration : ratingGeneration) === generation &&
      overlay.isConnected && overlay.getAttribute(attribute) === value && !gated();
  }

  function playRating(overlay, value) {
    cancelRating();
    var generation = ratingGeneration;
    var active = function () { return current(overlay, 'data-rive-effect', value, generation, false); };
    if (!active()) return;
    overlay.style.display = 'block';
    // Arm cleanup before either lazy script starts. Network errors and background throttling
    // cannot leave a visible layer waiting for an animation-completion event.
    ratingTimer = setTimeout(function () { if (active()) cancelRating(); }, 2600);
    withEffects(function (effects) {
      if (!active()) return;
      var specs = effects && effects.RATING_EFFECTS[value.split(':')[0]];
      if (!specs) { cancelRating(); return; }
      withRive(function (rive) {
        if (!active()) return;
        if (!rive) { cancelRating(); return; }
        try {
          configureWasm(rive);
          var rect = overlay.getBoundingClientRect();
          var canvases = overlay.querySelectorAll('canvas');
          specs.forEach(function (spec, index) {
            var canvas = canvases[index];
            if (!canvas || !active()) return;
            canvas.width = Math.max(1, Math.round(rect.width));
            canvas.height = Math.max(1, Math.round(rect.height));
            var instance = new rive.Rive({
              src: spec.src, canvas: canvas, stateMachines: spec.stateMachine, autoplay: true,
              onLoad: function () { if (active()) fireInput(instance, spec.stateMachine, spec.input); else if (instance) clean(instance); },
            });
            if (active()) currentRating.push(instance); else clean(instance);
          });
        } catch (e) { cancelRating(); }
      });
    });
  }

  function playChain(overlay, value) {
    cancelChain();
    var generation = chainGeneration;
    var active = function () { return current(overlay, 'data-rive-chain', value, generation, true); };
    if (!active()) return;
    overlay.style.display = 'block';
    chainTimer = setTimeout(function () { if (active()) cancelChain(); }, 3200);
    withEffects(function (effects) {
      if (!active()) return;
      if (!effects || !effects.CHAIN_COMPLETE) { cancelChain(); return; }
      withRive(function (rive) {
        if (!active()) return;
        if (!rive) { cancelChain(); return; }
        try {
          configureWasm(rive);
          var canvas = overlay.querySelector('canvas');
          if (!canvas) { cancelChain(); return; }
          var rect = overlay.getBoundingClientRect();
          canvas.width = Math.max(1, Math.round(rect.width));
          canvas.height = Math.max(1, Math.round(rect.height));
          var spec = effects.CHAIN_COMPLETE;
          var instance = new rive.Rive({
            src: spec.src, canvas: canvas, artboard: spec.artboard,
            animations: [spec.animation], autoplay: true,
            onLoad: function () { if (!active() && instance) clean(instance); },
          });
          if (active()) currentChain = instance; else clean(instance);
        } catch (e) { cancelChain(); }
      });
    });
  }

  var prewarmed = false;
  var prewarmScheduled = false;
  function prewarm() {
    prewarmScheduled = false;
    if (prewarmed || gated()) return;
    withRive(function (rive) {
      if (!rive || gated() || prewarmed) return;
      try {
        configureWasm(rive);
        prewarmed = true;
        var canvas = document.createElement('canvas');
        canvas.width = 1; canvas.height = 1;
        var instance = new rive.Rive({
          src: 'rive/confetti.riv', canvas: canvas, stateMachines: 'State Machine 1',
          autoplay: false, onLoad: function () { if (instance) clean(instance); },
        });
      } catch (e) { prewarmed = false; }
    });
  }
  function schedulePrewarm() {
    if (prewarmScheduled || prewarmed || gated()) return;
    prewarmScheduled = true;
    if (window.requestIdleCallback) window.requestIdleCallback(prewarm, { timeout: 2000 });
    else setTimeout(prewarm, 200);
  }

  function disposeAllRive() {
    cancelRating();
    cancelChain();
    [['polski-rive-overlay', 'data-rive-effect'], ['polski-rive-chain', 'data-rive-chain']]
      .forEach(function (pair) {
        var overlay = document.getElementById(pair[0]);
        if (overlay) { overlay.removeAttribute(pair[1]); overlay.removeAttribute('data-rive-owner'); }
      });
    lastRatingValue = '';
    lastChainValue = '';
  }

  function react(overlay) {
    if (overlay.id === 'polski-rive-overlay') {
      var rating = overlay.getAttribute('data-rive-effect') || '';
      if (rating === lastRatingValue) return;
      lastRatingValue = rating;
      if (!rating || gated()) { cancelRating(); overlay.style.display = 'none'; return; }
      playRating(overlay, rating);
    } else if (overlay.id === 'polski-rive-chain') {
      var chain = overlay.getAttribute('data-rive-chain') || '';
      if (chain === lastChainValue) return;
      lastChainValue = chain;
      if (!chain || gated()) { cancelChain(); overlay.style.display = 'none'; return; }
      playChain(overlay, chain);
    }
  }

  function attach() {
    if (!document.body || document.body.__polskiRiveObserved) return;
    document.body.__polskiRiveObserved = true;
    new MutationObserver(function (mutations) {
      for (var i = 0; i < mutations.length; i++) {
        var mutation = mutations[i];
        if (mutation.attributeName === 'data-rive-prewarm') schedulePrewarm();
        else if (mutation.attributeName === 'data-rive-dispose-all') disposeAllRive();
        else react(mutation.target);
      }
    }).observe(document.body, {
      attributes: true,
      attributeFilter: ['data-rive-effect', 'data-rive-chain', 'data-rive-prewarm', 'data-rive-dispose-all'],
      subtree: true,
    });
    new MutationObserver(function () { if (gated()) disposeAllRive(); }).observe(document.documentElement, {
      attributes: true, attributeFilter: ['data-motion', 'data-animations'],
    });
    document.addEventListener('visibilitychange', function () { if (document.hidden) disposeAllRive(); });
    var rating = document.getElementById('polski-rive-overlay');
    var chain = document.getElementById('polski-rive-chain');
    if (rating) react(rating);
    if (chain) react(chain);
    if (document.body.hasAttribute('data-rive-prewarm')) schedulePrewarm();
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', attach);
  else attach();
  setTimeout(attach, 0);
})();
