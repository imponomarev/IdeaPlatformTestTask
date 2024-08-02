package main.logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.Ticket;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;



public class FlightAnalyst {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Запуск: java -jar IdeaPlaatformTestTask.jar <path to tickets.json>");
            System.exit(1);
        }

        String filePath = args[0];
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);
            Type ticketListType = new TypeToken<ArrayList<Ticket>>() {}.getType();
            List<Ticket> tickets = gson.fromJson(jsonObject.get("tickets"), ticketListType);

            analyzeTickets(tickets);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void analyzeTickets(List<Ticket> tickets) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter1 = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter timeFormatter2 = DateTimeFormatter.ofPattern("HH:mm");

        ZoneId vladivostokZone = ZoneId.of("Asia/Vladivostok");
        ZoneId telAvivZone = ZoneId.of("Asia/Jerusalem");

        Map<String, Long> minFlightTimes = new HashMap<>();
        List<Integer> prices = new ArrayList<>();

        for (Ticket ticket : tickets) {
            if ("VVO".equals(ticket.origin()) && "TLV".equals(ticket.destination())) {
                try {
                    LocalDate departureDate = LocalDate.parse(ticket.departure_date(), dateFormatter);
                    LocalTime departureTime = parseTime(ticket.departure_time(), timeFormatter1, timeFormatter2);
                    ZonedDateTime departureDateTime = ZonedDateTime.of(departureDate, departureTime, vladivostokZone);

                    LocalDate arrivalDate = LocalDate.parse(ticket.arrival_date(), dateFormatter);
                    LocalTime arrivalTime = parseTime(ticket.arrival_time(), timeFormatter1, timeFormatter2);
                    ZonedDateTime arrivalDateTime = ZonedDateTime.of(arrivalDate, arrivalTime, telAvivZone);

                    long flightTime = Duration.between(departureDateTime, arrivalDateTime).toMinutes();

                    minFlightTimes.put(ticket.carrier(), Math.min(minFlightTimes.getOrDefault(ticket.carrier(), Long.MAX_VALUE), flightTime));

                    prices.add(ticket.price());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        System.out.println("Минимальное время полета авиаперевозчиков:");
        for (Map.Entry<String, Long> entry : minFlightTimes.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " минут");
        }

        double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);

        Collections.sort(prices);

        double medianPrice = prices.size() % 2 == 0 ?
                (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2.0 :
                prices.get(prices.size() / 2);

        System.out.println("Средняя цена: " + averagePrice);
        System.out.println("Медиана: " + medianPrice);
        System.out.println("Разница: " + (averagePrice - medianPrice));
    }

    private static LocalTime parseTime(String time, DateTimeFormatter formatter1, DateTimeFormatter formatter2) {
        try {
            return LocalTime.parse(time, formatter1);
        } catch (DateTimeParseException e) {
            return LocalTime.parse(time, formatter2);
        }
    }

}
