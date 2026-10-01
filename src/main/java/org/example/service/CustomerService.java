package org.example.service;

import jakarta.ejb.Local;
import org.example.dto.CustomerFormDTO;
import org.example.exception.TicketOperationException;

/** Локальный EJB-контракт для операций с клиентами. */
@Local
public interface CustomerService {

    /**
     * Создаёт клиента, проверив поля и уникальность email с учётом регистра.
     *
     * @param form данные нового клиента
     * @return идентификатор созданного клиента
     * @throws TicketOperationException если данные неверны или email уже занят
     */
    Long createCustomer(CustomerFormDTO form);

    /**
     * Находит ID клиента по полному email с учётом регистра.
     *
     * @param email полный email клиента
     * @return ID или {@code null}, если клиент не найден
     * @throws TicketOperationException если email неверен или в базе есть одинаковые email
     */
    Long findCustomerIdByEmail(String email);
}
