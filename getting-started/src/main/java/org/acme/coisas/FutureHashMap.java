package org.acme.coisas;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// basicamente a mesma coisa que o outro, mas sem bloquear a thread. Código gerado pelo gemini.
public class FutureHashMap<K, V> {
    private final Map<K, CompletableFuture<V>> futures = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        // se outro lugar já chamou get(x), então já existe um futuro lá dentro
        // e ele é completado com o valor. senão, ele é criado.
        CompletableFuture<V> future = futures.computeIfAbsent(key, k -> new CompletableFuture<>());
        // supposedly, isso retorna false se o future já foi completado anteriormente.
        future.complete(value);

        // se eu descomentar isso, eu não consigo botar um valor e depois tirar, só esperar por um valor        
        // future.whenComplete((result, error) -> futures.remove(key, future));
    }

    public CompletableFuture<V> get(K key) {
        // ou eu pego o valor, ou eu colo um buraquinho onde o put vai colocar o valor
        // e o valor vai cair diretament onde isso for chamado
        CompletableFuture<V> future = futures.computeIfAbsent(key, k -> new CompletableFuture<>());
        future.whenComplete((result, error) -> futures.remove(key, future));
        return future;
    }

    // completa o future com o valor ou null se der timeout
    public CompletableFuture<V> get(K key, long timeoutMs) {
        CompletableFuture<V> future = get(key); 
        return future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                     .exceptionally(e -> {
                         if (e instanceof TimeoutException) {
                             return null; 
                         }
                         throw new RuntimeException(e);
                     });
    }
}