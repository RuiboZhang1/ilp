package uk.ac.ed.inf;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Storing the detail of each order
 */
public class Order {

    // private variables
    private String orderNo;
    private Date deliveryDate;
    private String customer;
    private String deliverTo;
    private ArrayList<String> items;

    /**
     * Constructor of Order
     * @param orderNo order number
     * @param deliveryDate date of delivery  dd-mm-yyyy
     * @param customer student number of the customer
     * @param deliverTo What3words of the target position
     * @param items list of item of the order
     */
    public Order(String orderNo, Date deliveryDate, String customer, String deliverTo,
                  ArrayList<String> items) {
        this.orderNo = orderNo;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
    }

    // getter
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
