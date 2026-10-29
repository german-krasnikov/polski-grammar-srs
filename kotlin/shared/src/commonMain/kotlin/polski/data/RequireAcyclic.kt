package polski.data

/**
 * Fails on the first prerequisite cycle. A name that is no key is a leaf: the data contract rejects it (`[ref SkillRow]`).
 * Depth-first, linear in nodes plus edges; the recursion is as deep as the longest chain of prerequisites.
 */
internal fun <T> requireAcyclic(prerequisites: Map<T, List<T>>) {
    val done = HashMap<T, Boolean>() // false: on the current path, true: all prerequisites checked
    fun visit(id: T) {
        when (done[id]) {
            true -> return
            false -> error("prerequisite cycle through $id")
            null -> done[id] = false
        }
        prerequisites[id].orEmpty().forEach(::visit)
        done[id] = true
    }
    prerequisites.keys.forEach(::visit)
}
