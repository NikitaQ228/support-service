package org.example.bean;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.example.dto.TicketFormDTO;
import org.example.enums.TicketChannel;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;
import org.example.enums.TicketType;
import org.example.exception.CustomerNotFoundException;
import org.example.exception.TicketOperationException;
import org.example.service.TicketService;

import java.io.Serial;
import java.io.Serializable;

/** Модель отдельной JSF-страницы создания обращения. */
@Named
@ViewScoped
public class TicketCreateBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EJB
    private TicketService ticketService;

    @Getter @Setter
    private TicketFormDTO form = new TicketFormDTO();

    /** Показывать ли браузерное предложение перейти к созданию клиента. */
    @Getter
    private boolean customerNotFound;

    /** Задаёт начальные значения статуса и приоритета нового обращения. */
    public TicketCreateBean() {
        form.setStatus(TicketStatus.Open);
        form.setPriority(TicketPriority.Medium);
    }

    /**
     * Сохраняет обращение и открывает его карточку. При ошибке показывает
     * сообщение и оставляет заполненную форму на странице.
     *
     * @return переход к новому обращению либо {@code null} при ошибке
     */
    public String create() {
        customerNotFound = false;
        try {
            Long id = ticketService.createTicket(form);
            return "ticket-detail?faces-redirect=true&ticketId=" + id;
        } catch (CustomerNotFoundException e) {
            customerNotFound = true;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        } catch (TicketOperationException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        }
    }

    /** @return все доступные статусы обращения */
    public TicketStatus[] getAvailableStatuses() {
        return TicketStatus.values();
    }

    /** @return все доступные приоритеты обращения */
    public TicketPriority[] getAvailablePriorities() {
        return TicketPriority.values();
    }

    /** @return все категории обращения */
    public TicketType[] getAvailableTicketTypes() {
        return TicketType.values();
    }

    /** @return все каналы обращения */
    public TicketChannel[] getAvailableChannels() {
        return TicketChannel.values();
    }
}
