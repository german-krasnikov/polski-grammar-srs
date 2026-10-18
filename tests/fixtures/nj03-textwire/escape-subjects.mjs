// Subjects for escape.sources — JS implementation of PolskiTextCodec escaping.
// Matches the Kotlin PolskiTextCodec.escapeText / unescapeText exactly.

export function escapeText(input) {
    let out = '';
    for (const ch of input) {
        const cp = ch.codePointAt(0);
        if (ch === '\\') out += '\\\\';
        else if (ch === '\t') out += '\\t';
        else if (ch === '\n') out += '\\n';
        else if (ch === '\r') out += '\\r';
        else if (ch === '$') out += '\\$';
        else if (cp <= 0x1F || (cp >= 0x7F && cp <= 0x9F))
            out += '\\u' + cp.toString(16).toUpperCase().padStart(4, '0');
        else out += ch;
    }
    return out;
}

export function unescapeText(input) {
    let out = '';
    for (let i = 0; i < input.length; i++) {
        if (input[i] === '\\') {
            const next = input[++i];
            if (next === '\\') out += '\\';
            else if (next === 't') out += '\t';
            else if (next === 'n') out += '\n';
            else if (next === 'r') out += '\r';
            else if (next === '$') out += '$';
            else if (next === 'u') {
                const hex = input.slice(i + 1, i + 5);
                out += String.fromCharCode(parseInt(hex, 16));
                i += 4;
            } else return null; // invalid
        } else {
            out += input[i];
        }
    }
    return out;
}
