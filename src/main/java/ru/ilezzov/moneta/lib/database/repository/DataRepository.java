package ru.ilezzov.moneta.lib.database.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DataRepository<K, V> {
    CompletableFuture<V> get(K key);

    CompletableFuture<List<V>> getAll(boolean addToCache);

    CompletableFuture<List<V>> getAll(boolean addToCache, LocalDateTime date);

    CompletableFuture<Void> insert(V value);

    CompletableFuture<Void> save(V value);

    CompletableFuture<Void> saveCache();
}
