package org.example.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Результат постраничной выборки вместе с данными для навигации.
 *
 * @param <T> тип записи на странице
 */
@Data
public class PageResult<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> content;
    private long totalElements;
    private int totalPages;
    /** Номер текущей страницы, начиная с нуля. */
    private int currentPage;

    /**
     * Формирует результат и вычисляет число страниц по общему количеству записей.
     * Для пустой выборки число страниц равно нулю.
     *
     * @param content записи текущей страницы
     * @param totalElements общее количество записей после фильтрации
     * @param currentPage номер текущей страницы с нуля
     * @param pageSize количество записей на странице
     */
    public PageResult(List<T> content, long totalElements, int currentPage, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }

}
