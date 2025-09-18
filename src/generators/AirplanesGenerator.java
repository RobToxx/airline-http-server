package generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AirplanesGenerator {

    private static final String[] MODELS = {
        "A320", "B737", "A321", "B747", "A330", "B777", "D167", "C821", "C189", "E190", "A350"
    };

    public static List<Airplane> generate(int count) {

        List<Airplane> airplanes = new ArrayList<>(count);

        Random rnd = new Random();

        for (int i = 0; i < count; i++) {

            airplanes.add(
                new Airplane(
                    i+1, 
                    MODELS[rnd.nextInt(MODELS.length)]
                )
            );
        }
        return airplanes;
    }
}
