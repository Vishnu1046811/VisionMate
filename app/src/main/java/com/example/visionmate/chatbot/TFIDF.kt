package com.example.visionmate.chatbot

import kotlin.math.ln

class TFIDF {

    fun computeTF(words: Map<String, Int>, totalWords: Int): Map<String, Double> {
        val tfMap = mutableMapOf<String, Double>()
        for ((word, count) in words) {
            tfMap[word] = count.toDouble() / totalWords
        }
        return tfMap
    }

    fun computeIDF(corpus: List<Map<String, Int>>): Map<String, Double> {
        val idfMap = mutableMapOf<String, Double>()
        val totalDocuments = corpus.size

        // Calculate the IDF for each word in the corpus
        for (document in corpus) {
            for (word in document.keys) {
                val docFrequency = corpus.count { it.containsKey(word) }
                idfMap[word] = idfMap.getOrDefault(word, 0.0) + ln(totalDocuments.toDouble() / (docFrequency.toDouble() + 1))
            }
        }
        return idfMap
    }

    fun computeTFIDF(tf: Map<String, Double>, idf: Map<String, Double>): Map<String, Double> {
        val tfidfMap = mutableMapOf<String, Double>()
        for ((word, tfValue) in tf) {
            val idfValue = idf.getOrDefault(word, 0.0)
            tfidfMap[word] = tfValue * idfValue
        }
        return tfidfMap
    }
}