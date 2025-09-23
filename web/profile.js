const API_URL = 'http://187.132.146.115:8000';

// Se ejecuta cuando la página de perfil termina de cargar
window.addEventListener('DOMContentLoaded', () => {
    const spinner = document.getElementById('spinner');
    const reservationsContainer = document.getElementById('reservations-container');
    const sessionId = localStorage.getItem('sessionId');

    // 1. Verificación: Si no hay sesión, no debería estar aquí. Lo mandamos a login.
    if (!sessionId) {
        alert("Necesitas iniciar sesión para ver tus vuelos.");
        window.location.href = 'login.html';
        return;
    }

    // En tu profile.js, reemplaza únicamente la función fetchReservations con esta:

async function fetchReservations() {
    spinner.style.display = 'block';
    try {
        // 1. Pedimos la lista inicial de reservaciones
        const reservationsResponse = await fetch(`${API_URL}/user/reservations?sessionId=${sessionId}`);
        if (!reservationsResponse.ok) throw new Error('No se pudieron cargar tus reservaciones.');
        
        const reservations = await reservationsResponse.json();

        // Si no hay reservaciones, lo indicamos y terminamos.
        if (reservations.length === 0) {
            displayReservations([]);
            return;
        }

        // 2. Por cada reservación, creamos una petición para buscar los detalles de su vuelo
        const flightDetailPromises = reservations.map(res => {
            // ¡ATENCIÓN AQUÍ! Asegúrate de que el nombre de la propiedad ('flightid' o 'flightId')
            // coincida EXACTAMENTE con el JSON que envía el backend. JavaScript distingue mayúsculas.
            return fetch(`${API_URL}/flight?id=${res.flightid}`);
        });

        // 3. Ejecutamos todas las peticiones de detalles al mismo tiempo
        const flightDetailResponses = await Promise.all(flightDetailPromises);

        // 4. Convertimos todas las respuestas a JSON
        const flightDetails = await Promise.all(
            flightDetailResponses.map(res => {
                if (!res.ok) throw new Error('Fallo al obtener el detalle de un vuelo.');
                return res.json();
            })
        );

        // 5. Unimos la información: a cada reservación le asignamos los detalles de su vuelo
        const fullReservationDetails = reservations.map((res, index) => {
            return {
                // ¡ATENCIÓN AQUÍ! Asegúrate de que el nombre del ID de la reserva sea 'id' o 'reservationId'
                reservationId: res.id, 
                seatId: res.seatId,
                flight: flightDetails[index].flight // El objeto de vuelo completo
            };
        });

        // 6. Enviamos los datos ya combinados a la función que los muestra en pantalla
        displayReservations(fullReservationDetails);

    } catch (error) {
        console.error("Error detallado:", error);
        reservationsContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
    } finally {
        spinner.style.display = 'none';
    }
}

    // 3. Función para mostrar las reservaciones en el HTML
    function displayReservations(reservations) {
        if (reservations.length === 0) {
            reservationsContainer.innerHTML = '<p>Aún no tienes vuelos reservados.</p>';
            return;
        }

        reservations.forEach(res => {
            const flightCard = document.createElement('div');
            flightCard.className = 'flight-card'; // Reutilizamos la clase de estilo
            flightCard.innerHTML = `
                <p><strong>Vuelo:</strong> ${res.flight.origin} a ${res.flight.destination}</p>
                <p><strong>Aerolínea:</strong> ${res.flight.airline}</p>
                <p><strong>Salida:</strong> ${new Date(res.flight.departure).toLocaleString()}</p>
                <p><strong>Asiento:</strong> ${res.seatId}</p>
                <p><strong>ID de Reserva:</strong> ${res.reservationId}</p>
            `;
            reservationsContainer.appendChild(flightCard);
        });
    }

    // 4. ¡Llamamos a la función para que todo empiece!
    fetchReservations();
});