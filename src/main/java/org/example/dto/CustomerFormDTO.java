package org.example.dto;

import lombok.Data;
import org.example.enums.Gender;

import java.io.Serial;
import java.io.Serializable;

/**
 * Данные формы создания клиента. Не содержит JPA-сущностей, поэтому подходит
 * для хранения в модели JSF-страницы.
 */
@Data
public class CustomerFormDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
    private Integer age;
    private Gender gender;
}
