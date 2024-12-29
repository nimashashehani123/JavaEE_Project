package Dao;

import Dao.custom.impl.CustomerDAOImpl;

public class DAOFactory {
    private static DAOFactory daoFactory;

    private DAOFactory() {
    }

    public static DAOFactory getDaoFactory() {
        return (daoFactory == null) ? daoFactory = new DAOFactory() : daoFactory;
    }

    public enum DaoType {
        CUSTOMER, ITEM , PLACEORDER
    }

    public SuperDAO getDAO(DaoType daoType) {
        switch (daoType) {
            case CUSTOMER:
                return new CustomerDAOImpl();
            default:
                return null;
        }
    }
}
