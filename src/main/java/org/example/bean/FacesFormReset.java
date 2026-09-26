package org.example.bean;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;

/** Сбрасывает сохранённые JSF-значения формы после отмены ввода. */
final class FacesFormReset {

    /** Запрещает создание экземпляра служебного класса. */
    private FacesFormReset() {
    }

    /**
     * Удаляет введённые и не прошедшие проверку значения из дерева компонентов.
     * Иначе они могут снова появиться при следующем открытии формы.
     *
     * @param formId идентификатор формы на текущей странице
     */
    static void reset(String formId) {
        UIComponent form = FacesContext.getCurrentInstance().getViewRoot().findComponent(formId);
        if (form != null) {
            resetInputs(form);
        }
    }

    /**
     * Обходит потомков формы и очищает состояние компонентов ввода.
     *
     * @param component текущий компонент дерева JSF
     */
    private static void resetInputs(UIComponent component) {
        if (component instanceof UIInput input) {
            input.resetValue();
        }
        for (UIComponent child : component.getChildren()) {
            resetInputs(child);
        }
        for (UIComponent facet : component.getFacets().values()) {
            resetInputs(facet);
        }
    }
}
