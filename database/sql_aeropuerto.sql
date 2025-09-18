DROP TABLE reservations;
DROP TABLE tickets;
DROP TABLE users;
DROP TABLE flights;
DROP TABLE seats;
DROP TABLE airplanes;

-- 1) Tabla de Aviones (airplanes)
CREATE TABLE airplanes (
    id            VARCHAR(4) PRIMARY KEY,
    model         VARCHAR(50) NOT NULL
);

-- Tabla de Asientos
CREATE TABLE seats (
    airplane_id   VARCHAR(4) NOT NULL,
    id            VARCHAR(3) NOT NULL,
    class         VARCHAR(20) NOT NULL
    CHECK (class IN ('ECONOMY', 'FIRST')),
    PRIMARY KEY (airplane_id, id),
    FOREIGN KEY (airplane_id) REFERENCES airplanes(id)
);

-- Tabla de Vuelos 
CREATE TABLE flights (
    id           VARCHAR(8) PRIMARY KEY,
    airplane_id  VARCHAR(4) NOT NULL,
    origin       VARCHAR(50) NOT NULL,
    destination  VARCHAR(50) NOT NULL,
    departure    DATE NOT NULL,
    FOREIGN KEY (airplane_id) REFERENCES airplanes(id),
    CHECK (origin <> destination)
);

-- Tabla de Usuarios
CREATE TABLE users (
    id          VARCHAR(8) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL
    password    VARCHAR(200) NOT NULL
);

-- Tabla de Tickets
CREATE TABLE tickets (
    id           VARCHAR(10) PRIMARY KEY,
    user_id      VARCHAR(8) NOT NULL,
    flight_id    VARCHAR(8) NOT NULL,
    airplane_id  VARCHAR(4) NOT NULL,
    seat_id      VARCHAR(3) NOT NULL,
    purchase_date DATE DEFAULT CURRENT_DATE,
    price        MONEY NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (flight_id) REFERENCES flights(id),
    FOREIGN KEY (airplane_id, seat_id)
    REFERENCES seats(airplane_id, id)
);

--Tabla de Reservaciones 
CREATE TABLE reservations (
    id           VARCHAR(10) PRIMARY KEY,
    user_id      VARCHAR(8) NOT NULL,
    flight_id    VARCHAR(8) NOT NULL,
    airplane_id  VARCHAR(4) NOT NULL,
    seat_id  VARCHAR(3) NOT NULL,
    expiration_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (flight_id) REFERENCES flights(id),
    FOREIGN KEY (airplane_id, seat_id)
    REFERENCES seats(airplane_id, id)
);

TRUNCATE tickets, reservations, flights, seats, airplanes, users RESTART IDENTITY CASCADE;

-- ========================
-- Aviones
-- ========================
INSERT INTO airplanes (id, model, capacity) VALUES
('A001', 'Airbus A320', 180),
('A002', 'Boeing 737', 200),
('A003', 'Embraer E190', 100),
('A004', 'Boeing 787', 242),
('A005', 'Airbus A350', 300),
('A006', 'Bombardier CRJ900', 90),
('A007', 'Boeing 777', 396),
('A008', 'Airbus A321', 220);

-- ========================
-- Asientos (30 Economy + 12 First por avión)
-- ========================
DO $$
DECLARE
    a TEXT;
    i INT;
BEGIN
    FOR a IN SELECT id FROM airplanes LOOP
        -- First Class: F1..F12
        FOR i IN 1..12 LOOP
            INSERT INTO seats (airplane_id, id, class)
            VALUES (a, 'F' || i, 'FIRST');
        END LOOP;
        -- Economy: E1..E30
        FOR i IN 1..30 LOOP
            INSERT INTO seats (airplane_id, id, class)
            VALUES (a, 'E' || i, 'ECONOMY');
        END LOOP;
    END LOOP;
END$$;

-- ========================
-- Vuelos (ejemplo: 20 vuelos)
-- ========================
INSERT INTO flights (id, airplane_id, origin, destination, departure) VALUES
('FL000001', 'A001', 'Mexico City', 'New York', '2025-10-01'),
('FL000002', 'A002', 'New York', 'Los Angeles', '2025-10-02'),
('FL000003', 'A003', 'Los Angeles', 'Chicago', '2025-10-03'),
('FL000004', 'A004', 'Chicago', 'Miami', '2025-10-04'),
('FL000005', 'A005', 'Miami', 'Houston', '2025-10-05'),
('FL000006', 'A006', 'Houston', 'San Francisco', '2025-10-06'),
('FL000007', 'A007', 'San Francisco', 'Tokyo', '2025-10-07'),
('FL000008', 'A008', 'Tokyo', 'Mexico City', '2025-10-08'),
('FL000009', 'A001', 'Mexico City', 'Toronto', '2025-10-09'),
('FL000010', 'A002', 'Toronto', 'Paris', '2025-10-10'),
('FL000011', 'A003', 'Paris', 'London', '2025-10-11'),
('FL000012', 'A004', 'London', 'Rome', '2025-10-12'),
('FL000013', 'A005', 'Rome', 'Berlin', '2025-10-13'),
('FL000014', 'A006', 'Berlin', 'Madrid', '2025-10-14'),
('FL000015', 'A007', 'Madrid', 'Dubai', '2025-10-15'),
('FL000016', 'A008', 'Dubai', 'Singapore', '2025-10-16'),
('FL000017', 'A001', 'Singapore', 'Sydney', '2025-10-17'),
('FL000018', 'A002', 'Sydney', 'Mexico City', '2025-10-18'),
('FL000019', 'A003', 'Mexico City', 'Los Angeles', '2025-10-19'),
('FL000020', 'A004', 'Los Angeles', 'New York', '2025-10-20');

-- ========================
-- Usuarios
-- ========================
INSERT INTO users (id, name1, email, passwords) VALUES
('U0000001', 'Alice Johnson', 'alice@example.com', 'hashed_pw1'),
('U0000002', 'Bob Smith', 'bob@example.com', 'hashed_pw2'),
('U0000003', 'Carlos Pérez', 'carlos@example.com', 'hashed_pw3'),
('U0000004', 'Diana Torres', 'diana@example.com', 'hashed_pw4'),
('U0000005', 'Erik Müller', 'erik@example.com', 'hashed_pw5'),
('U0000006', 'Fatima Khan', 'fatima@example.com', 'hashed_pw6'),
('U0000007', 'George Brown', 'george@example.com', 'hashed_pw7'),
('U0000008', 'Hiro Tanaka', 'hiro@example.com', 'hashed_pw8');

-- ========================
-- Tickets (ejemplo: 5)
-- ========================
INSERT INTO tickets (id, user_id, flight_id, airplane_id, seat_id, price) VALUES
('T000000001', 'U0000001', 'FL000001', 'A001', 'F1', 500),
('T000000002', 'U0000002', 'FL000002', 'A002', 'E5', 200),
('T000000003', 'U0000003', 'FL000003', 'A003', 'E10', 180),
('T000000004', 'U0000004', 'FL000004', 'A004', 'F3', 600),
('T000000005', 'U0000005', 'FL000005', 'A005', 'E20', 220);

-- ========================
-- Reservaciones (ejemplo: 5)
-- ========================
INSERT INTO reservations (id, user_id, flight_id, airplane_id, seat_id, expiration_date) VALUES
('R000000001', 'U0000006', 'FL000006', 'A006', 'E2', '2025-09-20'),
('R000000002', 'U0000007', 'FL000007', 'A007', 'F5', '2025-09-21'),
('R000000003', 'U0000008', 'FL000008', 'A008', 'E15', '2025-09-22'),
('R000000004', 'U0000001', 'FL000009', 'A001', 'E25', '2025-09-23'),
('R000000005', 'U0000002', 'FL000010', 'A002', 'F7', '2025-09-24');