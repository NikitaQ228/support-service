package org.example.service;

import jakarta.ejb.Local;
import org.example.dto.PageResult;
import org.example.dto.TicketBriefDTO;
import org.example.dto.TicketDetailDTO;
import org.example.dto.TicketFilterDTO;

/**
 * Локальный EJB-контракт для чтения обращений из базы данных.
 */
@Local
public interface TicketService {
    /**
     * Возвращает страницу кратких сведений об обращениях с учётом фильтров.
     *
     * @param page номер страницы, начиная с нуля
     * @param size количество обращений на странице
     * @param filter фильтры по email клиента, статусу и приоритету; {@code null} означает отсутствие фильтров
     * @return обращения и общее число найденных записей
     */
    PageResult<TicketBriefDTO> getFilteredTickets(int page, int size, TicketFilterDTO filter);

    /**
     * Находит обращение вместе с данными клиента.
     *
     * @param id идентификатор обращения
     * @return подробные сведения или {@code null}, если обращение не найдено
     */
    TicketDetailDTO getTicketById(Long id);
}
