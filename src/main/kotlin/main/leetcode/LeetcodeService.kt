package main.leetcode

import dev.kord.common.entity.Snowflake
import me.jakejmattson.discordkt.annotations.Service


@Service
class LeetcodeService(
    private val storage: LeetcodePersistence,
    private val api: LeetcodeRemoteDatasource
) {

    suspend fun getStatistics(username: String): Result<LeetcodeStatistics> =
        api.getStatistics(username)
            .onFailure {
                println(it)
            }

    suspend fun allStatistics(): List<Pair<String, LeetcodeStatistics>> =
        storage
            .allUsers()
            .toSet()
            .map {
                println(it.second)
                val stats =
                    api
                        .getStatistics(it.second)
                        .getOrNull()
                        ?: return@map null
                return@map Pair(it.first, stats)
            }
            .filterNotNull()


    suspend fun register(id: String, username: String) = storage.register(id, username)
    suspend fun get(id: String): String? = storage.get(id)
    suspend fun remove(id: String) = storage.remove(id)

    suspend fun <T> get(id: T) = get("$id")
    suspend fun get(id: Snowflake) = get("${id.value}")

}
