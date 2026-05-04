package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Utility class for searching and filtering collections.
 * Provides functional programming style search operations.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class SearchUtil {

    private static final Logger LOGGER = Logger.getLogger(SearchUtil.class.getName());

    private SearchUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Searches a list using a custom predicate.
     *
     * @param <T>       the type of items in the list
     * @param items     the list to search
     * @param keyword   the search keyword
     * @param predicate the search predicate that tests each item
     * @return filtered list of items matching the search criteria
     */
    public static <T> List<T> search(List<T> items, String keyword, SearchPredicate<T> predicate) {
        if (items == null) {
            return new ArrayList<>();
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(items);
        }

        String searchTerm = keyword.toLowerCase().trim();

        try {
            return items.stream()
                    .filter(item -> item != null && predicate.test(item, searchTerm))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warning("Error during search: " + e.getMessage());
            return new ArrayList<>(items);
        }
    }

    /**
     * Filters a list using a predicate.
     *
     * @param <T>       the type of items in the list
     * @param items     the list to filter
     * @param predicate the filter predicate
     * @return filtered list
     */
    public static <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        if (items == null) {
            return new ArrayList<>();
        }

        if (predicate == null) {
            return new ArrayList<>(items);
        }

        return items.stream()
                .filter(item -> item != null && predicate.test(item))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a string contains the search term (case-insensitive).
     *
     * @param value      the string to check
     * @param searchTerm the search term
     * @return true if contains, false otherwise
     */
    public static boolean containsIgnoreCase(String value, String searchTerm) {
        if (value == null || searchTerm == null) return false;
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    /**
     * Checks if a string starts with the search term (case-insensitive).
     *
     * @param value      the string to check
     * @param searchTerm the search term
     * @return true if starts with, false otherwise
     */
    public static boolean startsWithIgnoreCase(String value, String searchTerm) {
        if (value == null || searchTerm == null) return false;
        return value.toLowerCase().startsWith(searchTerm.toLowerCase());
    }

    /**
     * Checks if a string ends with the search term (case-insensitive).
     *
     * @param value      the string to check
     * @param searchTerm the search term
     * @return true if ends with, false otherwise
     */
    public static boolean endsWithIgnoreCase(String value, String searchTerm) {
        if (value == null || searchTerm == null) return false;
        return value.toLowerCase().endsWith(searchTerm.toLowerCase());
    }

    /**
     * Checks if a string equals the search term (case-insensitive).
     *
     * @param value      the string to check
     * @param searchTerm the search term
     * @return true if equals, false otherwise
     */
    public static boolean equalsIgnoreCase(String value, String searchTerm) {
        if (value == null || searchTerm == null) return false;
        return value.equalsIgnoreCase(searchTerm);
    }

    /**
     * Functional interface for custom search logic.
     *
     * @param <T> the type of item being searched
     */
    @FunctionalInterface
    public interface SearchPredicate<T> {
        /**
         * Tests whether an item matches the search criteria.
         *
         * @param item       the item to test
         * @param searchTerm the normalized search term (lowercase, trimmed)
         * @return true if the item matches, false otherwise
         */
        boolean test(T item, String searchTerm);
    }

    /**
     * Creates a case-insensitive contains predicate for a specific field getter.
     *
     * @param <T>      the item type
     * @param fieldGet function to extract the string field from the item
     * @return a SearchPredicate that checks if the field contains the search term
     */
    public static <T> SearchPredicate<T> containsPredicate(java.util.function.Function<T, String> fieldGet) {
        return (item, searchTerm) -> {
            String fieldValue = fieldGet.apply(item);
            return fieldValue != null && fieldValue.toLowerCase().contains(searchTerm);
        };
    }

    /**
     * Creates a case-insensitive starts-with predicate for a specific field getter.
     *
     * @param <T>      the item type
     * @param fieldGet function to extract the string field from the item
     * @return a SearchPredicate that checks if the field starts with the search term
     */
    public static <T> SearchPredicate<T> startsWithPredicate(java.util.function.Function<T, String> fieldGet) {
        return (item, searchTerm) -> {
            String fieldValue = fieldGet.apply(item);
            return fieldValue != null && fieldValue.toLowerCase().startsWith(searchTerm);
        };
    }

    /**
     * Creates a case-insensitive equals predicate for a specific field getter.
     *
     * @param <T>      the item type
     * @param fieldGet function to extract the string field from the item
     * @return a SearchPredicate that checks if the field equals the search term
     */
    public static <T> SearchPredicate<T> equalsPredicate(java.util.function.Function<T, String> fieldGet) {
        return (item, searchTerm) -> {
            String fieldValue = fieldGet.apply(item);
            return fieldValue != null && fieldValue.equalsIgnoreCase(searchTerm);
        };
    }

    /**
     * Creates a multi-field search predicate that checks multiple fields.
     *
     * @param <T>       the item type
     * @param fieldGets array of functions to extract string fields
     * @return a SearchPredicate that checks if any field contains the search term
     */
    @SafeVarargs
    public static <T> SearchPredicate<T> multiFieldPredicate(java.util.function.Function<T, String>... fieldGets) {
        return (item, searchTerm) -> {
            for (java.util.function.Function<T, String> fieldGet : fieldGets) {
                String fieldValue = fieldGet.apply(item);
                if (fieldValue != null && fieldValue.toLowerCase().contains(searchTerm)) {
                    return true;
                }
            }
            return false;
        };
    }
}