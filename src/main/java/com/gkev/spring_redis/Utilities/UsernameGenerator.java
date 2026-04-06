package com.gkev.spring_redis.Utilities;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;

@Component
public class UsernameGenerator {

    private static final Random RANDOM = new Random();
    private static final String ALPHANUM = "abcdefghijklmnopqrstuvwxyz0123456789";


    private static final int MIN_SUBSTRINGS = 2;
    private static final int MAX_SUBSTRINGS = 3;
    private static final int TIMESTAMP_DIGITS = 4;


    public static Flux<String> generateSuggestions(String firstName, String surname, String lastName, int count) {
        List<String> nameParts = prepareNameParts(firstName, surname, lastName);
        Set<String> usedSuggestions = new HashSet<>();

        return Flux.generate(() -> 0, (state, sink) -> {
            if (state >= count) {
                sink.complete();
            } else {
                String candidate;
                if (nameParts.isEmpty()) {
                    candidate = generateRandomUsername();
                } else {
                    candidate = generateCandidate(nameParts);
                }

                while (usedSuggestions.contains(candidate)) {
                    candidate = nameParts.isEmpty() ? generateRandomUsername() : generateCandidate(nameParts);
                }

                usedSuggestions.add(candidate);
                sink.next(candidate);
            }
            return state + 1;
        });
    }

    private static List<String> prepareNameParts(String... names) {
        List<String> nameParts = new ArrayList<>();
        for (String name : names) {
            if (name != null && !name.trim().isEmpty()) {
                nameParts.add(name.trim().toLowerCase());
            }
        }
        return nameParts;
    }


    private static String generateCandidate(List<String> nameParts) {
        int numParts = MIN_SUBSTRINGS + RANDOM.nextInt(MAX_SUBSTRINGS - MIN_SUBSTRINGS + 1);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < numParts && !nameParts.isEmpty(); i++) {
            String part = nameParts.get(RANDOM.nextInt(nameParts.size()));
            int len = 1 + RANDOM.nextInt(Math.min(part.length(), 3)); // take 1-3 chars
            sb.append(part.substring(0, len));
        }


        if (RANDOM.nextBoolean()) {
            int number = RANDOM.nextInt((int) Math.pow(10, TIMESTAMP_DIGITS));
            sb.append(number);
        }

        return sb.toString();
    }


    private static String generateRandomUsername() {
        StringBuilder sb = new StringBuilder();
        int length = 6 + RANDOM.nextInt(5);
        for (int j = 0; j < length; j++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }


}

