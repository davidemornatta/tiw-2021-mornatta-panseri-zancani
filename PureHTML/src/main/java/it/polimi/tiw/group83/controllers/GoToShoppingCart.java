package it.polimi.tiw.group83.controllers;

import it.polimi.tiw.group83.beans.Cart;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.utils.ConnectionHandler;
import it.polimi.tiw.group83.utils.TemplateUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

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
import java.util.Map;
import java.util.Optional;

@WebServlet("/GoToShoppingCart")
public class GoToShoppingCart extends HttpServlet {
    private Connection connection;
    private TemplateEngine templateEngine;

    public GoToShoppingCart() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        templateEngine = TemplateUtils.initTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        Cart cart = (Cart) session.getAttribute("cart");
        Map<String, Integer> supplierCodes;
        try {
            supplierCodes = cart.getAllSupplierCodes(connection);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during db query");
            return;
        }
        Map<String, Map<Product, Integer>> supplierProducts;
        try {
            supplierProducts = cart.findAllProducts(connection);
        } catch (IOException | SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during db query");
            return;
        }
        Map<String, Integer> supplierQuantities = new HashMap<>();
        for (String supplier : supplierProducts.keySet()) {
            Optional<Integer> quantity = supplierProducts.get(supplier).values().stream().reduce(Integer::sum);
            supplierQuantities.put(supplier, quantity.orElse(-1));
        }

        Map<String, Float> supplierTotals;
        try {
            supplierTotals = cart.findAllProductTotals(connection);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during db query");
            return;
        }

        Map<String, Float> supplierShippings;
        try {
            supplierShippings = cart.getAllShippingCosts(connection);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during db query");
            return;
        }


        WebContext ctx = new WebContext(req, resp, getServletContext());
        ctx.setVariable("supplierCodes", supplierCodes);
        ctx.setVariable("supplierProducts", supplierProducts);
        ctx.setVariable("supplierQuantities", supplierQuantities);
        ctx.setVariable("supplierTotals", supplierTotals);
        ctx.setVariable("supplierShippings", supplierShippings);
        String path = "WEB-INF/cart.html";
        templateEngine.process(path, ctx, resp.getWriter());
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
