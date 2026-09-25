package org.example.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.example.dto.PageResult;
import org.example.dto.TicketBriefDTO;
import org.example.dto.TicketFilterDTO;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;
import org.example.service.TicketService;

import java.io.Serializable;
import java.util.List;

/**
 * Модель JSF-страницы со списком обращений, фильтрами и постраничной навигацией.
 * Номер текущей страницы хранится с нуля.
 */
@Named
@ViewScoped
public class TicketListBean implements Serializable {

    @EJB
    private TicketService ticketService;

    @Getter @Setter private String email;
    @Getter @Setter private TicketStatus status;
    @Getter @Setter private TicketPriority priority;

    /** Номер страницы с нуля; в интерфейсе пользователю показывается номер с единицы. */
    @Getter private int currentPage = 0;
    private int pageSize = 10;

    @Getter private PageResult<TicketBriefDTO> ticketPage;

    /** Загружает первую страницу при открытии списка. */
    @PostConstruct
    public void init() {
        loadTickets();
    }

    /** Применяет выбранные фильтры, начиная с первой страницы результатов. */
    public void search() {
        currentPage = 0;
        loadTickets();
    }

    /** Переходит на следующую страницу, если она существует. */
    public void nextPage() {
        if (ticketPage != null && currentPage < ticketPage.getTotalPages() - 1) {
            currentPage++;
            loadTickets();
        }
    }

    /** Переходит на предыдущую страницу, если она существует. */
    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadTickets();
        }
    }

    /** Сбрасывает фильтры и возвращается к первой странице. */
    public void resetFilters() {
        email = null;
        status = null;
        priority = null;
        currentPage = 0;
        loadTickets();
    }

    /** Получает страницу обращений из бизнес-слоя по текущим фильтрам. */
    private void loadTickets() {
        ticketPage = ticketService.getFilteredTickets(
                currentPage,
                pageSize,
                new TicketFilterDTO(email, status, priority)
        );
    }

    /**
     * Возвращает записи текущей страницы для таблицы JSF.
     *
     * @return обращения или пустой список до загрузки страницы
     */
    public List<TicketBriefDTO> getTickets() {
        return ticketPage != null ? ticketPage.getContent() : List.of();
    }

    /**
     * Проверяет доступность перехода вперёд.
     *
     * @return {@code true}, если есть следующая страница
     */
    public boolean getHasNext() {
        return ticketPage != null && currentPage < ticketPage.getTotalPages() - 1;
    }

    /**
     * Проверяет доступность перехода назад.
     *
     * @return {@code true}, если есть предыдущая страница
     */
    public boolean getHasPrev() {
        return currentPage > 0;
    }

    /**
     * Возвращает значения для фильтра по статусу.
     *
     * @return все статусы обращений
     */
    public TicketStatus[] getAvailableStatuses() {
        return TicketStatus.values();
    }

    /**
     * Возвращает значения для фильтра по приоритету.
     *
     * @return все приоритеты обращений
     */
    public TicketPriority[] getAvailablePriorities() {
        return TicketPriority.values();
    }
}
