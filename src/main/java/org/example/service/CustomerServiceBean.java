package org.example.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.example.dto.CustomerFormDTO;
import org.example.entity.Customer;
import org.example.exception.TicketOperationException;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Сессионный EJB для создания клиентов и поиска их по полному email.
 * Регистр email сохраняется и учитывается при проверке уникальности.
 */
@Stateless
public class CustomerServiceBean implements CustomerService {

    /** Проверяет базовый формат email после удаления пробелов по краям. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+$");

    /** Контекст JPA с транзакциями, управляемыми сервером приложений. */
    @PersistenceContext(unitName = "SupportPU")
    private EntityManager em;

    /**
     * Создаёт клиента с сохранением исходного регистра email.
     * Предварительная проверка даёт понятную ошибку, а ограничение БД
     * защищает от одновременного создания клиентов с одинаковым email.
     *
     * @param form данные клиента
     * @return ID созданного клиента
     * @throws TicketOperationException если форма неверна или email уже занят
     */
    @Override
    public Long createCustomer(CustomerFormDTO form) {
        if (form == null) {
            throw new TicketOperationException("Customer details are required.");
        }

        String name = requiredText(form.getName(), "Customer name");
        String email = validateEmail(form.getEmail());
        if (form.getAge() != null && form.getAge() < 0) {
            throw new TicketOperationException("Customer age cannot be negative.");
        }
        if (findCustomerIdByEmail(email) != null) {
            throw new TicketOperationException("A customer with this email already exists.");
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setAge(form.getAge());
        customer.setGender(form.getGender());
        try {
            em.persist(customer);
            em.flush();
        } catch (PersistenceException e) {
            if (isUniqueViolation(e)) {
                throw new TicketOperationException("A customer with this email already exists.");
            }
            throw e;
        }
        return customer.getCustomerId();
    }

    /**
     * Ищет клиента по полному email, сохраняя и учитывая регистр символов.
     *
     * @param value email клиента
     * @return ID клиента или {@code null}, если совпадения нет
     * @throws TicketOperationException если email неверен или найдены дубликаты
     */
    @Override
    public Long findCustomerIdByEmail(String value) {
        String email = validateEmail(value);
        List<Long> ids = em.createQuery(
                        "SELECT c.customerId FROM Customer c WHERE c.email = :email", Long.class)
                .setParameter("email", email)
                .setMaxResults(2)
                .getResultList();
        if (ids.size() > 1) {
            throw new TicketOperationException("More than one customer has this email. Resolve the duplicate first.");
        }
        return ids.isEmpty() ? null : ids.getFirst();
    }

    /**
     * Проверяет email без изменения регистра символов.
     *
     * @param value значение из формы
     * @return email без окружающих пробелов
     * @throws TicketOperationException если значение пустое, длинное или неверного формата
     */
    private String validateEmail(String value) {
        String email = requiredText(value, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new TicketOperationException("Enter a valid email address.");
        }
        return email;
    }

    /**
     * Распознаёт нарушение ограничения уникальности PostgreSQL в цепочке причин.
     *
     * @param error ошибка JPA
     * @return {@code true} для SQLSTATE 23505
     */
    private boolean isUniqueViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет обязательное текстовое поле и длину значения.
     *
     * @param value исходное значение
     * @param field название поля для сообщения об ошибке
     * @return значение без окружающих пробелов
     * @throws TicketOperationException если значение пустое или слишком длинное
     */
    private String requiredText(String value, String field) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new TicketOperationException(field + " is required.");
        }
        if (text.length() > 150) {
            throw new TicketOperationException(field + " must be " + 150 + " characters or fewer.");
        }
        return text;
    }
}
