package arvem.aspectral.util;


import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A weighted list with filtering capabilities.
 * Part of AspectData library (inspired by Calio from Origins).
 */
public class FilterableWeightedList<T> {

    private final List<Entry<T>> entries = new ArrayList<>();
    private int totalWeight = 0;

    public static class Entry<T> {
        private final T element;
        private final int weight;

        public Entry(T element, int weight) {
            this.element = element;
            this.weight = weight;
        }

        public T getElement() {
            return element;
        }

        public int getWeight() {
            return weight;
        }
    }

    public void add(T element, int weight) {
        entries.add(new Entry<>(element, weight));
        totalWeight += weight;
    }

    public int size() {
        return entries.size();
    }

    public Stream<Entry<T>> entryStream() {
        return entries.stream();
    }

    public List<Entry<T>> getEntries() {
        return new ArrayList<>(entries);
    }

    public T pickRandom(Random random) {
        if (entries.isEmpty()) {
            return null;
        }

        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Entry<T> entry : entries) {
            currentWeight += entry.weight;
            if (randomWeight < currentWeight) {
                return entry.element;
            }
        }

        return entries.get(entries.size() - 1).element;
    }

    public FilterableWeightedList<T> filter(Predicate<T> predicate) {
        FilterableWeightedList<T> filtered = new FilterableWeightedList<>();
        for (Entry<T> entry : entries) {
            if (predicate.test(entry.element)) {
                filtered.add(entry.element, entry.weight);
            }
        }
        return filtered;
    }
}
