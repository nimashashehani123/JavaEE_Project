package Controller;

import Bo.BoFactory;
import Bo.custom.CustomerBo;
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

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {

    CustomerBo customerBo = (CustomerBo) BoFactory.getBoFactory().getBo(BoFactory.BoType.CUSTOMER);

    protected String GenerateNextCustomerId() {
        String nextId = null;
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
            String query = "SELECT id FROM customer ORDER BY id DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String lastId = rs.getString("id");
                    int numericPart = Integer.parseInt(lastId.replaceAll("[^0-9]", ""));
                    nextId = String.format("C%03d", numericPart + 1);
                } else {
                    nextId = "C001";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextId;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("generateNewId".equals(req.getParameter("action"))) {
            String newCustomerId = GenerateNextCustomerId();
            resp.setContentType("text/plain");
            resp.getWriter().write(newCustomerId);
        } else {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234");
                ResultSet resultSet = connection.prepareStatement("select * from customer").executeQuery();

                //create json array builder
                JsonArrayBuilder allCustomer = Json.createArrayBuilder();


                while ((resultSet.next())) {
                    String id = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    String address = resultSet.getString("address");

                    JsonObjectBuilder customer = Json.createObjectBuilder();

                    customer.add("id", id);
                    customer.add("name", name);
                    customer.add("address", address);

                    allCustomer.add(customer);

                }

                resp.setContentType("application/json");
                resp.getWriter().write(allCustomer.build().toString());


            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    String id = req.getParameter("id");
                    String name = req.getParameter("name");
                    String address = req.getParameter("address");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {
                        String sql = "INSERT INTO customer (id, name, address) VALUES (?, ?, ?)";
                        try (PreparedStatement pst = connection.prepareStatement(sql)) {
                            pst.setString(1, id);
                            pst.setString(2, name);
                            pst.setString(3, address);

                            int rowsAffected = pst.executeUpdate();
                            resp.setContentType("text/plain");
                            if (rowsAffected > 0) {
                                resp.getWriter().write("Customer added successfully.");
                            } else {
                                resp.getWriter().write("Failed to add customer.");
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
            String id = req.getParameter("id");
            String name = req.getParameter("name");
            String address = req.getParameter("address");
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {

                String query = "UPDATE customer SET name = ?, address = ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setString(2, address);
                    stmt.setString(3, id);
                    int rowsAffected = stmt.executeUpdate();
                    resp.setContentType("text/plain");
                    if (rowsAffected > 0) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("Customer not found.");
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
        String id = req.getParameter("id");
            String query = "DELETE FROM customer WHERE id = ?";
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "1234")) {

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Customer deleted successfully!");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Customer not found.");
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }}
