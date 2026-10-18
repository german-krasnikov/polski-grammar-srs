// Generated from swipe.logic; source-sha256=4944f9411f45dd1f2c6bbc49b7009b06d7fef2bfd292268e5cfe1aa9479a845f
// contract-sha256=11fb3b7da4d623e677053e38e260d0c1847f6b5679ec9c10edc03e362ba251e8
import { validateQueryValue } from '@biome-dsl/kit';
// Generated from: swipe.logic
// Decision: rating
// Do not edit — regenerate from DSL source.

function decision_rating(host) {
  if (host.swipeBelowThreshold() === true) return "none";
  if (host.swipeYDominates() === true) return "none";
  if (host.swipeDxPositive() === true) return "good";
  if (!(host.swipeDxPositive() === true)) return "again";
  return "none";
}

const __facts = Object.fromEntries([["swipe.dxPositive",{"id":"swipe.dxPositive","resultType":"bool","member":"swipeDxPositive"}],["swipe.belowThreshold",{"id":"swipe.belowThreshold","resultType":"bool","member":"swipeBelowThreshold"}],["swipe.yDominates",{"id":"swipe.yDominates","resultType":"bool","member":"swipeYDominates"}]]);
function __capture(input, ids) {
  const host = Object.create(null);
  for (const id of ids) {
    const fact = __facts[id];
    if (typeof input?.[fact.member] !== 'function') throw Object.assign(new Error('Missing getter: ' + id), { code: 'LOGIC_INPUT_CONTRACT', factId: id });
    const value = validateQueryValue(fact, input[fact.member]());
    host[fact.member] = () => value;
  }
  return host;
}
export function rating(input) {
  const host = __capture(input, ["swipe.belowThreshold","swipe.dxPositive","swipe.yDominates"]);
  return decision_rating(host);
}
const __values = values => Object.fromEntries(Object.entries(values ?? {}).map(([key, value]) => [key, () => value]));
export const policy = Object.freeze({ rating: input => rating(__values(input)) });
export const RatingValues = Object.freeze(["again","good","none"]);
