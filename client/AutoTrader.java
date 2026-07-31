import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AutoTrader {

    private static final String ENDPOINT = "http://localhost:8080/api/v1/trades";

    private static final List<Integer> VALID_INSTRUMENT_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    private static final List<Integer> VALID_COUNTERPARTY_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private static final List<String> VALID_SIDES = List.of("BUY", "SELL");
    private static final List<String> VALID_CURRENCIES = List.of("USD", "EUR", "GBP", "JPY", "CHF");
    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 6, 30);
    private static final Random RANDOM = new Random(2345235L);
    private static Integer TRADE_COUNTER = 0;
    private static final Set<String> TRADE_REFS = Set.of();
    private static final int SLEEP_DURATION_MS = 1000;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java AutoTrader <Bearer-token>");
            System.exit(1);
        }

        String bearerToken = args[0];
        HttpClient client = HttpClient.newHttpClient();

        while (true) {
            TRADE_COUNTER++;
            int instrumentId = VALID_INSTRUMENT_IDS.get(RANDOM.nextInt(VALID_INSTRUMENT_IDS.size()));
            int counterpartyId = VALID_COUNTERPARTY_IDS.get(RANDOM.nextInt(VALID_COUNTERPARTY_IDS.size()));

            String side = VALID_SIDES.get(RANDOM.nextInt(VALID_SIDES.size()));
            String currency = VALID_CURRENCIES.get(RANDOM.nextInt(VALID_CURRENCIES.size()));
            double quantity =  1.0 + 100.0* RANDOM.nextDouble();
            double price = 1.0 + 1000.0 * RANDOM.nextDouble();
            String tradeRef = generateTradeRef();
            String tradeDate = generateTradeDate();

            String json = """
                    {
                        "tradeRef": "%s",
                        "instrumentId": %d,
                        "counterpartyId": %d,
                        "assetClass": "EQUITY",
                        "side": "%s",
                        "quantity": %s,
                        "price": %s,
                        "currency": "%s",
                        "tradeDate": "%s"
                    }
                    """.formatted(tradeRef, instrumentId, counterpartyId, side, Double.toString(quantity), Double.toString(price), currency, tradeDate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + bearerToken)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.printf("trade_ref=%s status=%d%n", tradeRef, response.statusCode());
            } catch (Exception e) {
                System.err.printf("trade_ref=%s request failed: %s%n", tradeRef, e.getMessage());
            }

            int delayMs = RANDOM.nextInt(SLEEP_DURATION_MS) + 1;
            Thread.sleep(delayMs);
        }
    }

    private static String generateTradeRef() {
        String tradeRef;
        do {
            tradeRef = "TRD-" + combination(8) + "-" + combination(4);
        } while (TRADE_REFS.contains(tradeRef));
        return tradeRef;
    }

    private static String combination(int length) {
        String s = "";
        for (int i = 0; i < length; i++)
            s += RANDOM.nextInt(10);
        return s;
    }

    private static String generateTradeDate() {
        long totalDays = ChronoUnit.DAYS.between(START_DATE, END_DATE) + 1;
        long offsetDays = RANDOM.nextLong(totalDays);
        return START_DATE.plusDays(offsetDays).toString();
    }
}
