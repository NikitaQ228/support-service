# Архитектура корпоротивных систем
Автор: Бабошин Никита Андреевич, группа 6132-0104022D.

## Ticket Support System (Support Service)

Веб-приложение для работы с обращениями клиентов (тикетами). На главной странице отображается список обращений с поиском по email клиента, фильтрацией по статусу и приоритету, а также постраничной навигацией. Карточка обращения показывает подробности тикета и данные клиента. Интерфейс предусматривает создание клиента и тикета, редактирование и удаление тикета; при создании тикет связывается с существующим клиентом по его полному email.

## Практическая работа № 1

### Задание и реализация

Общее требование: разработать приложение на Jakarta EE с тремя слоями (данные, бизнес-логика, представление) и операциями добавления, изменения и удаления данных. Развёртывание выполняется через средства сервера приложений, без использования IDE для этой операции.

1. **Сервер приложений.** По заданию требуется установить и запустить GlassFish или другой сервер и изучить консоль администратора. Проект использует Jakarta EE 10 и собирается в WAR для развёртывания. Источник данных задаётся в сервере под именем `jdbc/supportDS`.
2. **Реляционная СУБД.** Выбрана PostgreSQL. Подключение приложения к ней выполняется через JTA-источник данных, указанный в [persistence.xml](src/main/resources/META-INF/persistence.xml).
3. **Предметная область и SQL-скрипт.** Модель состоит из сущностей [Customer.java](src/main/java/org/example/entity/Customer.java) и [Ticket.java](src/main/java/org/example/entity/Ticket.java), связанных отношением «один клиент — много обращений». Скрипт [init_database.sql](src/main/resources/script/init_database.sql) создаёт таблицы и заполняет новую базу тестовыми данными; при повторном запуске существующие данные сохраняются.
4. **Слой данных.** Классы `Customer` и `Ticket` размечены аннотациями Jakarta Persistence (JPA). Для статусов и других фиксированных значений используются перечисления; для передачи данных в интерфейс — DTO.
5. **Бизнес-слой.** Сессионные EJB [TicketServiceBean.java](src/main/java/org/example/service/TicketServiceBean.java) и [CustomerServiceBean.java](src/main/java/org/example/service/CustomerServiceBean.java) работают через `EntityManager`. Первый отвечает за обращения; второй создаёт клиентов и ищет их по полному email. Их контракты — [TicketService.java](src/main/java/org/example/service/TicketService.java) и [CustomerService.java](src/main/java/org/example/service/CustomerService.java).
6. **Слой представления.** Страницы [tickets.xhtml](src/main/webapp/tickets.xhtml), [ticket-detail.xhtml](src/main/webapp/ticket-detail.xhtml), [add-ticket.xhtml](src/main/webapp/add-ticket.xhtml) и [add-customer.xhtml](src/main/webapp/add-customer.xhtml) построены на Jakarta Faces (JSF). Управляемые бины связывают таблицу и формы с соответствующими EJB-сервисами.
7. **Совместная работа слоёв.** Maven собирает WAR (`mvn clean package`); пользовательские действия проходят через JSF → EJB → JPA → PostgreSQL. Для работы приложения на сервере необходим настроенный источник данных `jdbc/supportDS` и подготовленная БД.

### Сборка

Требуются JDK 21 и Maven. Из каталога проекта выполните `mvn clean package`. Полученный WAR находится в `target/`.

### Развёртывание

1. Создайте базу PostgreSQL для приложения. Примените [init_database.sql](src/main/resources/script/init_database.sql) к этой базе, например из корня проекта: `psql -U <пользователь> -d <база> -f src/main/resources/script/init_database.sql`. Скрипт создаёт таблицы, загружает данные только в пустую базу.
2. В GlassFish или другом сервере, совместимом с Jakarta EE 10, установите JDBC-драйвер PostgreSQL, создайте JDBC connection pool для этой базы и JDBC resource с JNDI-именем `jdbc/supportDS`. Настройте URL, пользователя и пароль подключения; проверьте соединение в консоли администратора. Имя ресурса должно совпадать с [persistence.xml](src/main/resources/META-INF/persistence.xml).
3. Соберите WAR командой из раздела выше и загрузите `target/support-service-1.0-SNAPSHOT.war` через консоль администратора сервера. Откройте развёрнутое приложение по контекстному пути, указанному сервером. Для развёртывания IDE не требуется.
