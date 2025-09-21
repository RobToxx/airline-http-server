DROP TABLE reservations;
DROP TABLE bookings;
DROP TABLE sessions;
DROP TABLE users;
DROP TABLE flights;
DROP TABLE seats;
DROP TABLE airplanes;

-- 1) Tabla de Aviones (airplanes)
CREATE TABLE airplanes (
    id            SERIAL PRIMARY KEY,
    model         VARCHAR(50) NOT NULL
);

-- Tabla de Asientos
CREATE TABLE seats (
    airplane_id   INTEGER NOT NULL,
    id            VARCHAR(3) NOT NULL,
    class         VARCHAR(20) NOT NULL
    CHECK (class IN ('ECONOMY', 'FIRST')),
    PRIMARY KEY (airplane_id, id),
    FOREIGN KEY (airplane_id) REFERENCES airplanes(id)
);

-- Tabla de Vuelos 
CREATE TABLE flights (
    id           SERIAL PRIMARY KEY,
    airplane_id  INTEGER NOT NULL,
    origin       VARCHAR(50) NOT NULL,
    destination  VARCHAR(50) NOT NULL,
    departure    TIMESTAMP NOT NULL,
    FOREIGN KEY (airplane_id) REFERENCES airplanes(id),
    CHECK (origin <> destination)
);

-- Tabla de Usuarios
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(48) NOT NULL,
    email       VARCHAR(64) UNIQUE NOT NULL,
    password    VARCHAR(128) NOT NULL,
    salt        VARCHAR(32) NOT NULL
);

-- Tabla de Tickets
CREATE TABLE bookings (
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER NOT NULL,
    flight_id    INTEGER NOT NULL,
    airplane_id  INTEGER NOT NULL,
    seat_id      VARCHAR(3) NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    passenger_type VARCHAR(6) NOT NULL,
    price        NUMERIC NOT NULL,
    CHECK (passenger_type IN ('CHILD', 'ADULT', 'SENIOR')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (flight_id) REFERENCES flights(id),
    FOREIGN KEY (airplane_id) REFERENCES airplanes(id),
    FOREIGN KEY (airplane_id, seat_id) REFERENCES seats(airplane_id, id)
);

-- Tabla de Reservaciones 
CREATE TABLE reservations (
    id           VARCHAR(36) PRIMARY KEY,
    user_id      INTEGER NOT NULL,
    flight_id    INTEGER NOT NULL,
    airplane_id  INTEGER NOT NULL,
    seat_id      VARCHAR(3) NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (flight_id) REFERENCES flights(id),
    FOREIGN KEY (airplane_id, seat_id) REFERENCES seats(airplane_id, id)
);

-- Tabla de Sesiones
CREATE TABLE sessions (
    id                  VARCHAR(36) PRIMARY KEY,
    user_id             INTEGER NOT NULL,
    expiration_date     TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

TRUNCATE tickets, reservations, sessions, users, flights, seats, airplanes RESTART IDENTITY CASCADE;