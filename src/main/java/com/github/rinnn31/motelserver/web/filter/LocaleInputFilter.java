package com.github.rinnn31.motelserver.web.filter;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LocaleInputFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                
        String acceptLanguage = request.getHeader("Accept-Language");
        Locale locale = Locale.forLanguageTag("vi-VN"); // Default to Vietnamese if no Accept-Language header is present or if it doesn't contain supported languages.
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            String[] locales = acceptLanguage.split(",");
            // Only accept english and vietnamese.
            for (String loc : locales) {
                if (loc.trim().startsWith("en")) {
                    locale = Locale.forLanguageTag("en-US");
                    break;
                }
            }
        }
        LocaleContextHolder.setLocale(locale);

        filterChain.doFilter(request, response);
    }
    
}
