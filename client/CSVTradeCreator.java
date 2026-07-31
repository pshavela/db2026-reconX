import java.io.BufferedReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class CSVTradeCreator {

    // Change to your endpoint
    private static final String ENDPOINT =
            "http://localhost:8080/api/v1/trades";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java CSVTradeCreator <csv-file> <Bearer-token>");
            System.exit(1);
        }

        Path csvFile = Path.of(args[0]);
        String bearerToken = args[1];

        HttpClient client = HttpClient.newHttpClient();

        try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
            // Skip header
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] fields = line.split(",", -1);

                if (fields.length != 8) {
                    System.err.println("Skipping malformed row: " + line);
                    continue;
                }

                String tradeRef = fields[0];
                String instrumentId = fields[1];
                String counterpartyId = fields[2];
                String quantity = fields[3];
                String price = fields[4];
                String currency = fields[5];
                String side = fields[6];
                String tradeDate = fields[7];

                String json = """
                        {
                          "tradeRef": "%s",
                          "instrumentId": %s,
                          "counterpartyId": %s,
                          "assetClass": "EQUITY",
                          "side": "%s",
                          "quantity": %s,
                          "price": %s,
                          "tradeDate": "%s"
                        }
                        """.formatted(escape(tradeRef), instrumentId, counterpartyId, side, quantity, price, tradeDate);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ENDPOINT))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + bearerToken)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                try {
                    HttpResponse<String> response =
                            client.send(request, HttpResponse.BodyHandlers.ofString());

                    System.out.printf(
                            "trade_ref=%s status=%d%n",
                            tradeRef,
                            response.statusCode()
                    );

                } catch (Exception e) {
                    System.err.printf(
                            "trade_ref=%s request failed: %s%n",
                            tradeRef,
                            e.getMessage()
                    );
                }
            }
        }
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
