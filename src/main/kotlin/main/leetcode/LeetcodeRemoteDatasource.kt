package main.leetcode

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import main.Environment
import me.jakejmattson.discordkt.annotations.Service


@Service
class LeetcodeRemoteDatasource {
    suspend fun getStatistics(username: String): Result<LeetcodeStatistics> =
        Environment
            .client
            .fetchLeetcodeStatistics(username)
            .map { it.toDomain(username) }

}

private suspend fun HttpClient.fetchLeetcodeStatistics(username: String, year: Int = 2023): Result<LeetcodeQueryResponse> {
    val query =
        """
            {
            "query":"query getUserProfile(${"$"}username: String!, ${"$"}year: Int!) { allQuestionsCount { difficulty count } matchedUser(username: ${"$"}username) { userCalendar(year: ${"$"}year) { activeYears streak totalActiveDays submissionCalendar } problemsSolvedBeatsStats { difficulty percentage } submitStatsGlobal { acSubmissionNum { difficulty count } } contributions { points } profile { userAvatar realName aboutMe postViewCount postViewCountDiff reputation reputationDiff solutionCount solutionCountDiff categoryDiscussCount categoryDiscussCountDiff reputation ranking } submissionCalendar submitStats { acSubmissionNum { difficulty count submissions } totalSubmissionNum { difficulty count submissions } } } }",
            "variables":{"username":"$username", "year": $year}
            }
        """.trimIndent()

    return runCatching {
        get {
            url {
                protocol = URLProtocol.HTTPS
                host = "leetcode.com"
                method = HttpMethod.Post
                header("referer", "https://leetcode.com/$username/")
                header("Content-Type", "application/json")
                appendPathSegments("graphql")
                setBody(query)
            }
        }.body()
    }

}

private fun LeetcodeQueryResponse.toDomain(username: String): LeetcodeStatistics {
    fun findOrDefaultSolved(difficulty: String): Long =
        data.matchedUser.submitStatsGlobal.acSubmissionNum.find { it.difficulty == difficulty }?.count ?: 0

    fun calculateAcceptanceRate(): Double {
        val actual = data.matchedUser.submitStats.acSubmissionNum.find { it.difficulty == "All" }?.count ?: return 0.0
        val total = data.matchedUser.submitStats.totalSubmissionNum.find { it.difficulty == "All" }?.count ?: return 0.0
        return (actual / total).toDouble()
    }

    return LeetcodeStatistics(
        username = username,
        totalSolved = findOrDefaultSolved("All"),
        easySolved = findOrDefaultSolved("Easy"),
        mediumSolved = findOrDefaultSolved("Medium"),
        hardSolved = findOrDefaultSolved("Hard"),
        acceptanceRate = calculateAcceptanceRate(),
        ranking = data.matchedUser.profile.ranking,
        contributionPoints = data.matchedUser.contributions.points
    )
}

suspend fun main() {
    val res = LeetcodeRemoteDatasource().getStatistics("baksha97")
    println(res)
}

@Serializable
data class LeetcodeQueryResponse (
    val data: Data
)

@Serializable
data class Data (
    val allQuestionsCount: List<AllQuestionsCount>,
    val matchedUser: MatchedUser
)

@Serializable
data class AllQuestionsCount (
    val difficulty: String,
    val count: Long
)

@Serializable
data class MatchedUser (
    val userCalendar: UserCalendar,
    val problemsSolvedBeatsStats: List<ProblemsSolvedBeatsStat>,
    val submitStatsGlobal: SubmitStatsGlobal,
    val contributions: Contributions,
    val profile: Profile,
    val submissionCalendar: String,
    val submitStats: SubmitStats
)

@Serializable
data class Contributions (
    val points: Long
)

@Serializable
data class ProblemsSolvedBeatsStat (
    val difficulty: String,
    val percentage: Double
)

@Serializable
data class Profile (
    val userAvatar: String,
    val realName: String,
    val aboutMe: String,
    val postViewCount: Long,
    val postViewCountDiff: Long,
    val reputation: Long,
    val reputationDiff: Long,
    val solutionCount: Long,
    val solutionCountDiff: Long,
    val categoryDiscussCount: Long,
    val categoryDiscussCountDiff: Long,
    val ranking: Long
)

@Serializable
data class SubmitStats (
    val acSubmissionNum: List<SubmissionNum>,
    val totalSubmissionNum: List<SubmissionNum>
)

@Serializable
data class SubmissionNum (
    val difficulty: String,
    val count: Long,
    val submissions: Long
)

@Serializable
data class SubmitStatsGlobal (
    val acSubmissionNum: List<AllQuestionsCount>
)

@Serializable
data class UserCalendar (
    val activeYears: List<Long>,
    val streak: Long,
    val totalActiveDays: Long,
    val submissionCalendar: String
)
