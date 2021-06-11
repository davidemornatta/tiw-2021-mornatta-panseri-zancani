package it.polimi.tiw.group83.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.polimi.tiw.group83.beans.Order;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.OrderDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GetOrderList")
public class GetOrderList extends HttpServlet {
    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        OrderDAO orderDAO = new OrderDAO(connection);
        List<Order> orders;
        Map<Product, Integer> products;
        Map<Order, Map<Product, Integer>> orderProducts = new HashMap<>();


        try {
            orders = orderDAO.findUserOrders(user.getId());
            for (Order o : orders) {
                try {
                    products = orderDAO.findAllProductsInOrder(o.getCode());
                    orderProducts.put(o, products);
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Not possible to find products");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to find orders");
            return;
        }

        JsonObject resp = new JsonObject();

        for (Order order : orderProducts.keySet()) {
            JsonArray prodQuantity = new JsonArray();
            JsonObject orderTot = new JsonObject();

            for (Map.Entry<Product, Integer> entry : orderProducts.get(order).entrySet()) {
                JsonObject prod = new JsonObject();
                prod.addProperty("name", entry.getKey().getName());
                prod.addProperty("quantity", entry.getValue());
                prodQuantity.add(prod);
            }
            orderTot.add("prodQuantity", prodQuantity);

            JsonObject orderDetails = new JsonObject();
            orderDetails.addProperty("orderCode", order.getCode());
            orderDetails.addProperty("suppCode", order.getSupplierCode());
            orderDetails.addProperty("totalAmount", order.getTotalAmount());
            orderDetails.addProperty("shipppingDate", String.valueOf(order.getShippingDate()));
            orderDetails.addProperty("shippingAddress", order.getShippingAddress());

            orderTot.add("orderDetails", orderDetails);
            resp.add(String.valueOf(order.getCode()), orderTot);
        }


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(resp.toString());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
