// Generated from dsl/effects/card-effects.core-data by derive-rive-effects.mjs — do not edit.
// A plain global script, no IIFE and no module system: rive-bridge.js loads this with the same
// dynamic <script> trick it already uses to lazy-load rive.js, then reads these two globals.
var RATING_EFFECTS = {
  remembered: [
    { src: 'rive/confetti.riv', stateMachine: 'State Machine 1', input: 'Trigger explosion' },
    { src: 'rive/again.riv', stateMachine: 'State Machine 1', input: 'Check' },
  ],
  again: [
    { src: 'rive/again.riv', stateMachine: 'State Machine 1', input: 'Error' },
  ],
};
var CHAIN_COMPLETE = { src: 'rive/chain-complete.riv', artboard: 'Tada', animation: 'Reveal' };
