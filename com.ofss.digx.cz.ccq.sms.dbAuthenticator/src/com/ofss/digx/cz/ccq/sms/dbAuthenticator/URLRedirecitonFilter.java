package com.ofss.digx.cz.ccq.sms.dbAuthenticator;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import weblogic.logging.NonCatalogLogger;

public class URLRedirecitonFilter implements Filter {
  private static NonCatalogLogger a_oLogger = new NonCatalogLogger("Security.Authentication");
  
  public void destroy() {
    a_oLogger.info("URLRedirecitonFilter.destroy");
  }
  
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    a_oLogger.debug("Entered URLRedirecitonFilter.doFilter");
    HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
    HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
    a_oLogger.debug("Pre authenticaiton filter URLRedirecitonFilter.doFilter");
    filterChain.doFilter(servletRequest, servletResponse);
    a_oLogger.debug("Post authenticaiton filter URLRedirecitonFilter.doFilter");
    a_oLogger.debug("Exit URLRedirecitonFilter.doFilter");
  }
  
  public void init(FilterConfig filterConfig) throws ServletException {
    a_oLogger.info("URLRedirecitonFilter.init");
  }
}
