package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.controllers;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Cart;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.ConnectionHandler;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.TemplateUtils;
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

@WebServlet("/ProcessOrder")
public class ProcessOrder extends HttpServlet {
    private Connection connection;
    private TemplateEngine templateEngine;

    public ProcessOrder() {
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
        if(session == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No session found");
            return;
        }
        if(session.getAttribute("cart") == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No cart in session");
            return;
        }

        Cart cart = (Cart) session.getAttribute("cart");



//        WebContext ctx = new WebContext(req, resp, getServletContext());
//        ctx.setVariable("supplierProducts", supplierProducts);
//        ctx.setVariable("supplierTotals", supplierTotals);
//        String path = "WEB-INF/cart.html";
//        templateEngine.process(path, ctx, resp.getWriter());
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
