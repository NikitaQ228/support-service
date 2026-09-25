package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.example.enums.TicketChannel;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;
import org.example.enums.TicketType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA-сущность обращения в службу поддержки, соответствующая таблице {@code tickets}.
 * Каждое обращение может быть связано с одним клиентом.
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
public class Ticket {

    /** Идентификатор, создаваемый базой данных при сохранении нового обращения. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    /** Клиент, создавший обращение; связь может отсутствовать. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "product_purchased", length = 150)
    private String productPurchased;

    @Column(name = "date_of_purchase")
    private LocalDate dateOfPurchase;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type")
    private TicketType ticketType;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    private TicketChannel channel;

    /** Зафиксированное решение по обращению. */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "first_response_time")
    private LocalDateTime firstResponseTime;

    /** Дата и время разрешения обращения из исходного набора данных. */
    @Column(name = "time_to_resolution")
    private LocalDateTime timeToResolution;

    /** Оценка клиента по шкале от 1 до 5, если она была оставлена. */
    @Min(value = 1)
    @Max(value = 5)
    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;

    /**
     * Сравнивает сохранённые обращения по ID. Новые обращения без ID равны только самим себе.
     *
     * @param other объект для сравнения
     * @return {@code true}, если объекты представляют одно сохранённое обращение
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Ticket ticket)) {
            return false;
        }
        return getTicketId() != null && getTicketId().equals(ticket.getTicketId());
    }

    /**
     * Возвращает хеш-код, который не меняется после присвоения ID базой данных.
     *
     * @return постоянный хеш-код типа обращения
     */
    @Override
    public int hashCode() {
        return Ticket.class.hashCode();
    }
}
