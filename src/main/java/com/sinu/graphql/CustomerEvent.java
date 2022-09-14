package com.sinu.graphql;

public class CustomerEvent {
    
    private Customer customer;
    private CustomerEventyType event;
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public CustomerEventyType getEvent() {
        return event;
    }
    public void setEvent(CustomerEventyType event) {
        this.event = event;
    }
    
    
    }
