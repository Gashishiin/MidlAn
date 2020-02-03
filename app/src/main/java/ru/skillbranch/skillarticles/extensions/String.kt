package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    if (this == null || this.isEmpty() || substr.isEmpty()) return emptyList()

    val resultList = mutableListOf<Int>()
    var searchPosition = 0
    var foundStartPosition = this.indexOf(substr, searchPosition, ignoreCase)
    while (foundStartPosition >= 0) {
        resultList.add(foundStartPosition)
        searchPosition = foundStartPosition + substr.length
        foundStartPosition = this.indexOf(substr, searchPosition, ignoreCase)
    }
    return resultList
}