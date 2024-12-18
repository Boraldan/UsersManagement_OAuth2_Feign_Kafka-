INSERT INTO t_user (username, last_name, first_name, middle_name, date_birth, email, phone_number)
VALUES ('ivanov', 'Иванов', 'Иван', 'Иванович', '1980-01-01', 'ivanov@mail.ru', '+79012345678'),
       ('petrov', 'Петров', 'Петр', 'Петрович', '1990-02-15', 'petrov@mail.ru', '+79012345679'),
       ('admin', 'Админов', 'Андмин', 'Админович', '1982-12-17', 'admin@mail.ru', '+79012345617')
ON CONFLICT (username) DO NOTHING;