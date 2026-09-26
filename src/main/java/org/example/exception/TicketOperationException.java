package org.example.exception;

import jakarta.ejb.ApplicationException;

/**
 * Ожидаемая ошибка проверки данных или поиска записи при изменении обращений.
 * Текст ошибки можно показать пользователю, а текущая транзакция откатывается.
 */
@ApplicationException(rollback = true)
public class TicketOperationException extends RuntimeException {

    /**
     * Создаёт ошибку с сообщением для интерфейса.
     *
     * @param message причина отказа в сохранении или удалении
     */
    public TicketOperationException(String message) {
        super(message);
    }
}
