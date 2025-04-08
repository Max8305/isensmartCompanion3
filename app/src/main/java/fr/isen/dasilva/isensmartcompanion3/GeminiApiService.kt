package fr.isen.dasilva.isensmartcompanion3

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object GeminiApiService {
    private const val API_KEY = "AIzaSyC1QH1HKWVh4VM2eD6Hnt3mhCfoEqYBUbM" // 🔴 Remplace par ta clé API
    private const val API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=$API_KEY"

    private val client = OkHttpClient()

    fun getResponse(question: String, callback: (String) -> Unit) {
        val requestBody = """
            {
                "contents": [{
                    "parts": [{
                        "text": "$question"
                    }]
                }]
            }
        """.trimIndent()

        println("📡 Envoi de la requête à Gemini 1.5...")
        println("📨 Corps de la requête : $requestBody")

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("User-Agent", "ISENSmartCompanion/1.0")
            .post(RequestBody.Companion.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Erreur réseau : ${e.message}")
                callback("Erreur : Impossible de contacter le serveur.")
            }

            override fun onResponse(call: Call, response: Response) {
                println("✅ Réponse reçue (Code HTTP: ${response.code})")

                val responseBody = response.body?.string()
                if (responseBody.isNullOrBlank()) {
                    println("⚠ Réponse vide de l'API")
                    callback("Erreur : Réponse vide de l'API.")
                    return
                }

                println("📥 Réponse brute : $responseBody")

                try {
                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val firstPart = parts?.optJSONObject(0)
                    val reply = firstPart?.optString("text")

                    println("📝 Réponse extraite : $reply")
                    callback(reply ?: "Je n'ai pas compris la question.")
                } catch (e: Exception) {
                    println("❌ Erreur lors du parsing JSON : ${e.message}")
                    callback("Erreur : Format de réponse inattendu.")
                }
            }
        })
    }
}