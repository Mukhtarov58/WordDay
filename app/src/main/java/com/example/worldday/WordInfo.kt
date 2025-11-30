package com.example.worldday


import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.time.LocalDate
import java.util.*


data class WordInfo(
    val uuid: String,
    val ulid: String,
    val request: String,
    val message: String,
    val links: Map<String, String>,
    val titles: List<String>,
    val images: List<Image>,
    val hideRq: Boolean
)

data class Image(
    val image: String,
    val blurImage: Boolean,
    val order: Int
)


object WordOfTheDay {

    val WORD_LIST = listOf(
        "Катавасия",
        "Синестезия",
        "Эвфония",
        "Эпифания",
        "Филигрань",
        "Инсинуация",
        "Апофеоз",
        "Лалила",
        "Элоквенция",
        "Собеседование"
    )

    // Получаем слово по дате (каждый день — одно и то же)
    fun getWordOfTheDay(): String {
        val dayOfYear = LocalDate.now().dayOfYear
        return WORD_LIST[dayOfYear % WORD_LIST.size]
    }

    // Генерация ULID-подобной строки (упрощённо)
    private fun generateULID(): String {
        val hex = "0123456789ABCDEF"
        return "01J" + (1..12).map { hex.random() }.joinToString("")
    }

    // Основной метод — suspend, чтобы работал с корутинами
    suspend fun fetchWordInfo(): WordInfo? = withContext(Dispatchers.IO) {
        val word = getWordOfTheDay()
        val url = "https://ru.wikipedia.org/api/rest_v1/page/html/$word"

        val (_, _, result) = url.httpGet()
            .header("User-Agent" to "WordOfTheDayApp 1.0")
            .responseString()

        val html = result.fold(
            success = { it },
            failure = { error ->
                error.exception.printStackTrace()
                return@withContext null
            }
        )

        val doc = Jsoup.parse(html)

        // Удаляем ненужные элементы
        doc.select("sup, table, .vector-feature-vector-wikibase, .infobox").remove()

        // Берём первые 1-2 параграфа
        val paragraphs = doc.select("p")
            .map { it.text().trim() }
            .filter { it.isNotEmpty() && it.length > 20 }
            .take(2)

        if (paragraphs.isEmpty()) return@withContext null

        val messageHtml = paragraphs.joinToString("") { "<p>$it</p>" }

        // Ищем изображение
        val imageUrl = doc.select("img").firstOrNull()
            ?.attr("src")
            ?.takeIf { it.startsWith("//") }
            ?.let { "https:$it" }

        val wikiLink = "https://ru.wikipedia.org/wiki/$word"

        WordInfo(
            uuid = UUID.randomUUID().toString(),
            ulid = generateULID(),
            request = word,
            message = messageHtml,
            links = mapOf("1" to wikiLink),
            titles = listOf(word),
            images = if (imageUrl != null) listOf(
                Image(imageUrl, blurImage = false, order = 1)
            ) else emptyList(),
            hideRq = false
        )
    }
}