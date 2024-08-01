package main.logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.Ticket;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        Map<String, Long> minFlightTimes = new HashMap<>();
        List<Integer> prices = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        for (Ticket ticket : tickets) {
            if ("VVO".equals(ticket.origin()) && "TLV".equals(ticket.destination())) {
                try {
                    long departureTime = sdf.parse(ticket.departure_time()).getTime();
                    long arrivalTime = sdf.parse(ticket.arrival_time()).getTime();
                    long flightTime = (arrivalTime - departureTime) / (1000 * 60);

                    minFlightTimes.put(ticket.carrier(), Math.min(minFlightTimes.getOrDefault(ticket.carrier(), Long.MAX_VALUE), flightTime));
                    prices.add(ticket.price());
                } catch (ParseException e) {
                    e.printStackTrace();
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
}
