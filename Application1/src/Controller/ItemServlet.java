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
@WebServlet(urlPatterns = "/item")
public class ItemServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection =  DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root","1234");
            ResultSet resultSet = connection.prepareStatement("select * from item").executeQuery();

            //create json array builder
            JsonArrayBuilder allItem = Json.createArrayBuilder();


            while ((resultSet.next())){
                String code = resultSet.getString("code");
                String description = resultSet.getString("description");
                int qtyOnHand = resultSet.getInt("qtyOnHand");
                Double unitPrice = resultSet.getDouble("unitPrice");

                JsonObjectBuilder item = Json.createObjectBuilder();

                item.add("code", code);
                item.add("description", description);
                item.add("qtyOnHand", qtyOnHand);
                item.add("unitPrice", unitPrice);

                allItem.add(item);

            }

            resp.setContentType("application/json");
            resp.getWriter().write(allItem.build().toString());



        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        int qtyOnHand = Integer.parseInt(req.getParameter("qtyOnHand"));
        Double unitPrice = Double.valueOf(req.getParameter("unitPrice"));

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            String sql = "INSERT INTO item (code, description, qtyOnHand,unitPrice) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setString(1, code);
                pst.setString(2, description);
                pst.setString(3, String.valueOf(qtyOnHand));
                pst.setString(4, String.valueOf(unitPrice));

                int rowsAffected = pst.executeUpdate();
                resp.setContentType("text/plain");
                if (rowsAffected > 0) {
                    resp.getWriter().write("Item added successfully.");
                } else {
                    resp.getWriter().write("Failed to add item.");
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String description = req.getParameter("description");
        int qtyOnHand = Integer.parseInt(req.getParameter("qtyOnHand"));
        Double unitPrice = Double.valueOf(req.getParameter("unitPrice"));
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {

            String query = "UPDATE item SET description = ?, qtyOnHand = ?, unitPrice = ? WHERE code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, description);
                stmt.setString(2, String.valueOf(qtyOnHand));
                stmt.setString(3, String.valueOf(unitPrice));
                stmt.setString(4, code);

                int rowsAffected = stmt.executeUpdate();
                resp.setContentType("text/plain");
                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Item not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Database error occurred.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String query = "DELETE FROM item WHERE code = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, code);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Item deleted successfully!");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("item not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
