package com.example.visionmate.chatbot

import opennlp.tools.tokenize.SimpleTokenizer
import kotlin.collections.HashMap

class TextVectorizer {

    private val tokenizer = SimpleTokenizer.INSTANCE
    private val tfidf = TFIDF()

    fun tokenizeText(text: String): Array<String> {
        return tokenizer.tokenize(text)
    }

    fun computeBagOfWords(tokens: Array<String>): Map<String, Int> {
        val wordFrequencyMap = HashMap<String, Int>()
        for (token in tokens) {
            wordFrequencyMap[token] = wordFrequencyMap.getOrDefault(token, 0) + 1
        }
        return wordFrequencyMap
    }

    fun computeTFIDF(text: String, corpus: List<Map<String, Int>>): Map<String, Double> {
        val tokens = tokenizeText(text)
        val wordFrequencyMap = computeBagOfWords(tokens)
        val tf = tfidf.computeTF(wordFrequencyMap, tokens.size)
        val idf = tfidf.computeIDF(corpus)
        return tfidf.computeTFIDF(tf, idf)
    }

    fun parseTFIDF(tfidfString: String): Map<String, Double> {
        return tfidfString.split(";").associate {
            val parts = it.split(":")
            parts[0] to parts[1].toDouble()
        }
    }



    // Convert TF-IDF Map to a String for Storage
    fun convertToString(tfidf: Map<String, Double>): String {
        return tfidf.entries.joinToString(";") { "${it.key}:${it.value}" }
    }
}