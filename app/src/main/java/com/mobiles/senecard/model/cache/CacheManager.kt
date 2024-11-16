package com.mobiles.senecard.model.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

class CacheManager<K, V>(
    expiryDuration: Long,
    maxSize: Long
) {
    private val cache: Cache<K, V> = CacheBuilder.newBuilder()
        .expireAfterWrite(expiryDuration, TimeUnit.MINUTES)
        .maximumSize(maxSize)
        .build()

    fun put(key: K, value: V) {
        cache.put(key, value)
    }

    fun get(key: K): V? {
        return cache.getIfPresent(key)
    }

    fun getAll(): List<V> {
        return cache.asMap().values.toList()
    }

    fun invalidate(key: K) {
        cache.invalidate(key)
    }

    fun invalidateAll() {
        cache.invalidateAll()
    }
}
