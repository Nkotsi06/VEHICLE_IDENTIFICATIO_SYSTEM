package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SearchUtil {

    public static <T> List<T> search(List<T> items, String keyword, SearchPredicate<T> predicate) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(items);
        }

        String searchTerm = keyword.toLowerCase().trim();
        return items.stream()
                .filter(item -> predicate.test(item, searchTerm))
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    public interface SearchPredicate<T> {
        boolean test(T item, String searchTerm);
    }

    public static boolean containsIgnoreCase(String value, String searchTerm) {
        if (value == null) return false;
        return value.toLowerCase().contains(searchTerm);
    }

    public static boolean startsWithIgnoreCase(String value, String searchTerm) {
        if (value == null) return false;
        return value.toLowerCase().startsWith(searchTerm);
    }

    public static boolean endsWithIgnoreCase(String value, String searchTerm) {
        if (value == null) return false;
        return value.toLowerCase().endsWith(searchTerm);
    }

    public static boolean equalsIgnoreCase(String value, String searchTerm) {
        if (value == null) return false;
        return value.equalsIgnoreCase(searchTerm);
    }

    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        return items.stream().filter(predicate).collect(Collectors.toList());
    }
}