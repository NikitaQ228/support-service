package org.example.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.example.dto.PageResult;
import org.example.dto.TicketBriefDTO;
import org.example.dto.TicketDetailDTO;
import org.example.dto.TicketFilterDTO;
import org.example.entity.Customer;
import org.example.entity.Ticket;
import org.example.enums.TicketPriority;
import org.example.enums.TicketStatus;

import java.time.LocalDate;
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

    /**
     * Создаёт условия выборки для основного запроса и запроса подсчёта.
     *
     * @param cb построитель запросов JPA
     * @param root корень запроса по обращениям
     * @param customerJoin соединение с клиентом для фильтра по email
     * @param filter заданные пользователем фильтры
     * @return список условий; пустой список означает отсутствие фильтрации
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Ticket> root,
                                            Join<Ticket, Customer> customerJoin, TicketFilterDTO filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter != null) {
            if (filter.getEmail() != null && !filter.getEmail().trim().isEmpty()) {
                predicates.add(cb.equal(customerJoin.get("email"), filter.getEmail().trim()));
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
