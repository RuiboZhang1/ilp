package uk.ac.ed.inf;

import java.sql.Date;
import java.util.ArrayList;

public class Order {

    private String orderNo;
    private Date deliveryDate;
    private String customer;
    private String deliverTo;
    private ArrayList<String> items;


    public Order(String orderNo, Date deliveryDate, String customer, String deliverTo,
                  ArrayList<String> items) {
        this.orderNo = orderNo;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public ArrayList<String> getItems() {
        return items;
    }
}
