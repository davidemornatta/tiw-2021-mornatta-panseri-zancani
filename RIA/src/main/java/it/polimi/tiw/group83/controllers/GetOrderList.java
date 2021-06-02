package it.polimi.tiw.group83.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.group83.beans.Order;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.OrderDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;

import javax.servlet.ServletContext;
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

@WebServlet("/GoToOrders")
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
        Map<Order,Map<Product, Integer>> orderProducts = new HashMap<>();


        try {
            orders = orderDAO.findUserOrders(user.getId());
            for(Order o: orders){
                try {
                    products = orderDAO.findAllProductsInOrder(o.getCode());
                    orderProducts.put(o,products);
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


        String json = new Gson().toJson(orderProducts);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
