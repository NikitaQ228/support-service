package org.example.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import org.example.dto.TicketDetailDTO;
import org.example.service.TicketService;

import java.io.Serializable;
import java.util.Map;

/**
 * Модель JSF-страницы с подробной информацией об одном обращении.
 * Получает ID из параметра URL и хранит данные в области видимости страницы.
 */
@Named
@ViewScoped
public class TicketDetailBean implements Serializable {

    @EJB
    private TicketService ticketService;

    @Getter
    private TicketDetailDTO ticket;

    @Getter
    private String errorMessage;

    /**
     * Проверяет параметр {@code ticketId} и загружает обращение при открытии страницы.
     * Для отсутствующего или неверного ID задаёт сообщение вместо выполнения запроса.
     */
    @PostConstruct
    public void init() {
        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();
        String ticketIdParam = params.get("ticketId");
        if (ticketIdParam == null || ticketIdParam.isBlank()) {
            errorMessage = "Ticket ID is required.";
            return;
        }

        try {
            long ticketId = Long.parseLong(ticketIdParam);
            if (ticketId <= 0) {
                errorMessage = "Invalid ticket ID.";
                return;
            }
            loadTicket(ticketId);
        } catch (NumberFormatException e) {
            errorMessage = "Invalid ticket ID.";
        }
    }

    /**
     * Загружает обращение и задаёт сообщение, если запись не найдена.
     *
     * @param ticketId идентификатор обращения
     */
    public void loadTicket(Long ticketId) {
        this.ticket = ticketService.getTicketById(ticketId);
        errorMessage = ticket == null ? "Ticket not found." : null;
    }
}
