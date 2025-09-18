package generators;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Flight;

public class FlightsGenerator {

	private static final String[] CITIES = {
		// México (10)
		"CDMX", "Guadalajara", "Monterrey", "Cancún", "Tijuana", 
		"Mérida", "León", "Chihuahua", "Oaxaca", "Puebla",

		// Estados Unidos (10)
		"Los Angeles", "New York", "Miami", "Houston", "Chicago", 
		"San Francisco", "Dallas", "Las Vegas", "Atlanta", "Seattle",

		// Canadá (5)
		"Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa",

		// Centroamérica y Caribe (8)
		"Ciudad de Panama", "San José", "San Salvador", "Ciudad de Guatemala", 
		"La Habana", "Santo Domingo", "Punta Cana", "Kingston",

		// Sudamérica (12)
		"Bogotá", "Medellín", "Lima", "Cusco", "Santiago", 
		"Valparaíso", "Buenos Aires", "Córdoba", 
		"Rio de Janeiro", "São Paulo", "Brasilia", "Montevideo",

		// Extra en Latam (5)
		"La Paz", "Santa Cruz", "Quito", "Guayaquil", "Asunción"
	};

	public static List<Flight> generate(int count, List<Airplane> airplanes) {

		List<Flight> flights = new ArrayList<>(count);

		Random rnd = new Random();

		for (int i = 0; i < count; i++) {

			String origin = CITIES[rnd.nextInt(CITIES.length)];
			String destination;

			do {
				destination = CITIES[rnd.nextInt(CITIES.length)];

			} while (destination.equals(origin));

			flights.add(
				new Flight(
					i+1, 
					origin, 
					destination, 
					LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusMinutes(rnd.nextInt(2880)*15), 
					airplanes.get(
						rnd.nextInt(airplanes.size())
					).id()
				)
			);
		}

		return flights;
	}
}