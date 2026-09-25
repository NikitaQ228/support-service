package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Краткое представление обращения для строки таблицы на главной странице.
 * Содержит только поля, выбранные запросом списка.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketBriefDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private String customerEmail;
    private LocalDate dateOfPurchase;
    private String subject;
    private TicketStatus status;
    private TicketPriority priority;
}
