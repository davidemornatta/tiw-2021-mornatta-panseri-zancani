package it.polimi.tiw.group83.beans;

public class Supplier {
    private int code;
    private String name;
    private int rating;
    private int freeShippingCost;

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

    public int getFreeShippingCost() {
        return freeShippingCost;
    }

    public void setFreeShippingCost(int freeShippingCost) {
        this.freeShippingCost = freeShippingCost;
    }
}
