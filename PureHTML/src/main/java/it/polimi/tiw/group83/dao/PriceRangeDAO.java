package it.polimi.tiw.group83.dao;

import it.polimi.tiw.group83.beans.PriceRange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PriceRangeDAO {
    private final Connection con;

    public PriceRangeDAO(Connection connection) {
        this.con = connection;
    }

    public List<PriceRange> findPriceRangesForSupplier(int supplierCode) throws SQLException {
        String query = "SELECT  * FROM price_range  WHERE supplier_code = ?";
        List<PriceRange> ranges = new ArrayList<>();
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, supplierCode);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return ranges;
                else {
                    while (result.next()) {
                        PriceRange range = new PriceRange();
                        range.setMaxArticles(result.getInt("max_articles"));
                        range.setMinArticles(result.getInt("min_articles"));
                        range.setShippingCost(result.getInt("shipping_cost"));
                        ranges.add(range);
                    }
                }
            }
        }
        return ranges;
    }
}
