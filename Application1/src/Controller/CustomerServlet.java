package Controller;

import db.DbConnection;
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

    protected void GenerateNextCustomerId(){

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                ResultSet resultSet = DbConnection.getInstance().getConnection().prepareStatement("select * from customer").executeQuery();
                //create json Arrys
                JsonArrayBuilder allCustomers = Json.createArrayBuilder();
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    String address = resultSet.getString("address");

                    JsonObjectBuilder customer = Json.createObjectBuilder();
                    customer.add("id",id);
                    customer.add("name",name);
                    customer.add("address",address);

                    allCustomers.add(customer);
                }
                resp.setContentType("application/json");
                resp.getWriter().write(allCustomers.build().toString());

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    String id = req.getParameter("id");
                    String name = req.getParameter("name");
                    String address = req.getParameter("address");

                        String sql = "INSERT INTO customer (id, name, address) VALUES (?, ?, ?)";
                        try (PreparedStatement pst = DbConnection.getInstance().getConnection().prepareStatement(sql)) {
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
                } catch (ClassNotFoundException | SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("Error: " + e.getMessage());
                }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String id = req.getParameter("id");
            String name = req.getParameter("name");
            String address = req.getParameter("address");

                String query = "UPDATE customer SET name = ?, address = ? WHERE id = ?";
                try (PreparedStatement stmt = DbConnection.getInstance().getConnection().prepareStatement(query)) {
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
                } catch (ClassNotFoundException |SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Database error occurred.");
            }
        }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
            String query = "DELETE FROM customer WHERE id = ?";

            try (PreparedStatement stmt = DbConnection.getInstance().getConnection().prepareStatement(query)) {
                stmt.setString(1, id);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("Customer deleted successfully!");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Customer not found.");
                }
        } catch (ClassNotFoundException |SQLException e) {
            e.printStackTrace();
        }
    }
        }
