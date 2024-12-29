package Bo.custom;

import Bo.SuperBo;
import Dto.CustomerDto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface CustomerBo extends SuperBo{
    public boolean saveCustomer(CustomerDto dto) throws Exception;
    public boolean updateCustomer(CustomerDto dto) throws Exception;
    public boolean deleteCustomer(String ID) throws Exception;
    public List<CustomerDto> getAllCustomer() throws SQLException, ClassNotFoundException;
    public String generateNewCustomerID() throws SQLException, ClassNotFoundException, IOException;
    public boolean CustomerIdExists(String studentId) throws SQLException, ClassNotFoundException;
}
