package Bo;

import Bo.custom.impl.CustomerBoImpl;

public class BoFactory {
    private static BoFactory boFactory;
    private BoFactory() {

    }

    public static BoFactory getBoFactory() {
        return (boFactory == null) ? boFactory = new BoFactory() : boFactory;
    }
    public enum BoType{
        CUSTOMER, ITEM, PLACEORDER

    }
    public SuperBo getBo(BoType boType){
        switch (boType){
            case CUSTOMER:
                return new CustomerBoImpl();
            default:
                return null;

        }
    }
}
