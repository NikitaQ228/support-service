package org.example.dto;

import lombok.Data;
import org.example.enums.TicketChannel;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;
import org.example.enums.TicketType;

import java.io.Serial;
import java.io.Serializable;

/**
 * Данные формы создания и редактирования обращения. Даты передаются строками
 * в формате HTML-полей и проверяются бизнес-слоем перед сохранением.
 */
@Data
public class TicketFormDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Email существующего клиента; используется только при создании обращения. */
    private String customerEmail;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketType ticketType;
    private TicketChannel channel;
    private String dateOfPurchase;
    private String productPurchased;
    private String resolution;
    private String firstResponseTime;
    private String timeToResolution;
    private Integer satisfactionRating;

    /**
     * Копирует данные карточки в отдельную модель формы. Связанный клиент
     * при редактировании остаётся прежним.
     *
     * @param detail данные открытого обращения
     * @return заполненная форма редактирования
     */
    public static TicketFormDTO fromDetail(TicketDetailDTO detail) {
        TicketFormDTO form = new TicketFormDTO();
        form.setSubject(detail.getSubject());
        form.setDescription(detail.getDescription());
        form.setStatus(detail.getStatus());
        form.setPriority(detail.getPriority());
        form.setTicketType(detail.getTicketType());
        form.setChannel(detail.getChannel());
        form.setDateOfPurchase(detail.getDateOfPurchase() == null
                ? null : detail.getDateOfPurchase().toString());
        form.setProductPurchased(detail.getProductPurchased());
        form.setResolution(detail.getResolution());
        form.setFirstResponseTime(detail.getFirstResponseTime() == null
                ? null : detail.getFirstResponseTime().toString());
        form.setTimeToResolution(detail.getTimeToResolution() == null
                ? null : detail.getTimeToResolution().toString());
        form.setSatisfactionRating(detail.getSatisfactionRating());
        return form;
    }
}
