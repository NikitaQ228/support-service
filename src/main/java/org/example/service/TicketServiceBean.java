package org.example.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.example.dto.PageResult;
import org.example.dto.TicketBriefDTO;
import org.example.dto.TicketDetailDTO;
import org.example.dto.TicketFilterDTO;
import org.example.dto.TicketFormDTO;
import org.example.entity.Customer;
import org.example.entity.Ticket;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;
import org.example.exception.CustomerNotFoundException;
import org.example.exception.TicketOperationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса обращений на основе JPA и сессионного EJB-компонента.
 * Для списка выбирает только поля, необходимые странице, а для карточки загружает клиента.
 */
@Stateless
public class TicketServiceBean implements TicketService {

    @PersistenceContext(unitName = "SupportPU")
    private EntityManager em;

    @EJB
    private CustomerService customerService;

    /** {@inheritDoc} */
    @Override
    public PageResult<TicketBriefDTO> getFilteredTickets(int page, int size, TicketFilterDTO filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // --- COUNT ---
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Ticket> countRoot = countQuery.from(Ticket.class);
        Join<Ticket, Customer> countCustomerJoin = countRoot.join("customer", JoinType.LEFT);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, countCustomerJoin, filter)
                .toArray(new Predicate[0]));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        // --- DATA ---
        CriteriaQuery<Tuple> dataQuery = cb.createQuery(Tuple.class);
        Root<Ticket> root = dataQuery.from(Ticket.class);
        Join<Ticket, Customer> customerJoin = root.join("customer", JoinType.LEFT);

        dataQuery.multiselect(
                root.get("ticketId").alias("ticketId"),
                customerJoin.get("email").alias("customerEmail"),
                root.get("dateOfPurchase").alias("dateOfPurchase"),
                root.get("subject").alias("subject"),
                root.get("status").alias("status"),
                root.get("priority").alias("priority")
        );
        dataQuery.where(buildPredicates(cb, root, customerJoin, filter)
                .toArray(new Predicate[0]));
        dataQuery.orderBy(cb.desc(root.get("ticketId")));

        List<TicketBriefDTO> content = em.createQuery(dataQuery)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList()
                .stream()
                .map(t -> new TicketBriefDTO(
                        t.get("ticketId", Long.class),
                        t.get("customerEmail", String.class),
                        t.get("dateOfPurchase", LocalDate.class),
                        t.get("subject", String.class),
                        t.get("status", TicketStatus.class),
                        t.get("priority", TicketPriority.class)
                ))
                .toList();

        return new PageResult<>(content, totalElements, page, size);
    }

    /** {@inheritDoc} */
    @Override
    public TicketDetailDTO getTicketById(Long id) {
        return em.createQuery(
                        "SELECT t FROM Ticket t LEFT JOIN FETCH t.customer WHERE t.ticketId = :id",
                        Ticket.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst()
                .map(TicketDetailDTO::new)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Long createTicket(TicketFormDTO form) {
        if (form == null) {
            throw new TicketOperationException("Ticket details are required.");
        }

        Long customerId = customerService.findCustomerIdByEmail(form.getCustomerEmail());
        if (customerId == null) {
            throw new CustomerNotFoundException();
        }

        Ticket ticket = new Ticket();
        ticket.setCustomer(em.getReference(Customer.class, customerId));
        applyTicketFields(ticket, form, true);
        em.persist(ticket);
        em.flush();
        return ticket.getTicketId();
    }

    /** {@inheritDoc} */
    @Override
    public TicketDetailDTO updateTicket(Long id, TicketFormDTO form) {
        Ticket ticket = findTicketForChange(id);
        applyTicketFields(ticket, form, false);
        em.flush();
        return new TicketDetailDTO(ticket);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteTicket(Long id) {
        Ticket ticket = findTicketForChange(id);
        em.remove(ticket);
        em.flush();
    }

    /**
     * Находит обращение для изменения и сообщает об отсутствующей записи.
     *
     * @param id идентификатор обращения
     * @return управляемая JPA-сущность
     */
    private Ticket findTicketForChange(Long id) {
        if (id == null || id <= 0) {
            throw new TicketOperationException("Invalid ticket ID.");
        }
        Ticket ticket = em.find(Ticket.class, id);
        if (ticket == null) {
            throw new TicketOperationException("Ticket not found.");
        }
        return ticket;
    }

    /**
     * Проверяет и переносит значения формы в обращение. При создании задаёт
     * начальные статус и приоритет, если оператор оставил их пустыми.
     *
     * @param ticket изменяемое обращение
     * @param form данные формы
     * @param creating признак создания нового обращения
     */
    private void applyTicketFields(Ticket ticket, TicketFormDTO form, boolean creating) {
        if (form == null) {
            throw new TicketOperationException("Ticket details are required.");
        }
        String subject = requiredText(form.getSubject(), "Subject", 255);
        String product = optionalText(form.getProductPurchased());
        if (product != null && product.length() > 150) {
            throw new TicketOperationException("Product name must be 150 characters or fewer.");
        }
        Integer rating = form.getSatisfactionRating();
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new TicketOperationException("Satisfaction rating must be between 1 and 5.");
        }

        LocalDate purchaseDate = parseDate(form.getDateOfPurchase());
        LocalDateTime firstResponse = parseDateTime(form.getFirstResponseTime(), "First response time");
        LocalDateTime resolvedAt = parseDateTime(form.getTimeToResolution(), "Time to resolution");

        ticket.setSubject(subject);
        ticket.setDescription(optionalText(form.getDescription()));
        ticket.setStatus(creating && form.getStatus() == null ? TicketStatus.Open : form.getStatus());
        ticket.setPriority(creating && form.getPriority() == null ? TicketPriority.Medium : form.getPriority());
        ticket.setTicketType(form.getTicketType());
        ticket.setChannel(form.getChannel());
        ticket.setDateOfPurchase(purchaseDate);
        ticket.setProductPurchased(product);
        ticket.setResolution(optionalText(form.getResolution()));
        ticket.setFirstResponseTime(firstResponse);
        ticket.setTimeToResolution(resolvedAt);
        ticket.setSatisfactionRating(rating);
    }

    /**
     * Проверяет обязательный текст и максимальную длину столбца.
     *
     * @param value исходный текст
     * @param field название поля для сообщения
     * @param maxLength допустимая длина
     * @return текст без окружающих пробелов
     */
    private String requiredText(String value, String field, int maxLength) {
        String text = optionalText(value);
        if (text == null) {
            throw new TicketOperationException(field + " is required.");
        }
        if (text.length() > maxLength) {
            throw new TicketOperationException(field + " must be " + maxLength + " characters or fewer.");
        }
        return text;
    }

    /**
     * Превращает пустое значение формы в {@code null}.
     *
     * @param value исходный текст
     * @return непустой текст без окружающих пробелов либо {@code null}
     */
    private String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * Разбирает необязательную дату покупки в формате ISO.
     *
     * @param value текст из поля даты
     * @return дата либо {@code null}
     */
    private LocalDate parseDate(String value) {
        String text = optionalText(value);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new TicketOperationException("Purchase date must use YYYY-MM-DD.");
        }
    }

    /**
     * Разбирает необязательную дату со временем в формате локального времени HTML.
     *
     * @param value текст из формы
     * @param field название поля для сообщения
     * @return дата и время либо {@code null}
     */
    private LocalDateTime parseDateTime(String value, String field) {
        String text = optionalText(value);
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new TicketOperationException(field + " must use YYYY-MM-DDTHH:MM.");
        }
    }

    /**
     * Создаёт условия выборки для основного запроса и запроса подсчёта.
     *
     * @param cb построитель запросов JPA
     * @param root корень запроса по обращениям
     * @param customerJoin соединение с клиентом для поиска по части email
     * @param filter заданные пользователем фильтры
     * @return список условий; пустой список означает отсутствие фильтрации
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Ticket> root,
                                            Join<Ticket, Customer> customerJoin, TicketFilterDTO filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter != null) {
            if (filter.getEmail() != null && !filter.getEmail().trim().isEmpty()) {
                String fragment = filter.getEmail().trim()
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
                predicates.add(cb.like(customerJoin.get("email"),
                        "%" + fragment + "%", '\\'));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }
        }

        return predicates;
    }
}
