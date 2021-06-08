package it.polimi.tiw.group83.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.tiw.group83.beans.Cart;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/GetCartPrices")
@MultipartConfig
public class GetCartPrices extends HttpServlet {
    private Connection connection;

    @Override
    public void init() throws ServletException {
        this.connection = ConnectionHandler.getConnection(this.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jsonCart;
        Cart cart;
        try {
            jsonCart = new String(Base64.getDecoder().decode(request.getParameter("cart")));

            Map<Integer, Map<Integer, Integer>> cartContents = new HashMap<>();
            JsonObject cartObj = JsonParser.parseString(jsonCart).getAsJsonObject();
            for(String supplierCodeString : cartObj.keySet()) {
                int supplierCode = Integer.parseInt(supplierCodeString);
                JsonObject supplierProductsObj = cartObj.getAsJsonObject(supplierCodeString);
                HashMap<Integer, Integer> productQuantities = new HashMap<>();
                for(String productCodeString : supplierProductsObj.keySet()) {
                    int productCode = Integer.parseInt(productCodeString);
                    int quantity = supplierProductsObj.get(productCodeString).getAsInt();
                    productQuantities.put(productCode, quantity);
                }
                cartContents.put(supplierCode, productQuantities);
            }
            cart = new Cart(cartContents);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Malformed cart parameter!");
            return;
        }

        JsonObject resp = new JsonObject();
        Map<String, Map<Product, Integer>> mapWithNames;
        Map<String, Float> productsTotals;
        Map<String, Float> shippingCosts;
        try {
            mapWithNames = cart.findAllProducts(connection);
            productsTotals = cart.findAllProductTotals(connection);
            shippingCosts = cart.getAllShippingCosts(connection);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Db error!");
            return;
        }

        for(String supplierName : mapWithNames.keySet()) {
            JsonObject supplierValue = new JsonObject();
            JsonArray products = new JsonArray();

            for(Map.Entry<Product, Integer> entry : mapWithNames.get(supplierName).entrySet()) {
                JsonObject prod = new JsonObject();
                prod.addProperty("name", entry.getKey().getName());
                prod.addProperty("quantity", entry.getValue());
                products.add(prod);
            }
            supplierValue.add("products", products);

            supplierValue.addProperty("productsTotal", productsTotals.get(supplierName));
            supplierValue.addProperty("shippingTotal", shippingCosts.get(supplierName));

            resp.add(supplierName, supplierValue);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(resp.toString());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(this.connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
