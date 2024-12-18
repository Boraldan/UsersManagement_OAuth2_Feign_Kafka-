CREATE TABLE IF NOT EXISTS t_user (
    user_id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(30)  NOT NULL UNIQUE,
    last_name VARCHAR(50) ,
    first_name VARCHAR(50)  ,
    middle_name VARCHAR(50),
    date_birth DATE,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(50),
    photo_url VARCHAR(255)
    );