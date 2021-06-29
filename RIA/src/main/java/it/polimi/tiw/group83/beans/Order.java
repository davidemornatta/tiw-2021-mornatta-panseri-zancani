package it.polimi.tiw.group83.beans;

import java.sql.Date;

public class Order {
    private int code;
    private float totalAmount;
    private Date shippingDate;
    private String shippingAddress;
    private int supplierCode;
    private int userId;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(Date shippingDate) {
        this.shippingDate = shippingDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public int getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(int supplierCode) {
        this.supplierCode = supplierCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "code=" + code +
                ", totalAmount=" + totalAmount +
                ", shippingDate=" + shippingDate +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", supplierCode=" + supplierCode +
                ", userId=" + userId +
                '}';
    }
}
