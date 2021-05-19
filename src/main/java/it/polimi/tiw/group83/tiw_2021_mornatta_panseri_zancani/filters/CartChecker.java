package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.filters;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Cart;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.ConnectionHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class CartChecker implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        HttpSession session = req.getSession();
        Object cart = session.getAttribute("cart");
        if(cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        } else {
            //Check if cart has invalid items
            Cart existingCart = (Cart) cart;
            try(Connection con = ConnectionHandler.getConnection(servletRequest.getServletContext())) {
                existingCart.checkValidity(con);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // pass the request along the filter chain
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
