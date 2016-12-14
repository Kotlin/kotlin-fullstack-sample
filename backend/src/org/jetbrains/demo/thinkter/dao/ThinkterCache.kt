package org.jetbrains.demo.thinkter.dao

import org.ehcache.*
import org.ehcache.config.*
import org.ehcache.config.persistence.*
import org.ehcache.config.units.*
import org.jetbrains.demo.thinkter.model.*
import java.io.*
import java.time.*

class ThinkterCache(val delegate: ThinkterStorage, storagePath: File) : ThinkterStorage {
    val cacheManagerConfiguration = CacheManagerPersistenceConfiguration(storagePath)
    val cacheManager: PersistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(cacheManagerConfiguration)
            .withCache("thoughtsCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder<Int, Thought>()
                            .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(1000, EntryUnit.ENTRIES)
                                    .offheap(10, MemoryUnit.MB)
                                    .disk(100, MemoryUnit.MB, true)
                            )
                            .buildConfig(Int::class.javaObjectType, Thought::class.java))
            .withCache("usersCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder<String, User>()
                            .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(1000, EntryUnit.ENTRIES)
                                    .offheap(10, MemoryUnit.MB)
                                    .disk(100, MemoryUnit.MB, true)
                            )
                            .buildConfig(String::class.java, User::class.java))
            .build(true)

    val thoughtsCache: Cache<Int, Thought> = cacheManager.getCache("thoughtsCache", Int::class.javaObjectType, Thought::class.java)
    val usersCache: Cache<String, User> = cacheManager.getCache("usersCache", String::class.java, User::class.java)

    override fun countReplies(id: Int): Int {
        return delegate.countReplies(id)
    }

    override fun createThought(user: String, text: String, replyTo: Int?, date: LocalDateTime): Int {
        val id = delegate.createThought(user, text, replyTo)
        val thought = Thought(id, user, text, date, replyTo)
        thoughtsCache.put(id, thought)
        return id
    }

    override fun deleteThought(id: Int) {
        delegate.deleteThought(id)
        thoughtsCache.remove(id)
    }

    override fun getThought(id: Int): Thought {
        val cached = thoughtsCache.get(id)
        if (cached != null) {
            return cached
        }

        val thought = delegate.getThought(id)
        thoughtsCache.put(id, thought)

        return thought
    }

    override fun userThoughts(userId: String): List<Int> {
        return delegate.userThoughts(userId)
    }

    override fun user(userId: String, hash: String?): User? {
        val cached = usersCache.get(userId)
        val user = if (cached == null) {
            val dbUser = delegate.user(userId)
            if (dbUser != null) {
                usersCache.put(userId, dbUser)
            }
            dbUser
        } else {
            cached
        }

        return when {
            user == null -> null
            hash == null -> user
            user.passwordHash == hash -> user
            else -> null
        }
    }

    override fun userByEmail(email: String): User? {
        return delegate.userByEmail(email)
    }

    override fun createUser(user: User) {
        if (usersCache.get(user.userId) != null) {
            throw IllegalStateException("User already exist")
        }

        delegate.createUser(user)
        usersCache.put(user.userId, user)
    }

    override fun top(count: Long): List<Int> {
        return delegate.top(count)
    }

    override fun latest(count: Long): List<Int> {
        return delegate.latest(count)
    }

    override fun close() {
        try {
            delegate.close()
        } finally {
            cacheManager.close()
        }
    }
}
