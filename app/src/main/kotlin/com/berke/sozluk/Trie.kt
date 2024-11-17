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
            'İ', 'i' -> 'i' // Turkish-specific: 'İ' -> 'i'
            'I', 'ı' -> 'ı' // Turkish-specific: 'I' -> 'ı'
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
            'd' -> 3
            'e' -> 4
            'f' -> 5
            'g' -> 6
            'h' -> 7
            'i' -> 8
            'j' -> 9
            'k' -> 10
            'l' -> 11
            'm' -> 12
            'n' -> 13
            'o' -> 14
            'p' -> 15
            'r' -> 16
            's' -> 17
            't' -> 18
            'u' -> 19
            'v' -> 20
            'y' -> 21
            'z' -> 22
            'ç' -> 23
            'ğ' -> 24
            'ı' -> 25
            'ö' -> 26
            'ş' -> 27
            'ü' -> 28
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
                val nextChar = when (index) {
                    in 0..22 -> 'a' + index // English alphabet indices
                    23 -> 'ç'
                    24 -> 'ğ'
                    25 -> 'ı'
                    26 -> 'ö'
                    27 -> 'ş'
                    28 -> 'ü'
                    29 -> 'â'
                    30 -> 'î'
                    31 -> 'û'
                    32 -> ' '
                    33 -> '/'
                    34 -> '-'
                    35 -> '.'
                    36 -> '\''
                    else -> '?' // Fallback for unexpected cases
                }
                findWords(child, prefix + nextChar, result)
            }
        }
    }
}
