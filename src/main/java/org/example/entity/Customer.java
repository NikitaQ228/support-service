package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.Gender;

import java.util.List;

/**
 * JPA-сущность клиента, соответствующая таблице {@code customers}.
 * Один клиент может быть связан с несколькими обращениями.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "tickets")
public class Customer {

    /** Идентификатор, создаваемый базой данных при сохранении нового клиента. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    /** Обращения клиента; обратная сторона связи с {@link Ticket}. */
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    /**
     * Сравнивает сохранённых клиентов по ID. Новые клиенты без ID равны только самим себе.
     *
     * @param other объект для сравнения
     * @return {@code true}, если объекты представляют одного сохранённого клиента
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Customer customer)) {
            return false;
        }
        return getCustomerId() != null && getCustomerId().equals(customer.getCustomerId());
    }

    /**
     * Возвращает хеш-код, который не меняется после присвоения ID базой данных.
     *
     * @return постоянный хеш-код типа клиента
     */
    @Override
    public int hashCode() {
        return Customer.class.hashCode();
    }
}
