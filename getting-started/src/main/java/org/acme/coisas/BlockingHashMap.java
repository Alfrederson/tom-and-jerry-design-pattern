package org.acme.coisas;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BlockingHashMap<K, V> {

    private final Map<K, ArrayBlockingQueue<V>> queues = new ConcurrentHashMap<>();

    private ArrayBlockingQueue<V> queueForKey(K key) {
        return queues.computeIfAbsent(key, k -> new ArrayBlockingQueue<>(1));
    }

    public void put(K key, V value) {
        ArrayBlockingQueue<V> q = queueForKey(key);
        q.clear();
        q.offer(value);
    }

    public V get(K key, long timeoutMs) throws InterruptedException {
        ArrayBlockingQueue<V> q = queueForKey(key);
        V val = q.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (val != null) {
            queues.remove(key, q);
        }
        return val;
    }

    public V get(K key) throws InterruptedException {
        ArrayBlockingQueue<V> q = queueForKey(key);
        V val = q.take();

        queues.remove(key, q);

        return val;
    }
}
