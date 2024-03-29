package it.polimi.tiw.group83.beans;

public class PriceRange {
    private int minArticles;
    private int maxArticles;
    private float shippingCost;

    public int getMinArticles() {
        return minArticles;
    }

    public void setMinArticles(int minArticles) {
        this.minArticles = minArticles;
    }

    public int getMaxArticles() {
        return maxArticles;
    }

    public void setMaxArticles(int maxArticles) {
        this.maxArticles = maxArticles;
    }

    public float getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(float shippingCost) {
        this.shippingCost = shippingCost;
    }

    @Override
    public String toString() {
        return "Min Articles: " + minArticles +
                ", Max Articles: " + maxArticles +
                ", Shipping Cost: $" + shippingCost;
    }
}
