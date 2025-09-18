package model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import util.Result;

public record FlightFilter (
    Optional<String> origin, 
    Optional<String> destination,
    Optional<LocalDateTime> fromDate, 
    Optional<LocalDateTime> toDate
) {

    public static FlightFilter fromQuery(Map<String, String> queryParams) {

        return new FlightFilter(
                Optional.ofNullable(queryParams.get("origin")),
                Optional.ofNullable(queryParams.get("destination")),
                Result.of(() -> LocalDateTime.parse(queryParams.get("fromDate"))).asOptional(),
                Result.of(() -> LocalDateTime.parse(queryParams.get("toDate"))).asOptional()
        );
    }
}