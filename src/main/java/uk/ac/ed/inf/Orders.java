package uk.ac.ed.inf;

import java.sql.Date;
import java.util.ArrayList;

public class Orders {

    private String orderNumber;
    private Date deliveryDate;
    private String customer;
    private String deliverTo;
    private ArrayList<String> items;


    public Orders(String orderNumber, Date deliveryDate, String customer, String deliverTo,
                  ArrayList<String> items) {
        this.orderNumber = orderNumber;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
    }

}
