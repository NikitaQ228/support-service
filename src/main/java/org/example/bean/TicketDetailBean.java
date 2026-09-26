package org.example.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.example.dto.TicketDetailDTO;
import org.example.dto.TicketFormDTO;
import org.example.enums.TicketChannel;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;
import org.example.enums.TicketType;
import org.example.exception.TicketOperationException;
import org.example.service.TicketService;

import java.io.Serializable;
import java.util.Map;

/**
 * Модель JSF-страницы с подробной информацией и редактированием обращения.
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

    @Getter
    private boolean editing;

    @Getter @Setter
    private TicketFormDTO editForm;

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

    /** Копирует текущую карточку в форму и включает режим редактирования. */
    public void startEdit() {
        if (ticket != null) {
            editForm = TicketFormDTO.fromDetail(ticket);
            editing = true;
        }
    }

    /** Отменяет редактирование без обращения к базе данных. */
    public void cancelEdit() {
        editForm = null;
        editing = false;
        FacesFormReset.reset("ticketEditor");
    }

    /**
     * Сохраняет изменения через EJB и обновляет отображаемую карточку.
     * При ошибке оставляет форму открытой с введёнными значениями.
     */
    public void save() {
        if (ticket == null || !editing) {
            return;
        }
        try {
            ticket = ticketService.updateTicket(ticket.getTicketId(), editForm);
            cancelEdit();
        } catch (TicketOperationException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    /**
     * Удаляет обращение и направляет оператора к списку. Подтверждение удаления
     * выполняется в интерфейсе до отправки формы.
     *
     * @return переход на список после удаления либо {@code null} при ошибке
     */
    public String delete() {
        if (ticket == null) {
            return null;
        }
        try {
            ticketService.deleteTicket(ticket.getTicketId());
            return "tickets?faces-redirect=true";
        } catch (TicketOperationException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
            return null;
        }
    }

    /**
     * Добавляет сообщение для текущей страницы JSF.
     *
     * @param severity важность сообщения
     * @param text текст для оператора
     */
    private void showMessage(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, null));
    }

    /** @return все статусы обращения для формы */
    public TicketStatus[] getAvailableStatuses() {
        return TicketStatus.values();
    }

    /** @return все приоритеты обращения для формы */
    public TicketPriority[] getAvailablePriorities() {
        return TicketPriority.values();
    }

    /** @return все категории обращения для формы */
    public TicketType[] getAvailableTicketTypes() {
        return TicketType.values();
    }

    /** @return все каналы обращения для формы */
    public TicketChannel[] getAvailableChannels() {
        return TicketChannel.values();
    }
}
