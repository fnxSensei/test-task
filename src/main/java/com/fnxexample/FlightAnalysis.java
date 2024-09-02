package com.fnxexample;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightAnalysis {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    static class Ticket {
        public String origin;
        public String origin_name;
        public String destination;
        public String destination_name;
        public String departure_date;
        public String departure_time;
        public String arrival_date;
        public String arrival_time;
        public String carrier;
        public int stops;
        public int price;
    }

    static class Tickets {
        public List<Ticket> tickets;
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Tickets tickets = mapper.readValue(new File("tickets.json"), Tickets.class);

        // Фильтрация рейсов Владивосток-Тель-Авив
        List<Ticket> vvoToTlvFlights = tickets.tickets.stream()
                .filter(t -> t.origin.equals("VVO") && t.destination.equals("TLV"))
                .toList();

        // Минимальное время полета для каждого перевозчика
        Map<String, Long> minFlightTimes = new HashMap<>();
        for (Ticket ticket : vvoToTlvFlights) {
            long flightTime = calculateFlightTime(ticket.departure_date, ticket.departure_time, ticket.arrival_date, ticket.arrival_time);
            minFlightTimes.put(ticket.carrier, Math.min(minFlightTimes.getOrDefault(ticket.carrier, Long.MAX_VALUE), flightTime));
        }

        System.out.println("Минимальное время полета между Владивостоком и Тель-Авивом для каждого перевозчика:");
        minFlightTimes.forEach((carrier, time) -> System.out.println(carrier + ": " + time + " минут"));

        // Расчет средней цены
        double averagePrice = vvoToTlvFlights.stream().mapToInt(t -> t.price).average().orElse(0);

        // Расчет медианы цены
        List<Integer> prices = vvoToTlvFlights.stream().map(t -> t.price).sorted().toList();
        double medianPrice = prices.size() % 2 == 0 ?
                (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2.0 :
                prices.get(prices.size() / 2);

        System.out.println("Разница между средней ценой и медианой: " + (averagePrice - medianPrice));
    }

    private static long calculateFlightTime(String departureDate, String departureTime, String arrivalDate, String arrivalTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
        LocalDateTime departure = LocalDateTime.parse(departureDate + " " + departureTime, formatter);
        LocalDateTime arrival = LocalDateTime.parse(arrivalDate + " " + arrivalTime, formatter);
        return Duration.between(departure, arrival).toMinutes();
    }
}
