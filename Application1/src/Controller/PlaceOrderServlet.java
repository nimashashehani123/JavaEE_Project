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
    protected String GenerateNextOrderId() {
        String nextId = null;
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            String query = "SELECT oid FROM orders ORDER BY oid DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String lastId = rs.getString("oid");
                    int numericPart = Integer.parseInt(lastId.replaceAll("[^0-9]", ""));
                    nextId = String.format("O%03d", numericPart + 1);
                } else {
                    nextId = "O001";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextId;
    }

    //get all customers
    private void getAllCustomers(HttpServletResponse resp) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
        ResultSet resultSet = connection.prepareStatement("SELECT * FROM customer").executeQuery();

        JsonArrayBuilder allCustomer = Json.createArrayBuilder();

        while (resultSet.next()) {
            JsonObjectBuilder customer = Json.createObjectBuilder();
            customer.add("id", resultSet.getString("id"));
            customer.add("name", resultSet.getString("name"));
            customer.add("address", resultSet.getString("address"));

            allCustomer.add(customer);
        }

        resp.setContentType("application/json");
        resp.getWriter().write(allCustomer.build().toString());
    }


    // get customer by id
    private void getCustomerById(String customerId, HttpServletResponse resp) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

        String query = "SELECT * FROM customer WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, customerId);

            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                JsonObjectBuilder customer = Json.createObjectBuilder();
                customer.add("id", resultSet.getString("id"));
                customer.add("name", resultSet.getString("name"));
                customer.add("address", resultSet.getString("address"));

                resp.setContentType("application/json");
                resp.getWriter().write(customer.build().toString());
            } else {
                System.out.println("Customer not found.");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Customer not found.");
            }
        }
    }
    private void getItemById(String itemId, HttpServletResponse resp) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

        String query = "SELECT * FROM item WHERE code = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, itemId);

            ResultSet resultSet = pst.executeQuery();

            if (resultSet.next()) {
                JsonObjectBuilder item = Json.createObjectBuilder();
                item.add("code", resultSet.getString("code"));
                item.add("name", resultSet.getString("description"));
                item.add("qtyOnHand", resultSet.getDouble("qtyOnHand"));
                item.add("unitPrice", resultSet.getInt("unitPrice"));

                resp.setContentType("application/json");
                resp.getWriter().write(item.build().toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Item not found.");
            }
        }
    }
    private void getAllItems(HttpServletResponse resp) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");

        String query = "SELECT * FROM item";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            ResultSet resultSet = pst.executeQuery();

            JsonArrayBuilder allItems = Json.createArrayBuilder();

            while (resultSet.next()) {
                JsonObjectBuilder item = Json.createObjectBuilder();
                item.add("code", resultSet.getString("code"));
                item.add("name", resultSet.getString("description"));
                item.add("qtyOnHand", resultSet.getDouble("qtyOnHand"));
                item.add("unitPrice", resultSet.getInt("unitPrice"));

                allItems.add(item);
            }

            resp.setContentType("application/json");
            resp.getWriter().write(allItems.build().toString());
        }
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getParameter("type");
        String id = req.getParameter("id");
        String action = req.getParameter("action");

            try {
                if ("generateNewId".equals(action)) {
                    String newOrderId = GenerateNextOrderId();
                    resp.setContentType("text/plain");
                    resp.getWriter().write(newOrderId);
                }else if ("loadOrders".equals(action)) {
                     GetAllOrders(resp);
                }
                else if ("customer".equalsIgnoreCase(type)) {
                    if (id != null && !id.isEmpty()) {
                        getCustomerById(id, resp);
                    } else {
                        getAllCustomers(resp);
                    }
                } else if ("item".equalsIgnoreCase(type)) {
                    if (id != null && !id.isEmpty()) {
                        getItemById(id, resp);
                    } else {
                        getAllItems(resp);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Invalid type parameter. Use 'type=customer' or 'type=item'.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Database error occurred.");
            }

    }

    private void GetAllOrders(HttpServletResponse resp) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
            ResultSet resultSet = connection.prepareStatement("select * from orders").executeQuery();

            //create json array builder
            JsonArrayBuilder allOrder = Json.createArrayBuilder();


            while ((resultSet.next())) {
                String oid = resultSet.getString("oid");
                String date = resultSet.getString("date");
                String customerID = resultSet.getString("customerID");
                Double totalAmount = resultSet.getDouble("totalAmount");

                JsonObjectBuilder order = Json.createObjectBuilder();

                order.add("oid", oid);
                order.add("customerID", customerID);
                order.add("date", date);
                order.add("totalAmount", totalAmount);

                allOrder.add(order);

            }

            resp.setContentType("application/json");
            resp.getWriter().write(allOrder.build().toString());


        } catch (ClassNotFoundException | IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String action = req.getParameter("action");
        String type = req.getParameter("type"); // Get the type (customer or item)

        // Validate if action is provided
        if (action == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing action parameter.");
            return;
        }

        // Handle based on the action and type
        if ("updateQtyOnHand".equals(action)) {
            if ("item".equals(type)) {
                updateQtyOnHand(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid type. Expected 'item'.");
            }
        } else if ("restoreQtyOnHand".equals(action)) {
            if ("item".equals(type)) {
                restoreQtyOnHand(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid type. Expected 'item'.");
            }
        } else if ("placeOrder".equals(action)) {
            if ("customer".equals(type)) {
                placeOrder(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid type. Expected 'customer'.");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Unknown action.");
        }

       /* // Retrieve purchase order data from request
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
        }*/
    }
    private void updateQtyOnHand(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String itemId = req.getParameter("itemId");
        String quantity = req.getParameter("quantity");

        // Validate input parameters
        if (itemId == null || quantity == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid parameters.");
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            // Update the quantity in the database by decrementing
            String updateQuery = "UPDATE item SET qtyOnHand = qtyOnHand - ? WHERE code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setInt(1, Integer.parseInt(quantity));
                stmt.setString(2, itemId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Quantity updated successfully.");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Item not found.");
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Database error: " + e.getMessage());
        }
    }

    private void restoreQtyOnHand(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String itemId = req.getParameter("itemId");
        String quantity = req.getParameter("quantity");

        // Validate input parameters
        if (itemId == null || quantity == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid parameters.");
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            // Update the quantity in the database by restoring
            String updateQuery = "UPDATE item SET qtyOnHand = qtyOnHand + ? WHERE code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setInt(1, Integer.parseInt(quantity));
                stmt.setString(2, itemId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Quantity restored successfully.");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Item not found.");
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Database error: " + e.getMessage());
        }
    }

    private void placeOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Logic for placing an order (update order table, save order details)

        String customerId = req.getParameter("customerId");
        String oid = req.getParameter("oid");
        String items = req.getParameter("items"); // This could be a JSON array or a string with item details

        // Validate input parameters
        if (customerId == null || items == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid order details.");
            return;
        }

        // Add logic here to insert the order into the order table and update other relevant tables

        Connection connection = null; // Define connection outside the try block

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
            connection.setAutoCommit(false); // Start transaction

            // Insert into orders table
            String insertOrderQuery = "INSERT INTO orders (oid, customerID, date, totalAmount) VALUES (?, ?, NOW(), ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertOrderQuery)) {
                stmt.setString(1, oid); // Use the manually generated order ID
                stmt.setString(2, customerId);
                stmt.setDouble(3, calculateTotal(items)); // Assume calculateTotal is defined

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Insert order details (items ordered)
                    for (String item : items.split(",")) {
                        String[] itemDetails = item.split("-");
                        String itemId = itemDetails[0];
                        int quantity = Integer.parseInt(itemDetails[1]);
                        double unitPrice = Double.parseDouble(itemDetails[2]);

                        String insertOrderDetailsQuery = "INSERT INTO orderdetails (oid, itemCode, qty, unitPrice) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmtDetails = connection.prepareStatement(insertOrderDetailsQuery)) {
                            stmtDetails.setString(1, oid); // Use the manually generated order ID
                            stmtDetails.setString(2, itemId);
                            stmtDetails.setInt(3, quantity);
                            stmtDetails.setDouble(4, unitPrice);
                            stmtDetails.executeUpdate();
                        }
                    }

                    connection.commit(); // Commit transaction
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Order placed successfully.");
                } else {
                    connection.rollback(); // Rollback transaction
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("Failed to place order.");
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Database error: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Restore default behavior
                    connection.close(); // Close the connection
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }


    }

    private double calculateTotal(String items) {
        // Calculate the total based on the items in the cart
        // You can parse the items (or JSON) and calculate the total
        double total = 0.0;
        for (String item : items.split(",")) {
            String[] itemDetails = item.split("-");
            int quantity = Integer.parseInt(itemDetails[1]);
            double unitPrice = Double.parseDouble(itemDetails[2]);
            total += quantity * unitPrice;
        }
        return total;
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String oid = req.getParameter("oid");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            connection.setAutoCommit(false); // Start transaction

            try {
                // Delete order details
                String deleteOrderDetailsQuery = "DELETE FROM orderdetails WHERE oid = ?";
                try (PreparedStatement stmtDetails = connection.prepareStatement(deleteOrderDetailsQuery)) {
                    stmtDetails.setString(1, oid);
                    stmtDetails.executeUpdate();
                }

                // Delete order
                String deleteOrderQuery = "DELETE FROM orders WHERE oid = ?";
                try (PreparedStatement stmtOrder = connection.prepareStatement(deleteOrderQuery)) {
                    stmtOrder.setString(1, oid);
                    int rowsAffected = stmtOrder.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Order and related details deleted successfully.");
                    } else {
                        System.out.println("Order not found.");
                    }
                }

                connection.commit(); // Commit transaction
            } catch (SQLException e) {
                connection.rollback(); // Rollback if any error occurs
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
