package org.example.exception;

import jakarta.ejb.ApplicationException;

/** Ошибка создания обращения, когда клиент с указанным email не найден. */
@ApplicationException(rollback = true)
public class CustomerNotFoundException extends TicketOperationException {

    /** Создаёт сообщение об отсутствующем клиенте для интерфейса. */
    public CustomerNotFoundException() {
        super("Customer with this email was not found. Add a customer first.");
    }
}
