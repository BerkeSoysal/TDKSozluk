import android.util.Log

class TrieNode {
    val children = Array<TrieNode?>(38) { null } // Fixed-size array for Turkish alphabet (26 English + 9 extra)
    var isEndOfWord: Boolean = false
    var originalWord: String?= null
}

class Trie {
    private val root = TrieNode()
    // Normalize Turkish characters to lowercase
    private fun normalizeTurkishChar(c: Char): Char {
        return when (c) {
            'Ç', 'ç' -> 'ç'
            'Ğ', 'ğ' -> 'ğ'
            'İ' -> 'i' // Turkish-specific: 'İ' -> 'i'
            'I' -> 'ı' // Turkish-specific: 'I' -> 'ı'
            'Ö', 'ö' -> 'ö'
            'Ş', 'ş' -> 'ş'
            'Ü', 'ü' -> 'ü'
            'Â', 'â' -> 'â'
            'Î', 'î' -> 'î'
            'Û', 'û' -> 'û'
            else -> c.lowercaseChar() // Default for other characters
        }
    }
    // Map Turkish characters to array indices
    private fun charToIndex(c: Char): Int {
        return when (normalizeTurkishChar(c)) {
            'a' -> 0
            'b' -> 1
            'c' -> 2
            'ç' -> 3
            'd' -> 4
            'e' -> 5
            'f' -> 6
            'g' -> 7
            'ğ' -> 8
            'h' -> 9
            'ı' -> 10
            'i' -> 11
            'j' -> 12
            'k' -> 13
            'l' -> 14
            'm' -> 15
            'n' -> 16
            'o' -> 17
            'ö' -> 18
            'p' -> 19
            'r' -> 20
            's' -> 21
            'ş' -> 22
            't' -> 23
            'u' -> 24
            'ü' -> 25
            'v' -> 26
            'y' -> 27
            'z' -> 28
            'â' -> 29
            'î' -> 30
            'û' -> 31
            ' ' -> 32
            '/' -> 33
            '-' -> 34
            '.' -> 35
            '\'' -> 36
            else -> 37
        }
    }

    // Insert a word into the Trie
    fun insert(word: String) {
        var current = root
        for (char in word.trim()) {
            val index = charToIndex(char)
            if(index ==current.children.size-1) {
                Log.d("Trie", "$word $char")
            }
            if (current.children[index] == null) {
                current.children[index] = TrieNode()
            }
            current = current.children[index]!!
        }
        current.isEndOfWord = true
        current.originalWord = word
    }

    // Search for up to 5 words with a given prefix
    fun searchByPrefix(prefix: String): List<String> {
        val result = mutableListOf<String>()
        var current = root

        // Navigate to the node representing the last character of the prefix
        for (char in prefix) {
            val index = charToIndex(char)
            if (current.children[index] == null) return result // Prefix not found
            current = current.children[index]!!
        }

        // Perform a DFS to find up to 5 words
        findWords(current, prefix, result)
        return result
    }

    // Helper function to perform DFS and collect words
    private fun findWords(node: TrieNode?, prefix: String, result: MutableList<String>) {
        if (result.size == 5 || node == null) return // Stop if 5 words are found
        if (node.isEndOfWord) result.add(node.originalWord!!)
        for ((index, child) in node.children.withIndex()) {
            if (child != null) {
                val nextChar = if (index < 26) 'a' + index else listOf('ç', 'ğ', 'ı', 'ö', 'ş', 'ü', 'â', 'î', 'û')[index - 26]
                findWords(child, prefix + nextChar, result)
            }
        }
    }
}
