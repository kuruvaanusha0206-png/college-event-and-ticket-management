package com.univibe.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.univibe.dao.BookingDAO;
import com.univibe.dao.EventDAO;
import com.univibe.dao.PaymentDAO;
import com.univibe.dao.UserDAO;
import com.univibe.database.DatabaseInitializer;
import com.univibe.model.*;
import com.univibe.service.BookingManager;
import com.univibe.service.EventManager;
import com.univibe.service.ReportGenerator;
import com.univibe.util.FileHandler;
import com.univibe.util.QRMatrixGenerator;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Built-in Core Java Web Server for UniVibe.
 * Demonstrates:
 *  1. Zero external web frameworks (built strictly using standard JDK com.sun.net.httpserver)
 *  2. RESTful JSON/Form API endpoints integrated with existing DAO and Service layers
 *  3. Full modern Single-Page Web Application interface accessible via any web browser
 *  4. Concurrent request handling with ThreadPoolExecutor
 */
public class UniVibeWebServer {

    public static final int DEFAULT_PORT = 8080;
    private final int port;
    private HttpServer server;

    private final EventManager eventManager;
    private final BookingManager bookingManager;
    private final EventDAO eventDAO;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private final PaymentDAO paymentDAO;

    public UniVibeWebServer(int port) {
        this.port = port;
        this.eventManager = new EventManager();
        this.bookingManager = new BookingManager();
        this.eventDAO = new EventDAO();
        this.bookingDAO = new BookingDAO();
        this.userDAO = new UserDAO();
        this.paymentDAO = new PaymentDAO();
    }

    public void start() throws IOException {
        DatabaseInitializer.initializeDatabase();

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8)); // Multithreaded request handling

        // Register Web & API Handlers
        server.createContext("/", new MainWebHandler());
        server.createContext("/api/events", new EventsApiHandler());
        server.createContext("/api/book", new BookingApiHandler());
        server.createContext("/api/cancel", new CancelApiHandler());
        server.createContext("/api/events/create", new CreateEventApiHandler());
        server.createContext("/api/events/approve", new ApproveEventApiHandler());
        server.createContext("/api/events/reject", new RejectEventApiHandler());
        server.createContext("/api/attendance", new AttendanceApiHandler());
        server.createContext("/api/feedback", new FeedbackApiHandler());
        server.createContext("/api/stats", new StatsApiHandler());
        server.createContext("/api/download/ticket", new DownloadTicketHandler());
        server.createContext("/api/download/report", new DownloadReportHandler());

        server.start();
        System.out.println("======================================================================");
        System.out.println("  UNIVIBE LOCAL WEB SERVER IS LIVE!");
        System.out.println("  Access the interactive web portal at: http://localhost:" + port);
        System.out.println("======================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[UniVibeWebServer] Stopped.");
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            UniVibeWebServer webServer = new UniVibeWebServer(port);
            webServer.start();

            // Keep main thread alive
            Runtime.getRuntime().addShutdownHook(new Thread(webServer::stop));
        } catch (Exception e) {
            System.err.println("[UniVibeWebServer] Failed to start on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Web Page Handler: Serves the Complete Responsive Web Application
    // ------------------------------------------------------------------------
    private class MainWebHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String html = generateWebPageHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: List Events (JSON)
    // ------------------------------------------------------------------------
    private class EventsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Event> events = eventDAO.getAllEvents();
                StringBuilder json = new StringBuilder("[");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (int i = 0; i < events.size(); i++) {
                    Event e = events.get(i);
                    json.append("{")
                            .append("\"id\":").append(e.getEventId()).append(",")
                            .append("\"title\":\"").append(escapeJson(e.getTitle())).append("\",")
                            .append("\"description\":\"").append(escapeJson(e.getDescription())).append("\",")
                            .append("\"category\":\"").append(escapeJson(e.getCategory())).append("\",")
                            .append("\"venue\":\"").append(escapeJson(e.getVenue())).append("\",")
                            .append("\"date\":\"").append(sdf.format(e.getEventDate())).append("\",")
                            .append("\"time\":\"").append(escapeJson(e.getEventTime())).append("\",")
                            .append("\"price\":").append(e.getTicketPrice()).append(",")
                            .append("\"totalSeats\":").append(e.getTotalSeats()).append(",")
                            .append("\"availableSeats\":").append(e.getAvailableSeats()).append(",")
                            .append("\"status\":\"").append(e.getStatus()).append("\",")
                            .append("\"isLowSeat\":").append(e.isLowSeatAvailability()).append(",")
                            .append("\"avgRating\":").append(e.getAverageRating()).append(",")
                            .append("\"feedbackCount\":").append(e.getFeedbackCount()).append(",")
                            .append("\"organizerName\":\"").append(escapeJson(e.getOrganizerName() != null ? e.getOrganizerName() : "Faculty")).append("\"")
                            .append("}");
                    if (i < events.size() - 1) json.append(",");
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception ex) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Book Tickets (POST)
    // ------------------------------------------------------------------------
    private class BookingApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseFormBody(exchange);
            try {
                int studentId = Integer.parseInt(params.getOrDefault("studentId", "1"));
                int eventId = Integer.parseInt(params.get("eventId"));
                int seats = Integer.parseInt(params.getOrDefault("seats", "1"));
                String paymentMethod = params.getOrDefault("paymentMethod", "UPI");

                Student student = getStudentById(studentId);
                Event event = eventManager.getEventById(eventId);

                Booking booking = bookingManager.bookTickets(student, event, seats, paymentMethod);
                Ticket t = booking.getTickets().get(0);

                // Generate ASCII QR representation
                boolean[][] matrix = QRMatrixGenerator.generateMatrix(t.getTicketCode() + "#" + t.getQrHash());
                String asciiQR = QRMatrixGenerator.toAsciiArt(matrix);

                StringBuilder res = new StringBuilder();
                res.append("{")
                        .append("\"success\":true,")
                        .append("\"bookingRef\":\"").append(booking.getBookingReference()).append("\",")
                        .append("\"ticketCode\":\"").append(t.getTicketCode()).append("\",")
                        .append("\"seatNumber\":\"").append(t.getSeatNumber()).append("\",")
                        .append("\"qrHash\":\"").append(t.getQrHash()).append("\",")
                        .append("\"eventTitle\":\"").append(escapeJson(event.getTitle())).append("\",")
                        .append("\"venue\":\"").append(escapeJson(event.getVenue())).append("\",")
                        .append("\"studentName\":\"").append(escapeJson(student.getFullName())).append("\",")
                        .append("\"rollNumber\":\"").append(escapeJson(student.getRollNumber())).append("\",")
                        .append("\"totalAmount\":").append(booking.getTotalAmount()).append(",")
                        .append("\"seats\":").append(seats).append(",")
                        .append("\"asciiQR\":\"").append(escapeJson(asciiQR)).append("\"")
                        .append("}");

                sendJsonResponse(exchange, 200, res.toString());
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Cancel Booking (POST)
    // ------------------------------------------------------------------------
    private class CancelApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseFormBody(exchange);
            try {
                int bookingId = Integer.parseInt(params.get("bookingId"));
                boolean ok = bookingManager.cancelBooking(bookingId, "WEB_STUDENT");
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Create Event (POST)
    // ------------------------------------------------------------------------
    private class CreateEventApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"POST required\"}");
                return;
            }

            Map<String, String> params = parseFormBody(exchange);
            try {
                Event e = new Event();
                e.setTitle(params.get("title"));
                e.setDescription(params.getOrDefault("description", "Campus Event"));
                e.setCategory(params.getOrDefault("category", "Technical Events"));
                e.setVenue(params.get("venue"));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                e.setEventDate(sdf.parse(params.get("date")));
                e.setEventTime(params.getOrDefault("time", "10:00 AM"));
                e.setTicketPrice(Double.parseDouble(params.getOrDefault("price", "0.0")));
                int seats = Integer.parseInt(params.getOrDefault("seats", "100"));
                e.setTotalSeats(seats);
                e.setAvailableSeats(seats);
                e.setOrganizerId(Integer.parseInt(params.getOrDefault("organizerId", "1")));
                e.setStatus("APPROVED");

                boolean ok = eventManager.submitNewEvent(e);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Approve / Reject Event (POST)
    // ------------------------------------------------------------------------
    private class ApproveEventApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseFormBody(exchange);
            try {
                int id = Integer.parseInt(params.get("id"));
                boolean ok = eventManager.approveEvent(id);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    private class RejectEventApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseFormBody(exchange);
            try {
                int id = Integer.parseInt(params.get("id"));
                boolean ok = eventManager.rejectEvent(id);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Attendance Status (POST)
    // ------------------------------------------------------------------------
    private class AttendanceApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseFormBody(exchange);
            try {
                int ticketId = Integer.parseInt(params.get("ticketId"));
                String status = params.get("status");
                boolean ok = bookingManager.updateAttendance(ticketId, status, "WEB_ORGANIZER");
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Submit Feedback (POST)
    // ------------------------------------------------------------------------
    private class FeedbackApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseFormBody(exchange);
            try {
                Feedback fb = new Feedback();
                fb.setStudentId(Integer.parseInt(params.getOrDefault("studentId", "1")));
                fb.setEventId(Integer.parseInt(params.get("eventId")));
                fb.setRating(Integer.parseInt(params.get("rating")));
                fb.setComment(params.get("comment"));

                boolean ok = eventManager.submitFeedback(fb);
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + "}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Dashboard Stats (GET)
    // ------------------------------------------------------------------------
    private class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Student> students = userDAO.getAllStudents();
                List<Event> events = eventDAO.getAllEvents();
                List<Booking> bookings = bookingDAO.getAllBookings();
                double revenue = paymentDAO.getTotalNetRevenue();

                long confirmed = bookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count();
                long cancelled = bookings.stream().filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus())).count();

                StringBuilder sb = new StringBuilder();
                sb.append("{")
                        .append("\"totalStudents\":").append(students.size()).append(",")
                        .append("\"totalEvents\":").append(events.size()).append(",")
                        .append("\"confirmedBookings\":").append(confirmed).append(",")
                        .append("\"cancelledBookings\":").append(cancelled).append(",")
                        .append("\"netRevenue\":").append(revenue)
                        .append("}");

                sendJsonResponse(exchange, 200, sb.toString());
            } catch (Exception ex) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Download Generated Ticket File
    // ------------------------------------------------------------------------
    private class DownloadTicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String code = "TKT-SAMPLE";
            if (query != null && query.contains("code=")) {
                code = query.substring(query.indexOf("code=") + 5).split("&")[0];
            }

            String safeName = code.replaceAll("[^a-zA-Z0-9_-]", "_") + ".txt";
            File file = new File("tickets", safeName);
            if (!file.exists()) {
                // If not found, write one dynamically
                Ticket t = new Ticket(1, 1, code, "A-01", "UV#" + code, "PENDING", new Date());
                t.setEventTitle("UniVibe Campus Event");
                t.setStudentName("Alex Mercer");
                t.setRollNumber("CS2024-001");
                file = FileHandler.saveTicketToFile(t);
            }

            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + safeName + "\"");
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // ------------------------------------------------------------------------
    // API: Generate & Download Report
    // ------------------------------------------------------------------------
    private class DownloadReportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                final File[] resFile = new File[1];
                final Object lock = new Object();

                ReportGenerator generator = new ReportGenerator(ReportGenerator.ReportType.SYSTEM_SUMMARY, new ReportGenerator.ReportProgressListener() {
                    @Override
                    public void onProgress(int percentage, String message) {}

                    @Override
                    public void onComplete(File generatedFile) {
                        synchronized (lock) {
                            resFile[0] = generatedFile;
                            lock.notifyAll();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });

                new Thread(generator).start();

                synchronized (lock) {
                    lock.wait(5000);
                }

                if (resFile[0] != null && resFile[0].exists()) {
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + resFile[0].getName() + "\"");
                    byte[] bytes = java.nio.file.Files.readAllBytes(resFile[0].toPath());
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    sendResponse(exchange, 500, "Report generation timed out.");
                }
            } catch (Exception ex) {
                sendResponse(exchange, 500, "Error generating report: " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------
    private Student getStudentById(int id) {
        try {
            List<Student> list = userDAO.getAllStudents();
            for (Student s : list) {
                if (s.getStudentId() == id || s.getUserId() == id) return s;
            }
        } catch (Exception ignored) {}
        Student s = new Student();
        s.setStudentId(1);
        s.setUserId(4);
        s.setFullName("Alex Mercer");
        s.setRollNumber("CS2024-001");
        s.setDepartment("Computer Science");
        s.setSemester(5);
        return s;
    }

    private Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            String body = baos.toString(StandardCharsets.UTF_8);
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length >= 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                } else if (kv.length == 1) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
                }
            }
        }
        return map;
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    // ------------------------------------------------------------------------
    // HTML5 / Modern Responsive Single-Page UI
    // ------------------------------------------------------------------------
    private String generateWebPageHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>UniVibe – College Event & Ticket Management System</title>\n" +
                "  <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "  <link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap\" rel=\"stylesheet\">\n" +
                "  <style>\n" +
                "    :root {\n" +
                "      --primary: #4338ca; --primary-hover: #3730a3; --primary-light: #e0e7ff; --bg: #f8fafc;\n" +
                "      --surface: #ffffff; --text: #0f172a; --text-muted: #64748b; --border: #e2e8f0;\n" +
                "      --success: #10b981; --warning: #f59e0b; --danger: #ef4444; --card-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.05);\n" +
                "    }\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }\n" +
                "    body { background-color: var(--bg); color: var(--text); min-height: 100vh; display: flex; flex-direction: column; }\n" +
                "    header { background: #1e1b4b; color: white; padding: 14px 32px; display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #312e81; position: sticky; top: 0; z-index: 100; }\n" +
                "    .logo-container { display: flex; align-items: center; gap: 12px; }\n" +
                "    .logo-badge { background: #4f46e5; color: white; font-weight: 800; font-size: 18px; padding: 6px 12px; border-radius: 8px; letter-spacing: 1px; }\n" +
                "    .logo-title { font-size: 20px; font-weight: 700; }\n" +
                "    .logo-sub { font-size: 12px; color: #a5b4fc; }\n" +
                "    nav { display: flex; gap: 8px; }\n" +
                "    .nav-btn { background: transparent; border: 1px solid transparent; color: #c7d2fe; padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 14px; cursor: pointer; transition: all 0.2s; }\n" +
                "    .nav-btn:hover { background: rgba(255,255,255,0.08); color: white; }\n" +
                "    .nav-btn.active { background: #4338ca; color: white; border-color: #6366f1; }\n" +
                "    .user-pill { background: rgba(255,255,255,0.12); padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 8px; }\n" +
                "    .user-dot { width: 8px; height: 8px; background: #34d399; border-radius: 50%; }\n" +
                "    main { max-width: 1280px; width: 100%; margin: 24px auto; padding: 0 24px; flex: 1; }\n" +
                "    .kpi-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }\n" +
                "    .kpi-card { background: var(--surface); padding: 18px 20px; border-radius: 12px; border: 1px solid var(--border); box-shadow: var(--card-shadow); }\n" +
                "    .kpi-label { font-size: 12px; font-weight: 700; text-transform: uppercase; color: var(--text-muted); }\n" +
                "    .kpi-val { font-size: 26px; font-weight: 800; color: var(--text); margin: 6px 0 2px 0; }\n" +
                "    .kpi-sub { font-size: 12px; color: var(--primary); font-weight: 600; }\n" +
                "    .filter-bar { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 16px 20px; margin-bottom: 24px; display: flex; flex-wrap: wrap; gap: 16px; align-items: center; justify-content: space-between; }\n" +
                "    .search-input { padding: 10px 16px; border: 1px solid var(--border); border-radius: 8px; font-size: 14px; width: 280px; }\n" +
                "    .category-pills { display: flex; flex-wrap: wrap; gap: 8px; }\n" +
                "    .pill { padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; border: 1px solid var(--border); background: var(--bg); cursor: pointer; transition: 0.15s; }\n" +
                "    .pill:hover { background: #e2e8f0; }\n" +
                "    .pill.active { background: var(--primary); color: white; border-color: var(--primary); }\n" +
                "    .events-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }\n" +
                "    .event-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; overflow: hidden; box-shadow: var(--card-shadow); display: flex; flex-direction: column; transition: transform 0.2s, box-shadow 0.2s; }\n" +
                "    .event-card:hover { transform: translateY(-3px); box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1); }\n" +
                "    .card-top { padding: 20px; flex: 1; }\n" +
                "    .card-badges { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }\n" +
                "    .cat-badge { background: var(--primary-light); color: var(--primary); font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 6px; text-transform: uppercase; }\n" +
                "    .low-seat-badge { background: #fef3c7; color: #b45309; font-size: 11px; font-weight: 700; padding: 4px 8px; border-radius: 6px; display: inline-flex; align-items: center; gap: 4px; }\n" +
                "    .card-title { font-size: 18px; font-weight: 700; margin-bottom: 8px; line-height: 1.35; }\n" +
                "    .card-desc { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }\n" +
                "    .card-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 12px; color: var(--text-muted); border-top: 1px solid var(--border); padding-top: 12px; }\n" +
                "    .meta-item strong { color: var(--text); display: block; font-size: 13px; }\n" +
                "    .card-bottom { background: #f8fafc; border-top: 1px solid var(--border); padding: 14px 20px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .price-tag { font-size: 18px; font-weight: 800; color: var(--primary); }\n" +
                "    .btn-book { background: var(--primary); color: white; border: none; padding: 8px 18px; border-radius: 8px; font-weight: 700; font-size: 13px; cursor: pointer; transition: 0.2s; }\n" +
                "    .btn-book:hover { background: var(--primary-hover); }\n" +
                "    .btn-book:disabled { background: #cbd5e1; cursor: not-allowed; }\n" +
                "    /* Tables */\n" +
                "    .table-container { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; overflow: hidden; box-shadow: var(--card-shadow); }\n" +
                "    table { width: 100%; border-collapse: collapse; font-size: 13px; text-align: left; }\n" +
                "    th { background: #f1f5f9; padding: 12px 16px; font-weight: 700; color: var(--text-muted); border-bottom: 1px solid var(--border); text-transform: uppercase; font-size: 11px; letter-spacing: 0.5px; }\n" +
                "    td { padding: 14px 16px; border-bottom: 1px solid var(--border); vertical-align: middle; }\n" +
                "    tr:hover td { background: #f8fafc; }\n" +
                "    .badge { display: inline-block; padding: 4px 8px; border-radius: 6px; font-size: 11px; font-weight: 700; }\n" +
                "    .badge-approved { background: #d1fae5; color: #065f46; }\n" +
                "    .badge-pending { background: #fef3c7; color: #92400e; }\n" +
                "    .badge-rejected { background: #fee2e2; color: #991b1b; }\n" +
                "    /* Modal */\n" +
                "    .modal-overlay { position: fixed; inset: 0; background: rgba(15,23,42,0.6); backdrop-filter: blur(4px); display: none; align-items: center; justify-content: center; z-index: 200; padding: 20px; }\n" +
                "    .modal-card { background: white; width: 100%; max-width: 500px; border-radius: 16px; overflow: hidden; box-shadow: 0 25px 50px -12px rgb(0 0 0 / 0.25); animation: pop 0.2s ease-out; }\n" +
                "    @keyframes pop { from { opacity: 0; transform: scale(0.96); } to { opacity: 1; transform: scale(1); } }\n" +
                "    .modal-header { background: #1e1b4b; color: white; padding: 18px 24px; display: flex; justify-content: space-between; align-items: center; }\n" +
                "    .modal-header h3 { font-size: 18px; font-weight: 700; }\n" +
                "    .modal-close { background: transparent; border: none; color: white; font-size: 20px; cursor: pointer; }\n" +
                "    .modal-body { padding: 24px; }\n" +
                "    .form-group { margin-bottom: 16px; }\n" +
                "    .form-group label { display: block; font-size: 12px; font-weight: 700; text-transform: uppercase; color: var(--text-muted); margin-bottom: 6px; }\n" +
                "    .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 14px; border: 1px solid var(--border); border-radius: 8px; font-size: 14px; }\n" +
                "    .total-box { background: var(--primary-light); padding: 14px 18px; border-radius: 10px; display: flex; justify-content: space-between; align-items: center; margin: 20px 0; }\n" +
                "    .total-box span { font-weight: 600; color: #3730a3; }\n" +
                "    .total-box strong { font-size: 22px; color: var(--primary); font-weight: 800; }\n" +
                "    .qr-box { background: #18181b; color: #a1a1aa; padding: 14px; border-radius: 10px; font-family: 'JetBrains Mono', monospace; font-size: 8px; line-height: 1.1; letter-spacing: 1px; white-space: pre; text-align: center; margin: 16px 0; overflow-x: auto; }\n" +
                "    footer { background: #0f172a; color: #94a3b8; text-align: center; padding: 18px; font-size: 13px; margin-top: auto; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<header>\n" +
                "  <div class=\"logo-container\">\n" +
                "    <div class=\"logo-badge\">UV</div>\n" +
                "    <div>\n" +
                "      <div class=\"logo-title\">UniVibe</div>\n" +
                "      <div class=\"logo-sub\">College Event & Ticket Management System</div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <nav>\n" +
                "    <button class=\"nav-btn active\" onclick=\"showTab('discover')\">Discover Events</button>\n" +
                "    <button class=\"nav-btn\" onclick=\"showTab('organizer')\">Organizer Studio</button>\n" +
                "    <button class=\"nav-btn\" onclick=\"showTab('admin')\">Admin Console</button>\n" +
                "  </nav>\n" +
                "  <div class=\"user-pill\">\n" +
                "    <div class=\"user-dot\"></div>\n" +
                "    <span>Alex Mercer (CS2024-001)</span>\n" +
                "  </div>\n" +
                "</header>\n" +
                "\n" +
                "<main>\n" +
                "  <!-- KPI Row -->\n" +
                "  <div class=\"kpi-row\">\n" +
                "    <div class=\"kpi-card\">\n" +
                "      <div class=\"kpi-label\">Total Students</div>\n" +
                "      <div class=\"kpi-val\" id=\"kpiStudents\">3</div>\n" +
                "      <div class=\"kpi-sub\">Active Registered</div>\n" +
                "    </div>\n" +
                "    <div class=\"kpi-card\">\n" +
                "      <div class=\"kpi-label\">College Events</div>\n" +
                "      <div class=\"kpi-val\" id=\"kpiEvents\">8</div>\n" +
                "      <div class=\"kpi-sub\">Across 8 Categories</div>\n" +
                "    </div>\n" +
                "    <div class=\"kpi-card\">\n" +
                "      <div class=\"kpi-label\">Tickets Booked</div>\n" +
                "      <div class=\"kpi-val\" id=\"kpiBookings\">4</div>\n" +
                "      <div class=\"kpi-sub\">Transaction Safe</div>\n" +
                "    </div>\n" +
                "    <div class=\"kpi-card\">\n" +
                "      <div class=\"kpi-label\">Net Revenue</div>\n" +
                "      <div class=\"kpi-val\" id=\"kpiRevenue\" style=\"color: #059669;\">₹600.00</div>\n" +
                "      <div class=\"kpi-sub\">Audited Collections</div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- TAB 1: DISCOVER EVENTS -->\n" +
                "  <div id=\"tabDiscover\">\n" +
                "    <div class=\"filter-bar\">\n" +
                "      <input type=\"text\" id=\"searchInput\" class=\"search-input\" placeholder=\"Search events, venues, topics...\" oninput=\"filterEvents()\">\n" +
                "      <div class=\"category-pills\" id=\"catPills\">\n" +
                "        <div class=\"pill active\" onclick=\"selectCategory('All')\">All</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('Hackathons')\">Hackathons</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('Workshops')\">Workshops</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('College Festivals')\">Festivals</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('Technical Events')\">Technical</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('Sports Events')\">Sports</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('Music and Dance Competitions')\">Music & Dance</div>\n" +
                "        <div class=\"pill\" onclick=\"selectCategory('Seminars')\">Seminars</div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"events-grid\" id=\"eventsGrid\">\n" +
                "      <!-- Cards populated via JS -->\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- TAB 2: ORGANIZER STUDIO -->\n" +
                "  <div id=\"tabOrganizer\" style=\"display: none;\">\n" +
                "    <div style=\"display:flex; justify-content:space-between; align-items:center; margin-bottom: 16px;\">\n" +
                "      <h2 style=\"font-size:20px; font-weight:800;\">Organizer Management Studio</h2>\n" +
                "      <button class=\"btn-book\" onclick=\"openCreateModal()\">+ Create New College Event</button>\n" +
                "    </div>\n" +
                "    <div class=\"table-container\">\n" +
                "      <table>\n" +
                "        <thead>\n" +
                "          <tr><th>ID</th><th>Title</th><th>Category</th><th>Venue</th><th>Date</th><th>Price</th><th>Seats (Avail/Total)</th><th>Status</th></tr>\n" +
                "        </thead>\n" +
                "        <tbody id=\"orgTableBody\"></tbody>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- TAB 3: ADMIN CONSOLE -->\n" +
                "  <div id=\"tabAdmin\" style=\"display: none;\">\n" +
                "    <div style=\"display:flex; justify-content:space-between; align-items:center; margin-bottom: 16px;\">\n" +
                "      <h2 style=\"font-size:20px; font-weight:800;\">Campus Governance & Event Approvals</h2>\n" +
                "      <a href=\"/api/download/report\" class=\"btn-book\" style=\"text-decoration:none; display:inline-block;\">Export System Audit Report (.txt)</a>\n" +
                "    </div>\n" +
                "    <div class=\"table-container\">\n" +
                "      <table>\n" +
                "        <thead>\n" +
                "          <tr><th>ID</th><th>Title</th><th>Category</th><th>Venue</th><th>Date</th><th>Seats</th><th>Status</th><th>Actions</th></tr>\n" +
                "        </thead>\n" +
                "        <tbody id=\"adminTableBody\"></tbody>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</main>\n" +
                "\n" +
                "<!-- BOOKING MODAL -->\n" +
                "<div class=\"modal-overlay\" id=\"bookingModal\">\n" +
                "  <div class=\"modal-card\">\n" +
                "    <div class=\"modal-header\">\n" +
                "      <h3 id=\"modalEventTitle\">Book Event Ticket</h3>\n" +
                "      <button class=\"modal-close\" onclick=\"closeBookingModal()\">&times;</button>\n" +
                "    </div>\n" +
                "    <div class=\"modal-body\">\n" +
                "      <input type=\"hidden\" id=\"bookEventId\">\n" +
                "      <div class=\"form-group\">\n" +
                "        <label>Attendee Name:</label>\n" +
                "        <input type=\"text\" value=\"Alex Mercer (CS2024-001)\" disabled>\n" +
                "      </div>\n" +
                "      <div class=\"form-group\">\n" +
                "        <label>Number of Seats:</label>\n" +
                "        <select id=\"seatQty\" onchange=\"updateModalTotal()\">\n" +
                "          <option value=\"1\">1 Seat</option>\n" +
                "          <option value=\"2\">2 Seats</option>\n" +
                "          <option value=\"3\">3 Seats</option>\n" +
                "          <option value=\"4\">4 Seats</option>\n" +
                "        </select>\n" +
                "      </div>\n" +
                "      <div class=\"form-group\">\n" +
                "        <label>Payment Method:</label>\n" +
                "        <select id=\"payMethod\">\n" +
                "          <option value=\"UPI\">UPI / QR Pay</option>\n" +
                "          <option value=\"CAMPUS_CARD\">Campus Student ID Card</option>\n" +
                "          <option value=\"NET_BANKING\">Net Banking</option>\n" +
                "        </select>\n" +
                "      </div>\n" +
                "      <div class=\"total-box\">\n" +
                "        <span>Total Amount Payable:</span>\n" +
                "        <strong id=\"modalTotalAmount\">₹0.00</strong>\n" +
                "      </div>\n" +
                "      <button class=\"btn-book\" style=\"width:100%; padding: 12px; font-size:15px;\" onclick=\"confirmBooking()\">Confirm & Pay</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "\n" +
                "<!-- DIGITAL TICKET PASS MODAL -->\n" +
                "<div class=\"modal-overlay\" id=\"ticketModal\">\n" +
                "  <div class=\"modal-card\">\n" +
                "    <div class=\"modal-header\" style=\"background:#065f46;\">\n" +
                "      <h3>Official Digital Admit Pass</h3>\n" +
                "      <button class=\"modal-close\" onclick=\"closeTicketModal()\">&times;</button>\n" +
                "    </div>\n" +
                "    <div class=\"modal-body\" style=\"text-align:center;\">\n" +
                "      <div style=\"font-size:12px; font-weight:700; color:var(--text-muted); text-transform:uppercase;\">Generated 2D QR Verification Pattern</div>\n" +
                "      <div class=\"qr-box\" id=\"ticketQrAscii\"></div>\n" +
                "      <h4 id=\"tktCodeDisplay\" style=\"font-family:'JetBrains Mono',monospace; font-size:18px; margin:8px 0;\">TKT-XXXX</h4>\n" +
                "      <p id=\"tktDetailsDisplay\" style=\"font-size:13px; color:var(--text-muted); line-height:1.6;\"></p>\n" +
                "      <div style=\"margin-top:20px; display:flex; gap:10px;\">\n" +
                "        <button class=\"btn-book\" style=\"flex:1; background:#059669;\" onclick=\"downloadTicketFile()\">Download Ticket (.txt)</button>\n" +
                "        <button class=\"btn-book\" style=\"flex:1; background:#64748b;\" onclick=\"closeTicketModal()\">Close</button>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "\n" +
                "<!-- CREATE EVENT MODAL -->\n" +
                "<div class=\"modal-overlay\" id=\"createModal\">\n" +
                "  <div class=\"modal-card\">\n" +
                "    <div class=\"modal-header\">\n" +
                "      <h3>Publish New College Event</h3>\n" +
                "      <button class=\"modal-close\" onclick=\"closeCreateModal()\">&times;</button>\n" +
                "    </div>\n" +
                "    <div class=\"modal-body\">\n" +
                "      <div class=\"form-group\"><label>Title:</label><input type=\"text\" id=\"newTitle\" placeholder=\"e.g. AI Innovation Summit\"></div>\n" +
                "      <div class=\"form-group\"><label>Category:</label>\n" +
                "        <select id=\"newCategory\">\n" +
                "          <option>Technical Events</option><option>Cultural Events</option><option>Sports Events</option>\n" +
                "          <option>Workshops</option><option>Seminars</option><option>Hackathons</option><option>Music and Dance Competitions</option><option>College Festivals</option>\n" +
                "        </select>\n" +
                "      </div>\n" +
                "      <div class=\"form-group\"><label>Venue:</label><input type=\"text\" id=\"newVenue\" placeholder=\"e.g. Science Auditorium\"></div>\n" +
                "      <div class=\"form-group\"><label>Date (YYYY-MM-DD):</label><input type=\"date\" id=\"newDate\" value=\"2026-12-15\"></div>\n" +
                "      <div class=\"form-group\"><label>Time:</label><input type=\"text\" id=\"newTime\" value=\"10:00 AM\"></div>\n" +
                "      <div class=\"form-group\"><label>Ticket Price (₹):</label><input type=\"number\" id=\"newPrice\" value=\"0.00\" step=\"10\"></div>\n" +
                "      <div class=\"form-group\"><label>Capacity (Seats):</label><input type=\"number\" id=\"newSeats\" value=\"100\"></div>\n" +
                "      <button class=\"btn-book\" style=\"width:100%; padding:12px; font-size:15px;\" onclick=\"submitNewEvent()\">Submit Event</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "\n" +
                "<footer>\n" +
                "  UniVibe – Academic Edition • Core Java & Built-in HTTP Server • Zero External Web Frameworks\n" +
                "</footer>\n" +
                "\n" +
                "<script>\n" +
                "let allEvents = [];\n" +
                "let currentCategory = 'All';\n" +
                "let selectedEventForBooking = null;\n" +
                "let lastBookedTicketCode = '';\n" +
                "\n" +
                "async function fetchEvents() {\n" +
                "  const res = await fetch('/api/events');\n" +
                "  allEvents = await res.json();\n" +
                "  renderEvents();\n" +
                "  renderTables();\n" +
                "}\n" +
                "\n" +
                "async function fetchStats() {\n" +
                "  const res = await fetch('/api/stats');\n" +
                "  const data = await res.json();\n" +
                "  document.getElementById('kpiStudents').innerText = data.totalStudents;\n" +
                "  document.getElementById('kpiEvents').innerText = data.totalEvents;\n" +
                "  document.getElementById('kpiBookings').innerText = data.confirmedBookings;\n" +
                "  document.getElementById('kpiRevenue').innerText = '₹' + data.netRevenue.toFixed(2);\n" +
                "}\n" +
                "\n" +
                "function renderEvents() {\n" +
                "  const grid = document.getElementById('eventsGrid');\n" +
                "  const q = document.getElementById('searchInput').value.toLowerCase();\n" +
                "  grid.innerHTML = '';\n" +
                "\n" +
                "  const filtered = allEvents.filter(e => {\n" +
                "    const matchCat = (currentCategory === 'All' || e.category.toLowerCase().includes(currentCategory.toLowerCase()));\n" +
                "    const matchQ = (e.title.toLowerCase().includes(q) || e.venue.toLowerCase().includes(q) || e.category.toLowerCase().includes(q));\n" +
                "    return matchCat && matchQ;\n" +
                "  });\n" +
                "\n" +
                "  if (filtered.length === 0) {\n" +
                "    grid.innerHTML = '<div style=\"grid-column:1/-1; text-align:center; padding:40px; color:var(--text-muted);\">No events found matching your criteria.</div>';\n" +
                "    return;\n" +
                "  }\n" +
                "\n" +
                "  filtered.forEach(e => {\n" +
                "    const isFull = e.availableSeats <= 0;\n" +
                "    const card = document.createElement('div');\n" +
                "    card.className = 'event-card';\n" +
                "    card.innerHTML = `\n" +
                "      <div class=\"card-top\">\n" +
                "        <div class=\"card-badges\">\n" +
                "          <span class=\"cat-badge\">${e.category}</span>\n" +
                "          ${e.isLowSeat ? '<span class=\"low-seat-badge\">⚠️ ' + e.availableSeats + ' seats left!</span>' : ''}\n" +
                "        </div>\n" +
                "        <h3 class=\"card-title\">${e.title}</h3>\n" +
                "        <p class=\"card-desc\">${e.description}</p>\n" +
                "        <div class=\"card-meta\">\n" +
                "          <div class=\"meta-item\">Venue<strong>${e.venue}</strong></div>\n" +
                "          <div class=\"meta-item\">Date & Time<strong>${e.date} at ${e.time}</strong></div>\n" +
                "          <div class=\"meta-item\">Available<strong>${e.availableSeats} / ${e.totalSeats} seats</strong></div>\n" +
                "          <div class=\"meta-item\">Rating<strong>${e.avgRating > 0 ? e.avgRating.toFixed(1) + ' ★ (' + e.feedbackCount + ')' : 'Unrated'}</strong></div>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div class=\"card-bottom\">\n" +
                "        <div class=\"price-tag\">${e.price > 0 ? '₹' + e.price.toFixed(2) : 'FREE'}</div>\n" +
                "        <button class=\"btn-book\" ${isFull ? 'disabled' : ''} onclick=\"openBookingModal(${e.id})\">\n" +
                "          ${isFull ? 'Sold Out' : 'Book Tickets'}\n" +
                "        </button>\n" +
                "      </div>\n" +
                "    `;\n" +
                "    grid.appendChild(card);\n" +
                "  });\n" +
                "}\n" +
                "\n" +
                "function renderTables() {\n" +
                "  // Organizer table\n" +
                "  const orgT = document.getElementById('orgTableBody');\n" +
                "  orgT.innerHTML = allEvents.map(e => `\n" +
                "    <tr>\n" +
                "      <td><strong>${e.id}</strong></td>\n" +
                "      <td>${e.title}</td>\n" +
                "      <td>${e.category}</td>\n" +
                "      <td>${e.venue}</td>\n" +
                "      <td>${e.date}</td>\n" +
                "      <td>₹${e.price.toFixed(2)}</td>\n" +
                "      <td>${e.availableSeats} / ${e.totalSeats}</td>\n" +
                "      <td><span class=\"badge badge-${e.status.toLowerCase()}\">${e.status}</span></td>\n" +
                "    </tr>\n" +
                "  `).join('');\n" +
                "\n" +
                "  // Admin table\n" +
                "  const admT = document.getElementById('adminTableBody');\n" +
                "  admT.innerHTML = allEvents.map(e => `\n" +
                "    <tr>\n" +
                "      <td><strong>${e.id}</strong></td>\n" +
                "      <td>${e.title}</td>\n" +
                "      <td>${e.category}</td>\n" +
                "      <td>${e.venue}</td>\n" +
                "      <td>${e.date}</td>\n" +
                "      <td>${e.availableSeats} / ${e.totalSeats}</td>\n" +
                "      <td><span class=\"badge badge-${e.status.toLowerCase()}\">${e.status}</span></td>\n" +
                "      <td>\n" +
                "        <button class=\"btn-book\" style=\"padding:4px 8px; font-size:11px; background:#059669;\" onclick=\"approveEvent(${e.id})\">Approve</button>\n" +
                "        <button class=\"btn-book\" style=\"padding:4px 8px; font-size:11px; background:#dc2626;\" onclick=\"rejectEvent(${e.id})\">Reject</button>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  `).join('');\n" +
                "}\n" +
                "\n" +
                "function selectCategory(cat) {\n" +
                "  currentCategory = cat;\n" +
                "  document.querySelectorAll('#catPills .pill').forEach(p => {\n" +
                "    p.classList.toggle('active', p.innerText === cat || (cat === 'All' && p.innerText === 'All'));\n" +
                "  });\n" +
                "  renderEvents();\n" +
                "}\n" +
                "\n" +
                "function filterEvents() { renderEvents(); }\n" +
                "\n" +
                "function showTab(tab) {\n" +
                "  document.getElementById('tabDiscover').style.display = (tab === 'discover' ? 'block' : 'none');\n" +
                "  document.getElementById('tabOrganizer').style.display = (tab === 'organizer' ? 'block' : 'none');\n" +
                "  document.getElementById('tabAdmin').style.display = (tab === 'admin' ? 'block' : 'none');\n" +
                "  document.querySelectorAll('nav .nav-btn').forEach((b, i) => {\n" +
                "    b.classList.toggle('active', (tab === 'discover' && i === 0) || (tab === 'organizer' && i === 1) || (tab === 'admin' && i === 2));\n" +
                "  });\n" +
                "}\n" +
                "\n" +
                "function openBookingModal(eventId) {\n" +
                "  selectedEventForBooking = allEvents.find(e => e.id === eventId);\n" +
                "  if (!selectedEventForBooking) return;\n" +
                "  document.getElementById('modalEventTitle').innerText = selectedEventForBooking.title;\n" +
                "  document.getElementById('bookEventId').value = eventId;\n" +
                "  document.getElementById('seatQty').value = '1';\n" +
                "  updateModalTotal();\n" +
                "  document.getElementById('bookingModal').style.display = 'flex';\n" +
                "}\n" +
                "\n" +
                "function closeBookingModal() { document.getElementById('bookingModal').style.display = 'none'; }\n" +
                "function closeTicketModal() { document.getElementById('ticketModal').style.display = 'none'; }\n" +
                "function openCreateModal() { document.getElementById('createModal').style.display = 'flex'; }\n" +
                "function closeCreateModal() { document.getElementById('createModal').style.display = 'none'; }\n" +
                "\n" +
                "function updateModalTotal() {\n" +
                "  if (!selectedEventForBooking) return;\n" +
                "  const qty = parseInt(document.getElementById('seatQty').value);\n" +
                "  const total = qty * selectedEventForBooking.price;\n" +
                "  document.getElementById('modalTotalAmount').innerText = '₹' + total.toFixed(2);\n" +
                "}\n" +
                "\n" +
                "async function confirmBooking() {\n" +
                "  const eventId = document.getElementById('bookEventId').value;\n" +
                "  const seats = document.getElementById('seatQty').value;\n" +
                "  const paymentMethod = document.getElementById('payMethod').value;\n" +
                "\n" +
                "  const res = await fetch('/api/book', {\n" +
                "    method: 'POST',\n" +
                "    body: new URLSearchParams({ studentId: '1', eventId, seats, paymentMethod })\n" +
                "  });\n" +
                "  const data = await res.json();\n" +
                "  if (data.success) {\n" +
                "    closeBookingModal();\n" +
                "    lastBookedTicketCode = data.ticketCode;\n" +
                "    document.getElementById('ticketQrAscii').innerText = data.asciiQR;\n" +
                "    document.getElementById('tktCodeDisplay').innerText = data.ticketCode;\n" +
                "    document.getElementById('tktDetailsDisplay').innerHTML = `\n" +
                "      <strong>${data.eventTitle}</strong><br>\n" +
                "      Attendee: ${data.studentName} (${data.rollNumber})<br>\n" +
                "      Venue: ${data.venue} | Seat: ${data.seatNumber}<br>\n" +
                "      Ref: ${data.bookingRef} | Paid: ₹${data.totalAmount.toFixed(2)}\n" +
                "    `;\n" +
                "    document.getElementById('ticketModal').style.display = 'flex';\n" +
                "    fetchEvents();\n" +
                "    fetchStats();\n" +
                "  } else {\n" +
                "    alert('Booking Failed: ' + data.error);\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "function downloadTicketFile() {\n" +
                "  window.location.href = '/api/download/ticket?code=' + encodeURIComponent(lastBookedTicketCode);\n" +
                "}\n" +
                "\n" +
                "async function submitNewEvent() {\n" +
                "  const title = document.getElementById('newTitle').value;\n" +
                "  const category = document.getElementById('newCategory').value;\n" +
                "  const venue = document.getElementById('newVenue').value;\n" +
                "  const date = document.getElementById('newDate').value;\n" +
                "  const time = document.getElementById('newTime').value;\n" +
                "  const price = document.getElementById('newPrice').value;\n" +
                "  const seats = document.getElementById('newSeats').value;\n" +
                "\n" +
                "  const res = await fetch('/api/events/create', {\n" +
                "    method: 'POST',\n" +
                "    body: new URLSearchParams({ title, category, venue, date, time, price, seats, organizerId: '1' })\n" +
                "  });\n" +
                "  const data = await res.json();\n" +
                "  if (data.success) {\n" +
                "    alert('Event created successfully!');\n" +
                "    closeCreateModal();\n" +
                "    fetchEvents();\n" +
                "    fetchStats();\n" +
                "  } else {\n" +
                "    alert('Error: ' + data.error);\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "async function approveEvent(id) {\n" +
                "  await fetch('/api/events/approve', { method: 'POST', body: new URLSearchParams({ id }) });\n" +
                "  fetchEvents();\n" +
                "}\n" +
                "\n" +
                "async function rejectEvent(id) {\n" +
                "  await fetch('/api/events/reject', { method: 'POST', body: new URLSearchParams({ id }) });\n" +
                "  fetchEvents();\n" +
                "}\n" +
                "\n" +
                "// Init\n" +
                "fetchEvents();\n" +
                "fetchStats();\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }
}
