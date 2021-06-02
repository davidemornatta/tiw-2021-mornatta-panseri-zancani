package it.polimi.tiw.group83.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.ProductDAO;
import it.polimi.tiw.group83.dao.UserDAO;
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
import java.util.List;

@WebServlet("/GetRecentlyViewedList")
public class GetRecentlyViewedList extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetRecentlyViewedList() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        UserDAO userDAO = new UserDAO(connection);
        List<Product> lastViewed;

        try {
            lastViewed = userDAO.findLastFiveViewedBy(user.getId());
            if(lastViewed.size() < 5) {
                ProductDAO productDAO = new ProductDAO(connection);
                lastViewed.addAll(productDAO.findRandomProducts(5 - lastViewed.size()));
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to recover last five viewed products");
            return;
        }

        String json = new Gson().toJson(lastViewed);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);


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

