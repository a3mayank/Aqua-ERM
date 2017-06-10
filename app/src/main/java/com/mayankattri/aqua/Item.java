package com.mayankattri.aqua;

import java.io.Serializable;

/**
 * Created by mayank on 4/6/17.
 */

public class Item implements Serializable {

    private String name;
    private String quantity;
    private  boolean empty;

    public Item() {}

    public Item(String name, String quantity, Boolean empty) {
        this.name = name;
        this.quantity = quantity;
        this.empty = empty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
