package main.leetcode

import kotlinx.serialization.Serializable

@Serializable
data class LeetcodeStatistics(
    val username: String,
    val totalSolved: Long,
    val easySolved: Long,
    val mediumSolved: Long,
    val hardSolved: Long,
    val acceptanceRate: Double,
    val ranking: Long,
    val contributionPoints: Long,
)