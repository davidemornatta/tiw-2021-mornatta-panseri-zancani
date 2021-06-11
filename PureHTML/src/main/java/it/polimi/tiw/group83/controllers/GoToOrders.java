package it.polimi.tiw.group83.controllers;

import it.polimi.tiw.group83.beans.Order;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.OrderDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;
import it.polimi.tiw.group83.utils.TemplateUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

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
public class GoToOrders extends HttpServlet {
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        templateEngine = TemplateUtils.initTemplateEngine(getServletContext());
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
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to find products");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to find orders");
            return;
        }

        String path = "/WEB-INF/Orders.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("orders", orders);
        ctx.setVariable("orderProducts", orderProducts);
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
