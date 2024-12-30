package Controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.sql.*;
@WebServlet(urlPatterns = "/placeOrder")
public class PlaceOrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

            // Fetch orders (example)
            String query = "SELECT * FROM orders";
            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            // Create JSON array builder to store all orders
            JsonArrayBuilder allOrders = Json.createArrayBuilder();

            while (resultSet.next()) {
                String orderId = resultSet.getString("oid");
                String customerId = resultSet.getString("date");
                double totalAmount = resultSet.getDouble("totalAmount");

                // Build order JSON object
                JsonObjectBuilder order = Json.createObjectBuilder();
                order.add("orderId", orderId);
                order.add("customerId", customerId);
                order.add("totalAmount", totalAmount);
                allOrders.add(order);
            }

            resp.setContentType("application/json");
            resp.getWriter().write(allOrders.build().toString());

        } catch (ClassNotFoundException | SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

        // Retrieve purchase order data from request
        String customerId = req.getParameter("customerId");
        String[] itemCodes = req.getParameterValues("itemCodes"); // Item codes in array
        String[] quantities = req.getParameterValues("quantities"); // Corresponding quantities in array

        double totalAmount = 0;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {

            //Create SQL query for inserting a new order
            String orderQuery = "INSERT INTO purchase_order (customerId, totalAmount) VALUES (?, ?)";
            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setString(1, customerId);

                //Calculate total amount by iterating through items
                for (int i = 0; i < itemCodes.length; i++) {
                    String itemCode = itemCodes[i];
                    int quantity = Integer.parseInt(quantities[i]);

                    //Fetch item details (unit price) from item table
                    String itemQuery = "SELECT unitPrice FROM item WHERE code = ?";
                    try (PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
                        itemStmt.setString(1, itemCode);
                        ResultSet itemResultSet = itemStmt.executeQuery();
                        if (itemResultSet.next()) {
                            double unitPrice = itemResultSet.getDouble("unitPrice");
                            totalAmount += unitPrice * quantity;
                        }
                    }
                }

                //Set total amount in the order statement and execute
                orderStmt.setDouble(2, totalAmount);
                int rowsAffected = orderStmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setContentType("text/plain");
                    resp.getWriter().write("Order placed successfully!");
                } else {
                    resp.getWriter().write("Failed to place the order.");
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("orderId");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            // Delete the purchase order
            String deleteOrderQuery = "DELETE FROM purchase_order WHERE orderId = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteOrderQuery)) {
                deleteStmt.setString(1, orderId);

                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Order deleted successfully!");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Order not found.");
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
