package utils;

import java.util.ArrayList;
import java.util.List;

public class PaginationUtil<T> {

    private List<T> fullList;
    private int pageSize;
    private int currentPage;
    private int totalPages;

    public PaginationUtil(int pageSize) {
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.fullList = new ArrayList<>();
        this.totalPages = 0;
    }

    public void setData(List<T> data) {
        this.fullList = data;
        this.totalPages = (int) Math.ceil((double) data.size() / pageSize);
        this.currentPage = 0;
    }

    public List<T> getCurrentPageData() {
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullList.size());
        if (start >= fullList.size()) {
            return new ArrayList<>();
        }
        return fullList.subList(start, end);
    }

    public void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    public void goToPage(int page) {
        if (page >= 0 && page < totalPages) {
            currentPage = page;
        }
    }

    public void firstPage() {
        currentPage = 0;
    }

    public void lastPage() {
        currentPage = totalPages - 1;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getStartIndex() {
        return currentPage * pageSize + 1;
    }

    public int getEndIndex() {
        return Math.min((currentPage + 1) * pageSize, fullList.size());
    }

    public int getTotalItems() {
        return fullList.size();
    }

    public boolean hasNext() {
        return currentPage < totalPages - 1;
    }

    public boolean hasPrevious() {
        return currentPage > 0;
    }

    public List<Integer> getPageNumbers() {
        List<Integer> pages = new ArrayList<>();
        int start = Math.max(0, currentPage - 2);
        int end = Math.min(totalPages - 1, currentPage + 2);

        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}