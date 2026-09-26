package org.example.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.example.dto.CustomerFormDTO;
import org.example.enums.Gender;
import org.example.exception.TicketOperationException;
import org.example.service.CustomerService;

import java.io.Serial;
import java.io.Serializable;

/** Модель отдельной JSF-страницы создания клиента. */
@Named
@ViewScoped
public class CustomerCreateBean implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EJB
    private CustomerService customerService;

    @Getter @Setter
    private CustomerFormDTO form = new CustomerFormDTO();

    /** Подставляет email, переданный из предложения создать отсутствующего клиента. */
    @PostConstruct
    public void init() {
        String email = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("email");
        if (email != null && email.length() <= 150) {
            form.setEmail(email);
        }
    }

    /**
     * Сохраняет клиента и возвращает оператора к списку обращений.
     * При ошибке оставляет данные в форме для исправления.
     *
     * @return переход к списку либо {@code null} при ошибке
     */
    public String create() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            customerService.createCustomer(form);
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Customer created.", null));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "tickets?faces-redirect=true";
        } catch (TicketOperationException e) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        }
    }

    /** @return значения пола клиента из модели данных */
    public Gender[] getAvailableGenders() {
        return Gender.values();
    }
}
