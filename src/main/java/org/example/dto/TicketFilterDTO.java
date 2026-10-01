package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;

/**
 * Условия поиска обращений, передаваемые из страницы списка в EJB-сервис.
 * Пустые значения не ограничивают выборку; email ищется по фрагменту с учётом регистра.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketFilterDTO {
    private String email;
    private TicketStatus status;
    private TicketPriority priority;
}
