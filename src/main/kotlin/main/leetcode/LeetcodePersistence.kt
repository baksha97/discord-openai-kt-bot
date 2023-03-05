package main.leetcode

import dev.kord.common.entity.Snowflake
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import main.Environment
import me.jakejmattson.discordkt.annotations.Service

interface LeetcodePersistence {
    suspend fun register(id: String, username: String)
    suspend fun contains(id: String): Boolean
    suspend fun get(id: String): String?
    suspend fun remove(id: String)
    suspend fun allUsers(): List<Pair<String, String>>
    suspend fun <T> get(id: T) = get("$id")
    suspend fun get(id: Snowflake) = get("${id.value}")
}

@Service
class RedisLeetcodePersistence : LeetcodePersistence {
    private val redis = newClient(Endpoint.from(Environment.redisUri))

    override suspend fun register(id: String, username: String): Unit =
        redis.use { client ->
            client.set(id, username)
        }


    override suspend fun contains(id: String): Boolean =
        redis.use { it.get(id) != null }

    override suspend fun get(id: String): String? =
        redis.use { it.get(id) }

    override suspend fun remove(id: String): Unit =
        redis.use { it.expire(id, 0u) }

    override suspend fun allUsers(): List<Pair<String, String>> =
        redis.use { client ->
            client
                .keys("*")
                .map {
                    Pair(it, client.get(it)!!)
                }
        }
}