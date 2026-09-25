package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Customer;
import org.example.entity.Ticket;
import org.example.enums.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Данные обращения и связанного клиента для страницы подробной информации.
 * Поля клиента вынесены в DTO, чтобы страница не обращалась напрямую к JPA-сущностям.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketType ticketType;
    private TicketChannel channel;
    private LocalDate dateOfPurchase;
    private String productPurchased;
    private String resolution;
    private LocalDateTime firstResponseTime;
    private LocalDateTime timeToResolution;
    private Integer satisfactionRating;
    private String customerName;
    private String customerEmail;
    private Integer customerAge;
    private Gender customerGender;

    /**
     * Создаёт представление из обращения и связанного с ним клиента.
     * Отсутствующий клиент допускается, поскольку связь в базе может быть пустой.
     *
     * @param ticket найденное обращение
     */
    public TicketDetailDTO(Ticket ticket) {
        Customer c = ticket.getCustomer();
        this.ticketId = ticket.getTicketId();
        this.subject = ticket.getSubject();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus();
        this.priority = ticket.getPriority();
        this.ticketType = ticket.getTicketType();
        this.channel = ticket.getChannel();
        this.dateOfPurchase = ticket.getDateOfPurchase();
        this.productPurchased = ticket.getProductPurchased();
        this.resolution = ticket.getResolution();
        this.firstResponseTime = ticket.getFirstResponseTime();
        this.timeToResolution = ticket.getTimeToResolution();
        this.satisfactionRating = ticket.getSatisfactionRating();
        this.customerName = c != null ? c.getName() : null;
        this.customerEmail = c != null ? c.getEmail() : null;
        this.customerAge = c != null ? c.getAge() : null;
        this.customerGender = c != null ? c.getGender() : null;
    }
}
