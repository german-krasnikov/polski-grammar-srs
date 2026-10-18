package polski.textwire

/** SHA-256 for input-only legacy test controls, shared by JVM, Native, JS and Wasm tests. */
internal fun legacyAdmissionSha256(bytes: ByteArray): String {
    val constantsHex =
        "428a2f9871374491b5c0fbcfe9b5dba5" +
            "3956c25b59f111f1923f82a4ab1c5ed5" +
            "d807aa9812835b01243185be550c7dc3" +
            "72be5d7480deb1fe9bdc06a7c19bf174" +
            "e49b69c1efbe47860fc19dc6240ca1cc" +
            "2de92c6f4a7484aa5cb0a9dc76f988da" +
            "983e5152a831c66db00327c8bf597fc7" +
            "c6e00bf3d5a7914706ca635114292967" +
            "27b70a852e1b21384d2c6dfc53380d13" +
            "650a7354766a0abb81c2c92e92722c85" +
            "a2bfe8a1a81a664bc24b8b70c76c51a3" +
            "d192e819d6990624f40e3585106aa070" +
            "19a4c1161e376c082748774c34b0bcb5" +
            "391c0cb34ed8aa4a5b9cca4f682e6ff3" +
            "748f82ee78a5636f84c878148cc70208" +
            "90befffa a4506ceb bef9a3f7 c67178f2".replace(" ", "")
    val constants = IntArray(64) { index -> constantsHex.substring(index * 8, index * 8 + 8).toLong(16).toInt() }
    val state = intArrayOf(
        0x6a09e667, 0xbb67ae85.toInt(), 0x3c6ef372, 0xa54ff53a.toInt(),
        0x510e527f, 0x9b05688c.toInt(), 0x1f83d9ab, 0x5be0cd19,
    )
    val bitLength = bytes.size.toLong() * 8
    require(bytes.size <= Int.MAX_VALUE - 72)
    val paddedLength = ((bytes.size + 72) / 64) * 64
    fun byteAt(index: Int): Int = when {
        index < bytes.size -> bytes[index].toInt() and 0xff
        index == bytes.size -> 0x80
        index >= paddedLength - 8 -> (bitLength ushr ((paddedLength - 1 - index) * 8)).toInt() and 0xff
        else -> 0
    }
    fun rotate(value: Int, bits: Int): Int = (value ushr bits) or (value shl (32 - bits))
    val words = IntArray(64)
    var offset = 0
    while (offset < paddedLength) {
        for (index in 0 until 16) {
            val base = offset + index * 4
            words[index] = (byteAt(base) shl 24) or (byteAt(base + 1) shl 16) or
                (byteAt(base + 2) shl 8) or byteAt(base + 3)
        }
        for (index in 16 until 64) {
            val a = words[index - 15]
            val b = words[index - 2]
            val s0 = rotate(a, 7) xor rotate(a, 18) xor (a ushr 3)
            val s1 = rotate(b, 17) xor rotate(b, 19) xor (b ushr 10)
            words[index] = words[index - 16] + s0 + words[index - 7] + s1
        }
        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]
        for (index in 0 until 64) {
            val s1 = rotate(e, 6) xor rotate(e, 11) xor rotate(e, 25)
            val choice = (e and f) xor (e.inv() and g)
            val t1 = h + s1 + choice + constants[index] + words[index]
            val s0 = rotate(a, 2) xor rotate(a, 13) xor rotate(a, 22)
            val majority = (a and b) xor (a and c) xor (b and c)
            val t2 = s0 + majority
            h = g
            g = f
            f = e
            e = d + t1
            d = c
            c = b
            b = a
            a = t1 + t2
        }
        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
        state[5] += f
        state[6] += g
        state[7] += h
        offset += 64
    }
    val hex = "0123456789abcdef"
    return buildString(64) {
        for (word in state) {
            for (shift in 28 downTo 0 step 4) append(hex[(word ushr shift) and 15])
        }
    }
}

internal fun legacyAdmissionMatches(label: String, raw: String): Boolean {
    val (size, digest) = legacyAdmissionLedger.getValue(label)
    val bytes = raw.encodeToByteArray()
    return bytes.size == size && legacyAdmissionSha256(bytes) == digest
}
