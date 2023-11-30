package ru.bukhtaev.util;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Реализация LRU-кэша.
 *
 * @param <K> тип ключа
 * @param <V> тип значения
 * @see <a href="https://www.interviewcake.com/concept/java/lru-cache">LRU-кэш</a>
 */
public class LruCache<K, V> {

    /**
     * Модифицированная {@link LinkedHashMap}.
     */
    private final LinkedHashMap<K, WeakReference<V>> map;

    /**
     * Конструктор.
     * <p>
     * Создает {@link LinkedHashMap}, которая поддерживает порядок доступа к элементам
     * и удаляет последний элемент при превышении начальной вместимости.
     *
     * @param capacity размер кэша
     */
    public LruCache(final int capacity) {
        this.map = new LinkedHashMap<>(
                capacity,
                0.75f,
                true
        ) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<K, WeakReference<V>> eldest) {
                return size() > capacity;
            }
        };
    }

    /**
     * Возвращает значение из кэша по ключу, если это значение содержится в кэше.
     * В противном случае возвращает {@code null}.
     *
     * @param key ключ
     * @return значение из кэша по ключу, если это значение содержится в кэше
     */
    public V get(final K key) {
        WeakReference<V> valueReference = map.get(key);
        return valueReference != null
                ? valueReference.get()
                : null;
    }

    /**
     * Добавляет значение в кэш по указанному ключу.
     *
     * @param key   ключ
     * @param value значение
     */
    public void put(final K key, final V value) {
        map.put(key, new WeakReference<>(value));
    }

    /**
     * Удаляет значение из кэша по указанному ключу.
     *
     * @param key ключ
     */
    public void delete(final K key) {
        map.remove(key);
    }
}
