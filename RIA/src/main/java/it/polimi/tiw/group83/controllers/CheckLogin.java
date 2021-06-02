package it.polimi.tiw.group83.controllers;

import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.UserDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
    private Connection connection = null;

    public CheckLogin() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // obtain and escape params
        String mail;
        String pwd;
        try {
            mail = StringEscapeUtils.escapeJava(request.getParameter("mail"));
            pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));

            if (mail == null || pwd == null || mail.isEmpty() || pwd.isEmpty()) {
                throw new Exception("Missing or empty credential value");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing credential value");
            return;
        }

        // query db to authenticate for user
        UserDAO userDao = new UserDAO(connection);
        User user;
        try {
            user = userDao.checkCredentials(mail, pwd);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not Possible to check credentials");
            return;
        }

        // If the user exists, add info to the session and go to home page, otherwise
        // show login page with error message
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Incorrect mail or password");
        } else {
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(user.getName() + " " + user.getSurname());
        }

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