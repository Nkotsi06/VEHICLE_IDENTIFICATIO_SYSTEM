package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic pagination utility for managing large datasets.
 * Provides methods for navigating through paginated data.
 *
 * @param <T> the type of data being paginated
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PaginationUtil<T> {

    private List<T> fullList;
    private List<T> filteredList;
    private int pageSize;
    private int currentPage;
    private int totalPages;
    private Predicate<T> currentFilter;

    /**
     * Constructs a PaginationUtil with the specified page size.
     *
     * @param pageSize number of items per page (must be > 0)
     * @throws IllegalArgumentException if pageSize <= 0
     */
    public PaginationUtil(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.fullList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.totalPages = 0;
    }

    /**
     * Sets the data to be paginated.
     *
     * @param data the list of data
     */
    public void setData(List<T> data) {
        this.fullList = data != null ? new ArrayList<>(data) : new ArrayList<>();
        applyCurrentFilter();
    }

    /**
     * Applies a filter to the data.
     *
     * @param filter the filter predicate (null to clear filter)
     */
    public void setFilter(Predicate<T> filter) {
        this.currentFilter = filter;
        applyCurrentFilter();
    }

    /**
     * Applies the current filter to the full list.
     */
    private void applyCurrentFilter() {
        if (currentFilter != null) {
            filteredList = fullList.stream()
                    .filter(currentFilter)
                    .collect(Collectors.toList());
        } else {
            filteredList = new ArrayList<>(fullList);
        }

        recalculateTotalPages();
        currentPage = 0;
    }

    /**
     * Recalculates total pages based on filtered list size.
     */
    private void recalculateTotalPages() {
        if (filteredList == null || filteredList.isEmpty()) {
            totalPages = 0;
        } else {
            totalPages = (int) Math.ceil((double) filteredList.size() / pageSize);
        }
    }

    /**
     * Gets the data for the current page.
     *
     * @return list of items for the current page
     */
    public List<T> getCurrentPageData() {
        if (filteredList == null || filteredList.isEmpty()) {
            return new ArrayList<>();
        }

        int start = currentPage * pageSize;
        if (start >= filteredList.size()) {
            return new ArrayList<>();
        }

        int end = Math.min(start + pageSize, filteredList.size());
        return filteredList.subList(start, end);
    }

    /**
     * Moves to the next page.
     */
    public void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
        }
    }

    /**
     * Moves to the previous page.
     */
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    /**
     * Goes to a specific page number.
     *
     * @param page the page number (0-indexed)
     */
    public void goToPage(int page) {
        if (page >= 0 && page < totalPages) {
            currentPage = page;
        }
    }

    /**
     * Goes to the first page.
     */
    public void firstPage() {
        currentPage = 0;
    }

    /**
     * Goes to the last page.
     */
    public void lastPage() {
        if (totalPages > 0) {
            currentPage = totalPages - 1;
        }
    }

    /**
     * Gets the current page number (0-indexed).
     *
     * @return current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Gets the current display page number (1-indexed for UI).
     *
     * @return current page number starting from 1
     */
    public int getCurrentPageDisplay() {
        return currentPage + 1;
    }

    /**
     * Gets the total number of pages.
     *
     * @return total pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Gets the page size.
     *
     * @return page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Changes the page size and recalculates pagination.
     *
     * @param newPageSize the new page size
     */
    public void setPageSize(int newPageSize) {
        if (newPageSize > 0 && newPageSize != pageSize) {
            this.pageSize = newPageSize;
            recalculateTotalPages();
            // Ensure current page is valid with new page size
            if (currentPage >= totalPages && totalPages > 0) {
                currentPage = totalPages - 1;
            }
        }
    }

    /**
     * Gets the starting index (1-indexed) of the current page.
     *
     * @return start index (1-indexed)
     */
    public int getStartIndex() {
        if (filteredList.isEmpty()) return 0;
        return currentPage * pageSize + 1;
    }

    /**
     * Gets the ending index (1-indexed) of the current page.
     *
     * @return end index (1-indexed)
     */
    public int getEndIndex() {
        if (filteredList.isEmpty()) return 0;
        return Math.min((currentPage + 1) * pageSize, filteredList.size());
    }

    /**
     * Gets the total number of items in the filtered list.
     *
     * @return total items count
     */
    public int getTotalItems() {
        return filteredList != null ? filteredList.size() : 0;
    }

    /**
     * Gets the total number of items in the original full list.
     *
     * @return total original items count
     */
    public int getTotalOriginalItems() {
        return fullList != null ? fullList.size() : 0;
    }

    /**
     * Checks if there is a next page.
     *
     * @return true if next page exists
     */
    public boolean hasNext() {
        return currentPage < totalPages - 1;
    }

    /**
     * Checks if there is a previous page.
     *
     * @return true if previous page exists
     */
    public boolean hasPrevious() {
        return currentPage > 0;
    }

    /**
     * Gets the list of page numbers to display in pagination controls.
     * Returns up to 5 pages around the current page.
     *
     * @return list of page numbers (0-indexed)
     */
    public List<Integer> getPageNumbers() {
        List<Integer> pages = new ArrayList<>();
        if (totalPages == 0) return pages;

        int start = Math.max(0, currentPage - 2);
        int end = Math.min(totalPages - 1, currentPage + 2);

        // Ensure we show up to 5 pages
        if (end - start < 4) {
            if (start > 0) {
                start = Math.max(0, end - 4);
            }
            if (end < totalPages - 1) {
                end = Math.min(totalPages - 1, start + 4);
            }
        }

        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }

    /**
     * Gets a summary string for display (e.g., "Showing 1-10 of 50").
     *
     * @return formatted summary string
     */
    public String getSummary() {
        if (filteredList.isEmpty()) {
            return "No items to display";
        }
        return String.format("Showing %d-%d of %d items",
                getStartIndex(), getEndIndex(), getTotalItems());
    }

    /**
     * Clears all data and resets pagination.
     */
    public void clear() {
        fullList.clear();
        filteredList.clear();
        currentFilter = null;
        currentPage = 0;
        totalPages = 0;
    }

    /**
     * Checks if the paginator has any data.
     *
     * @return true if there is data, false otherwise
     */
    public boolean hasData() {
        return filteredList != null && !filteredList.isEmpty();
    }

    /**
     * Gets an unmodifiable view of the full list.
     *
     * @return unmodifiable list of all data
     */
    public List<T> getUnmodifiableFullList() {
        return Collections.unmodifiableList(fullList);
    }

    /**
     * Gets an unmodifiable view of the filtered list.
     *
     * @return unmodifiable list of filtered data
     */
    public List<T> getUnmodifiableFilteredList() {
        return Collections.unmodifiableList(filteredList);
    }
}