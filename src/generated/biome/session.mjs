// Generated from session.logic; source-sha256=f535abb2731a42a070bcb8c07dd91891325cfdefc832f4d5bb90930b61e30884
// contract-sha256=7eea90644d523369085c2ff22812dc978e2f8ab6ca78a3e74e6b05a5efefc8ba
function validateQueryValue(contract, value) {
  const type = contract.resultType;
  const valid = type === 'bool' ? typeof value === 'boolean'
    : type === 'number' ? typeof value === 'number' && Number.isFinite(value)
    : type === 'string' ? typeof value === 'string'
    : typeof value === 'string' && (contract.enumValues?.indexOf(value) ?? -1) >= 0;
  const bad = expected => Object.assign(new Error(`Fact ${contract.id}: expected ${expected}, received ${String(value)}`), { code: 'LOGIC_INPUT_CONTRACT', factId: contract.id, expected, actual: value });
  if (!valid) throw bad(type === 'number' ? 'finite number' : type);
  if (type === 'number' && contract.domain) {
    const { min, max, integer } = contract.domain;
    if ((min !== undefined && value < min) || (max !== undefined && value > max) || (integer && !Number.isSafeInteger(value))) throw bad(JSON.stringify(contract.domain));
  }
  return value;
}
// Generated from: session.logic
// Decision: countsCorrect
// Do not edit — regenerate from DSL source.

function decision_countsCorrect(host) {
  if ((host.reviewHasTypedResult() === true) && (!(host.reviewTypedCorrect() === true))) return false;
  if ((!(host.reviewHasTypedResult() === true)) && (host.reviewRating() === "again")) return false;
  return true;
}

const __facts = Object.fromEntries([["review.rating",{"id":"review.rating","resultType":"enum","enumValues":["again","hard","good","easy"],"member":"reviewRating"}],["review.hasTypedResult",{"id":"review.hasTypedResult","resultType":"bool","member":"reviewHasTypedResult"}],["review.typedCorrect",{"id":"review.typedCorrect","resultType":"bool","member":"reviewTypedCorrect"}]]);
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
export function countsCorrect(input) {
  const host = __capture(input, ["review.hasTypedResult","review.rating","review.typedCorrect"]);
  return decision_countsCorrect(host);
}
const __values = values => Object.fromEntries(Object.entries(values ?? {}).map(([key, value]) => [key, () => value]));
export const policy = Object.freeze({ countsCorrect: input => countsCorrect(__values(input)) });
