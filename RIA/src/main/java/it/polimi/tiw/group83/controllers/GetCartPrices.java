package it.polimi.tiw.group83.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
        Cart cart;
        try {
            cart = Cart.loadFromBase64(request.getParameter("cart"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Malformed cart parameter!");
            return;
        }

        try {
            if (!cart.checkValidity(connection)) {
                response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                response.getWriter().println(Base64.getEncoder().encodeToString(
                        new Gson().toJsonTree(cart).getAsJsonObject().get("supplierProductsMap").toString().getBytes()));
                return;
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Unable to check cart validity");
            return;
        }

        JsonObject resp = new JsonObject();
        Map<String, Map<Product, Integer>> mapWithNames;
        Map<String, Integer> supplierCodes;
        Map<String, Float> productsTotals;
        Map<String, Float> shippingCosts;
        try {
            mapWithNames = cart.findAllProducts(connection);
            supplierCodes = cart.getAllSupplierCodes(connection);
            productsTotals = cart.findAllProductTotals(connection);
            shippingCosts = cart.getAllShippingCosts(connection);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Db error!");
            return;
        }

        for (String supplierName : mapWithNames.keySet()) {
            JsonObject supplierValue = new JsonObject();

            supplierValue.addProperty("code", supplierCodes.get(supplierName));

            JsonArray products = new JsonArray();
            for (Map.Entry<Product, Integer> entry : mapWithNames.get(supplierName).entrySet()) {
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
