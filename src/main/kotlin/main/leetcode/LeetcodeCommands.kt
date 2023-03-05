package main

import dev.kord.common.Color
import main.leetcode.LeetcodeService
import main.leetcode.LeetcodeStatistics
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.CommandSet
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.dsl.SlashResponder

fun buildLeetcodeCommands(
    service: LeetcodeService
): CommandSet = commands("Leetcode") {

    slash("Register", "Register your Leetcode") {
        execute(AnyArg("username")) {
            runCatching {
                val id = "${author.id.value}"
                service.register(id = id, args.first.trim())
                respond("Successfully registered to your discord account.")
            }.onFailure {
                respond("Something went wrong: $it")
            }
        }
    }

    slash("Stats", "Get statistics for your leetcode account") {
        execute {
            runCatching {
                val username = service.get(author.id)!!
                val statistic = service.getStatistics(username).getOrThrow()
                sendStatistic(username = username, statistic = statistic)
            }.onFailure {
                when(it) {
                    is NullPointerException ->
                        respond("Register your leetcode account using `/register` to proceed.")
                    else ->
                        respond("Something went wrong: $it")
                }
            }
        }
    }

    slash("Leaderboard", "Rankings based on Leetcode Rank") {
        execute {
            runCatching {
                val usernamesAndStatistics = service.allStatistics()
                sendLeaderboard(usernamesAndStatistics)
            }.onFailure {
                respond("Something went wrong: $it")
            }
        }
    }
}

suspend fun SlashResponder.sendStatistic(username: String, statistic: LeetcodeStatistics) = respondPublic {
    title = "$username statistics dev"
    color = Color(0x00bfff)
    url = "https://leetcode.com/$username/"

    field("🎖 Ranking", false) {
        "${statistic.ranking}"
    }

    field("Easy Solved", false) {
        "\uD83D\uDFE2 ${statistic.easySolved}"
    }
    field("Medium Solved", false) {
        "\uD83D\uDFE1 ${statistic.mediumSolved}"
    }
    field("Hard Solved", false) {
        "\uD83D\uDD34 ${statistic.hardSolved}"
    }

    field("Total Solved", false) {
        "\uD83D\uDFF0 ${statistic.totalSolved}"
    }

    field("Contribution Points", false) {
        "\uD83E\uDDE9 ${statistic.contributionPoints}"
    }

    field("Acceptance Rate", false) {
        "${statistic.acceptanceRate}%"
    }
}

suspend fun SlashResponder.sendLeaderboard(data: List<Pair<String, LeetcodeStatistics>>) = respondPublic {
    title = "Leaderboard"
    color = Color(0x00bfff)

    val leaderboard = data.sortedBy { pair -> pair.second.ranking }

    leaderboard.forEachIndexed { index, (username, statistic) ->
        field("{$index+1})", false) {
            "$username | ${statistic.ranking}"
        }
    }
}