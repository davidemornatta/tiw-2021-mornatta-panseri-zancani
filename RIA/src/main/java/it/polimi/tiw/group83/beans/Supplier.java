package it.polimi.tiw.group83.beans;

public class Supplier {
    private int code;
    private String name;
    private int rating;
    private float freeShippingCost;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public float getFreeShippingCost() {
        return freeShippingCost;
    }

    public void setFreeShippingCost(float freeShippingCost) {
        this.freeShippingCost = freeShippingCost;
    }
}
