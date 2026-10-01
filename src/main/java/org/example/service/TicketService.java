package org.example.service;

import jakarta.ejb.Local;
import org.example.dto.PageResult;
import org.example.dto.TicketBriefDTO;
import org.example.dto.TicketDetailDTO;
import org.example.dto.TicketFilterDTO;
import org.example.dto.TicketFormDTO;
import org.example.exception.TicketOperationException;

/**
 * Локальный EJB-контракт для чтения и изменения обращений.
 */
@Local
public interface TicketService {
    /**
     * Возвращает страницу кратких сведений об обращениях с учётом фильтров.
     *
     * @param page номер страницы, начиная с нуля
     * @param size количество обращений на странице
     * @param filter фильтры по части email с учётом регистра, статусу и приоритету;
     *               {@code null} означает отсутствие фильтров
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

    /**
     * Создаёт обращение и связывает его с найденным по email клиентом.
     *
     * @param form данные обращения и email существующего клиента
     * @return идентификатор созданного обращения
     * @throws TicketOperationException если данные неверны или клиент не найден
     */
    Long createTicket(TicketFormDTO form);

    /**
     * Обновляет поля обращения, сохраняя его идентификатор и связанного клиента.
     *
     * @param id идентификатор обращения
     * @param form новые значения полей
     * @return актуальные данные обращения
     * @throws TicketOperationException если обращение не найдено или данные неверны
     */
    TicketDetailDTO updateTicket(Long id, TicketFormDTO form);

    /**
     * Удаляет обращение. Связанный клиент при этом не удаляется.
     *
     * @param id идентификатор обращения
     * @throws TicketOperationException если обращение не найдено
     */
    void deleteTicket(Long id);
}
