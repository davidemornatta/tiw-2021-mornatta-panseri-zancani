package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.filters;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Cart;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CartChecker implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        HttpSession session = req.getSession();
        if(session.getAttribute("cart") == null) {
            Cart cart = new Cart();
            session.setAttribute("cart", cart);
        }

        //TODO Check if cart has invalid items

        // pass the request along the filter chain
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
