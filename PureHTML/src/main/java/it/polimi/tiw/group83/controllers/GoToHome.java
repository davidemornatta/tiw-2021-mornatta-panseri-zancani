package it.polimi.tiw.group83.controllers;

import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.ProductDAO;
import it.polimi.tiw.group83.dao.UserDAO;
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
import java.util.List;

@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public GoToHome() {
        super();
    }

    @Override
    public void init() throws ServletException {
        templateEngine = TemplateUtils.initTemplateEngine(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // If the user is not logged in (not present in session) redirect to the login
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserDAO userDAO = new UserDAO(connection);
        List<Product> lastViewed;

        try {
            lastViewed = userDAO.findLastFiveViewedBy(user.getId());
            if (lastViewed.size() < 5) {
                ProductDAO productDAO = new ProductDAO(connection);
                lastViewed.addAll(productDAO.findRandomProducts(5 - lastViewed.size()));
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover last five viewed products");
            return;
        }

        // Redirect to the Home page and add last viewed products to the parameters
        String path = "/WEB-INF/Home.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("lastViewed", lastViewed);
        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
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
