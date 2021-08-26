import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import com.sun.net.httpserver.*;

public class Server {
    // Port number used to connect to this server
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));
    // JSON endpoint structure
    private static final String QUERY_TEMPLATE = "{\"items\":[%s],\"votes\":\"%s\"}";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", (HttpExchange t) -> {
            String html = Files.readString(Paths.get("index.html"));
            send(t, "text/html; charset=utf-8", html);
        });
        server.createContext("/query", (HttpExchange t) -> {
            String year = parse("year", t.getRequestURI().getQuery().split("&"));
            File file = new File("data/" + year + ".csv");
            if (!file.isFile()) {
                send(t, "application/json", String.format(QUERY_TEMPLATE, "", ""));
                return;
            }
            List<State> states = new ArrayList<>(51);
            int totalVotes = 0;
            try (Scanner input = new Scanner(file)) {
                while (input.hasNextLine()) {
                    State state = State.fromCsv(input.nextLine());
                    states.add(state);
                    totalVotes += state.popularVotes;
                }
            }
            Set<State> result = new ElectionSimulator(states).simulate();
            int minVotes = ElectionSimulator.minPopularVotes(result);
            double percent = (minVotes / (double) totalVotes) * 100;
            String votes = String.format("%.1f%% of votes", percent);
            send(t, "application/json", String.format(QUERY_TEMPLATE, json(result), votes));
        });
        server.setExecutor(null);
        server.start();
    }

    private static String parse(String key, String... params) {
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }
        return "";
    }

    private static void send(HttpExchange t, String contentType, String data)
            throws IOException, UnsupportedEncodingException {
        t.getResponseHeaders().set("Content-Type", contentType);
        byte[] response = data.getBytes("UTF-8");
        t.sendResponseHeaders(200, response.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response);
        }
    }

    private static String json(Iterable<State> matches) {
        StringBuilder results = new StringBuilder();
        for (State s : matches) {
            if (results.length() > 0) {
                results.append(',');
            }
            results.append('"').append(s.abbr).append('"');
        }
        return results.toString();
    }
}
