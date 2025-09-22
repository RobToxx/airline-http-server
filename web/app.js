const API_URL = 'http://10.60.0.220:8000';

// Seleccionamos los elementos del DOM
const searchForm = document.getElementById('search-form');
const originInput = document.getElementById('origin');
const destinationInput = document.getElementById('destination');
const fromDateInput = document.getElementById('fromDate');
const toDateInput = document.getElementById('toDate');
const resultsContainer = document.getElementById('results-container');
const statusDiv = document.getElementById('status');
const submitButton = document.getElementById('submit-button');
const spinner = document.getElementById('spinner');

// --- VARIABLES GLOBALES PARA MANEJAR EL ESTADO ---
let selectedSeats = [];
let currentFlightDetails = null;
let currentReservationId = null;
let countdownTimer = null;
let currentReservations = []; // Array para almacenar los resultados de las reservas exitosas

// --- EVENTO PRINCIPAL AL CARGAR LA PÁGINA ---
window.addEventListener('DOMContentLoaded', () => {
    const userStatusDiv = document.getElementById('user-status');
    const sessionId = localStorage.getItem('sessionId');
    if (sessionId) {
        userStatusDiv.innerHTML = `<p><a href="profile.html">Mis Vuelos</a> | <button id="logout-button" class="link-button">Cerrar Sesión</button></p>`;
        document.getElementById('logout-button').addEventListener('click', () => {
            localStorage.removeItem('sessionId');
            window.location.reload();
        });
    } else {
        userStatusDiv.innerHTML = `<a href="login.html"><button>Iniciar Sesión</button></a>`;
    }
    searchFlights();
});

// --- LÓGICA DE BÚSQUEDA ---
async function searchFlights() {
    resultsContainer.innerHTML = '';
    statusDiv.innerHTML = '';
    spinner.style.display = 'block';
    submitButton.disabled = true;

    const params = new URLSearchParams();

    if (originInput.value.trim()) {
        params.append("origin", originInput.value.trim());
    }
    if (destinationInput.value.trim()) {
        params.append("destination", destinationInput.value.trim());
    }
    if (fromDateInput.value.trim()) {
        params.append("fromDate", fromDateInput.value.trim());
    }
    if (toDateInput.value.trim()) {
        params.append("toDate", toDateInput.value.trim());
    }

    const searchUrl = `${API_URL}/flight/search?${params.toString()}`;

    console.log(searchUrl);
    
    try {
        const response = await fetch(searchUrl);
        if (!response.ok) throw new Error(`Error en la petición: ${response.statusText}`);
        const flights = await response.json();
        displayFlights(flights);
    } catch (error) {
        console.error('Hubo un error al buscar vuelos:', error);
        statusDiv.innerHTML = `<p style="color: red;">No se pudieron encontrar vuelos.</p>`;
    } finally {
        spinner.style.display = 'none';
        submitButton.disabled = false;
    }
}

searchForm.addEventListener('submit', (event) => {
    event.preventDefault();
    searchFlights();
});

// --- FUNCIONES DE VISUALIZACIÓN ---

function displayFlights(flights) {
    resultsContainer.innerHTML = '';
    if (flights.length === 0) {
        resultsContainer.innerHTML = '<p>No se encontraron vuelos para esta búsqueda.</p>';
        return;
    }
    flights.forEach(flight => {
        const flightCard = document.createElement('div');
        flightCard.className = 'flight-card';
        flightCard.innerHTML = `
            <p><strong>Origen:</strong> ${flight.origin} - <strong>Destino:</strong> ${flight.destination}</p>
            <p><strong>Salida:</strong> ${new Date(flight.departure).toLocaleString()}</p>
            <button onclick="getFlightDetails(${flight.id})">Ver Asientos</button>
        `;
        resultsContainer.appendChild(flightCard);
    });
}

async function getFlightDetails(flightId) {
    statusDiv.innerHTML = '';
    spinner.style.display = 'block';

    try {
        const response = await fetch(`${API_URL}/flight?id=${flightId}`, {
            // Esta opción obliga al navegador a no usar la caché
            cache: 'no-store' 
        });
        if (!response.ok) throw new Error('No se pudieron obtener los detalles del vuelo.');

        currentFlightDetails = await response.json();
        console.log('Datos recibidos del vuelo:', currentFlightDetails);

        searchForm.style.display = 'none';
        resultsContainer.innerHTML = '';

        const detailsDiv = document.createElement('div');
        let cabinHTML = '';

        const seatsByRow = {};
        currentFlightDetails.seats.forEach(seat => {
            const rowNumber = parseInt(seat.id.match(/\d+/)[0]);
            if (!seatsByRow[rowNumber]) seatsByRow[rowNumber] = [];
            seatsByRow[rowNumber].push(seat);
        });
        const sortedRowNumbers = Object.keys(seatsByRow).sort((a, b) => parseInt(a) - parseInt(b));
        let currentClass = null;
        sortedRowNumbers.forEach(rowNumber => {
            const rowSeats = seatsByRow[rowNumber].sort((a, b) => a.id.localeCompare(b.id));
            if (rowSeats.length > 0 && rowSeats[0].seatClass !== currentClass) {
                currentClass = rowSeats[0].seatClass;
                cabinHTML += `<h4>${currentClass === 'FIRST' ? 'Primera Clase' : 'Clase Turista'}</h4>`;
            }
            cabinHTML += '<div class="seat-row">';
            rowSeats.forEach(seat => {
                const isAvailable = seat.status === 'AVAILABLE';
                const clickHandler = isAvailable ? `onclick="selectSeat('${seat.id}', this)"` : '';
                cabinHTML += `<div class="seat ${isAvailable ? 'available' : 'occupied'}" ${isAvailable ? '' : `aria-disabled="true"`} ${clickHandler}>${seat.id}</div>`;
                if (currentClass === 'FIRST' && seat.id.endsWith('C') && rowSeats.length > 3) {
                    cabinHTML += '<div class="aisle-first-class"></div>';
                } else if (currentClass === 'ECONOMY' && seat.id.endsWith('C') && rowSeats.length > 3) {
                    cabinHTML += '<div class="aisle-economy-class"></div>';
                }
            });
            cabinHTML += '</div>';
        });

        detailsDiv.innerHTML = `
            <h2>Selecciona tu asiento</h2>
            <div class="details-container">
                <div class="airplane">
                    <div class="cockpit"></div><div class="cabin">${cabinHTML}</div><div class="tail"></div>
                </div>
                <div id="summary-panel" class="summary-panel"></div>
            </div>
            <div class="controls-area">
                <div class="form-group">
                    <label for="passenger-type">Tipo de Pasajero</label>
                    <select id="passenger-type">
                        <option value="ADULT">Adulto</option><option value="CHILD">Niño</option><option value="SENIOR">Adulto Mayor</option>
                    </select>
                </div>
                <div id="reservation-actions"></div>
                <button onclick="showSearchForm()">Volver a la Búsqueda</button>
            </div>
        `;
        resultsContainer.appendChild(detailsDiv);
        updateSummaryPanel();
    } catch (error) {
        statusDiv.innerHTML = `<p style="color: red;">${error.message}</p>`;
    } finally {
        spinner.style.display = 'none';
    }
}

function selectSeat(seatId, seatElement) {
    if (countdownTimer) return;

    const seatIndex = selectedSeats.findIndex(s => s.id === seatId);

    const seatData = currentFlightDetails.seats.find(s => s.id === seatId);

    if (seatIndex > -1) {
        selectedSeats.splice(seatIndex, 1);
        seatElement.classList.remove('selected');
    } else {
        selectedSeats.push({
            id: seatData.id,
            class: seatData.seatClass
        });
        seatElement.classList.add('selected');
    }

    const actionsDiv = document.getElementById('reservation-actions');
    if (actionsDiv) {
        if (selectedSeats.length > 0) {
            actionsDiv.innerHTML = `<button onclick="reservarAsiento(${currentFlightDetails.flight.id})">Apartar Asientos por 10 min</button>`;
        } else {
            actionsDiv.innerHTML = '';
        }
    }
}

function updateSummaryPanel() {
    const summaryPanel = document.getElementById('summary-panel');
    const actionsDiv = document.getElementById('reservation-actions');

    if (!actionsDiv) {
        return;
    }

    let subtotal = 0;
    let summaryHTML = '<h4>Resumen de Reserva</h4>';
    const seatsToShow = currentReservations;

    if (seatsToShow.length === 0) {
        summaryHTML += '<p class="summary-placeholder">Selecciona uno o más asientos en el mapa.</p>';
    } else {
        summaryHTML += '<ul>';
        seatsToShow.forEach(seat => {
            summaryHTML += `
                <li>
                    <span>Asiento: <b>${seat.seatId}</b> (${seat.seatClass})</span>
                    <span>${seat.passengerType}</span>
                    <span>$${seat.price.toFixed(2)}</span>
                </li>
            `;
            subtotal += seat.price;
        });
        summaryHTML += '</ul>';
        summaryHTML += `<hr><div class="subtotal"><span>Subtotal:</span><span>$${subtotal.toFixed(2)}</span></div>`;
    }

    summaryPanel.innerHTML = summaryHTML;
}

async function reservarAsiento(flightId) {
    const sessionId = localStorage.getItem('sessionId');
    if (!sessionId) {
        alert("Por favor, inicia sesión para poder reservar.");
        window.location.href = 'login.html';
        return;
    }
    if (selectedSeats.length === 0) {
        alert("Por favor, selecciona al menos un asiento.");
        return;
    }

    spinner.style.display = 'block';

    const successfulReservations = [];
    const passengerTypeSelect = document.getElementById('passenger-type');
    const passengerType = passengerTypeSelect ? passengerTypeSelect.value : 'ADULT';

    try {
        for (const seat of selectedSeats) {
            console.log(`Enviando reserva para el asiento: ${seat.id}`);
            const response = await fetch(`${API_URL}/seat/reserve`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    flightId: flightId,
                    seatId: seat.id,
                    sessionId: sessionId,
                    passengerType: passengerType
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `No se pudo reservar el asiento ${seat.id}. Es posible que ya no esté disponible.`);
            }

            const result = await response.json();
            successfulReservations.push(result);
        }

        currentReservations = successfulReservations;
        selectedSeats = [];

        const actionsDiv = document.getElementById('reservation-actions');

        if (currentReservations.length > 0) {
            currentReservationId = currentReservations[0].reservationId;
            actionsDiv.innerHTML = `
                <div class="countdown-timer">Tienes <span id="timer">10:00</span> para comprar</div>
                <button id="book-button" onclick="comprarAsiento('${currentReservationId}')">Comprar Ahora</button>
                <button class="link-button" onclick="cancelarReserva(true, ${flightId})">Cancelar Apartado</button>
            `;
            startCountdown(10 * 60, flightId);
        }
        updateSummaryPanel();
    } catch (error) {
        alert(error.message);
        getFlightDetails(flightId);
    } finally {
        spinner.style.display = 'none';
    }
}

async function comprarAsiento() {
    clearInterval(countdownTimer);
    spinner.style.display = 'block';

    try {
        const response = await fetch(`${API_URL}/seat/book`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reservationId: currentReservationId, sessionId: localStorage.getItem('sessionId') })
        });
        if (!response.ok) throw new Error('No se pudo completar la compra.');

        alert("¡Compra exitosa! Tu asiento está confirmado.");
        window.location.href = 'profile.html';

    } catch(error) {
        alert(error.message);
    } finally {
        spinner.style.display = 'none';
    }
}

async function cancelarReserva(showAlert, flightId) {
    clearInterval(countdownTimer);
    if(currentReservationId) {
        if (showAlert) {
            alert("Tu reserva ha sido cancelada.");
        } else {
            alert("¡Se acabó el tiempo! Tu asiento ha sido liberado.");
        }

        // Se envía la petición al servidor para liberar los asientos
        await fetch(`${API_URL}/seat/reserve`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reservationId: currentReservationId })
        });
    }

    // Se reinicia el estado local para que no haya conflictos
    selectedSeats = [];
    currentReservations = [];
    currentReservationId = null;
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
    
    // Y se recargan los datos del vuelo para actualizar el estado visual.
    // Con la opción 'no-store' en getFlightDetails, se asegura de que sea una nueva petición.
    getFlightDetails(flightId);
}

function startCountdown(duration, flightId) {
    let timer = duration, minutes, seconds;
    const display = document.getElementById('timer');
    if(!display) return;

    countdownTimer = setInterval(function () {
        minutes = parseInt(timer / 60, 10);
        seconds = parseInt(timer % 60, 10);
        minutes = minutes < 10 ? "0" + minutes : minutes;
        seconds = seconds < 10 ? "0" + seconds : seconds;
        display.textContent = minutes + ":" + seconds;

        if (--timer < 0) {
            clearInterval(countdownTimer);
            cancelarReserva(false, flightId);
        }
    }, 1000);
}

function showSearchForm() {
    document.getElementById('search-form').style.display = 'grid';
    resultsContainer.innerHTML = '';
    
    // Se limpia todo el estado de reserva al volver a la búsqueda
    selectedSeats = [];
    currentReservations = [];
    currentReservationId = null;
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
    
    searchFlights();
}