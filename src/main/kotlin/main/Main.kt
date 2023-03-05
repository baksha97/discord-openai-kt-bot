package main

import dev.kord.common.annotation.KordPreview
import io.github.cdimascio.dotenv.dotenv
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import main.leetcode.LeetcodeService
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.locale.Language

object Environment {
    private val dotenv = dotenv()

    val discordToken: String get() = dotenv.get("DISCORD_BOT_TOKEN")
    val redisUri: String get() = dotenv.get("REDIS")

    val client = HttpClient(CIO) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
    }
}

@OptIn(KordPreview::class)
suspend fun main(args: Array<String>) {
    bot(Environment.discordToken) {
        onStart {
            val guilds = kord.guilds.toList()
            println("Guilds: ${guilds.joinToString { it.name }}")
        }

        onException {
            println("Exception $this")
        }

        localeOf(Language.EN) {
            helpName = "Help"
            helpCategory = "Utility"
            commandRecommendation = "Recommendation: {0}"
        }
    }
}

