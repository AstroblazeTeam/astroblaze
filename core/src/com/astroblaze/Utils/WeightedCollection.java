package com.astroblaze.Utils;

import java.util.*;

/**
 * Class provides a weighted collection and a method
 * to select random element according to their weight
 * @param <T> Weighted result
 */
public class WeightedCollection<T> {
    private final NavigableMap<Integer, T> map = new TreeMap<>();
    private final Random random;
    private int total = 0;

    public WeightedCollection() {
        this(new Random());
    }

    public WeightedCollection(Random random) {
        this.random = random;
    }

    public void add(int weight, T object) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, object);
    }

    public T getRandom() {
        int value = random.nextInt(total) + 1; // Can also use floating-point weights
        return map.ceilingEntry(value).getValue();
    }
}