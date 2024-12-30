package Dao.custom.impl;
import Dao.custom.CustomerDAO;
import Db.DbConnection;
import Dto.CustomerDto;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO{

    @Override
    public boolean save(CustomerDto entity) throws Exception {
        return false;
    }

    @Override
    public boolean update(CustomerDto entity) throws Exception {
        return false;
    }

    @Override
    public boolean delete(String ID) throws Exception {
        return false;
    }

    @Override
    public List<CustomerDto> getAll() throws SQLException, ClassNotFoundException {
        List<CustomerDto> cusList = new ArrayList<>();
        ResultSet resultSet = DbConnection.getInstance().getConnection().createStatement().executeQuery("select * from customer");
        while (resultSet.next()) {
            cusList.add(new CustomerDto(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3)));
        }
        return cusList;
    }

    @Override
    public String generateNewID() throws SQLException, ClassNotFoundException, IOException {
        return null;
    }

    @Override
    public boolean IdExists(String id) throws SQLException, ClassNotFoundException {
        return false;
    }
}
