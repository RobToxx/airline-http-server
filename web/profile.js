const API_URL = 'http://localhost:8000';

const formatterMX = new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN',
});

// Se ejecuta cuando la página de perfil termina de cargar
window.addEventListener('DOMContentLoaded', () => {
    const spinner = document.getElementById('spinner');
    const bookingsContainer = document.getElementById('bookings-container');
    const sessionId = localStorage.getItem('sessionId');

    // 1. Verificación: Si no hay sesión, no debería estar aquí. Lo mandamos a login.
    if (!sessionId) {
        alert("Necesitas iniciar sesión para ver tus vuelos.");
        window.location.href = 'login.html';
        return;
    }

    // En tu profile.js, reemplaza únicamente la función fetchReservations con esta:

async function fetchBookings() {
    spinner.style.display = 'block';
    try {
        // 1. Pedimos la lista inicial de reservaciones
        const bookingsResponse = await fetch(`${API_URL}/user/books?sessionId=${sessionId}`);

        if (!bookingsResponse.ok) throw new Error('No se pudieron cargar tus vuelos comprados.');
        
        const bookings = await bookingsResponse.json();

        displayBookings(bookings);

    } catch (error) {
        console.error("Error detallado:", error);
        bookingsContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
    } finally {
        spinner.style.display = 'none';
    }
}

    // 3. Función para mostrar las reservaciones en el HTML
    function displayBookings(bookings) {
        if (bookings.length === 0) {
            bookingsContainer.innerHTML = '<p>Aún no tienes vuelos comprados.</p>';
            return;
        }

        const seatClassMap = {
            "FIRST": "Primera",
            "ECONOMY": "Turista"
        };

        const passengerTypeMap = {
            "ADULT": "Adulto",
            "CHILD": "Niño",
            "SENIOR": "Adulto mayor"
        };

        bookings.forEach(book => {
            const flightCard = document.createElement('div');

            flightCard.className = 'flight-card';

            flightCard.innerHTML = `
                <h2><p><strong>Vuelo:</strong> ${book.flight.origin} → ${book.flight.destination}</p></h2>
                <p><strong>Salida:</strong> ${new Date(book.flight.departure).toLocaleString()}</p>
                <p><strong>ID Avión:</strong> ${book.flight.airplaneId}</p>
            `;

            const seatsList = document.createElement('ul');
            seatsList.className = 'seat-list';

            let total = 0

            book.seats.forEach(seat => {
                const seatItem = document.createElement('li');
                seatItem.className = 'seat-item';
                seatItem.innerHTML = `
                    <span><strong>Asiento:</strong> ${seat.seatId}</span> 
                    <span><strong>Clase:</strong> ${seatClassMap[seat.seatClass]}</span>
                    <span><strong>Pasajero:</strong> ${passengerTypeMap[seat.passengerType]}</span>
                    <span><strong>Precio:</strong> ${formatterMX.format(seat.price)}</span>
                    <span><strong>Compra:</strong> ${new Date(seat.purchaseDate).toLocaleString()}</span>
                `;
                seatsList.appendChild(seatItem);

                total += seat.price;
            });

            flightCard.appendChild(seatsList);

            const totalHeader = document.createElement('h3');
            totalHeader.innerHTML = `Total: ${formatterMX.format(total)}`;

            flightCard.appendChild(totalHeader)

            bookingsContainer.appendChild(flightCard);
        });
    }

    fetchBookings();
});