package com.shopme.security.oauth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.shopme.common.entity.AuthenticationType;
import com.shopme.common.entity.Customer;
import com.shopme.customer.CustomerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private CustomerService customerService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomerOAuth2User auth2User = (CustomerOAuth2User) authentication.getPrincipal();

        String name = auth2User.getName();
        String email = auth2User.getEmail();
        String countryCode = request.getLocale().getCountry();
        String clientName = auth2User.getClientName();

        String loginName = auth2User.getLogin();
        String fullName = auth2User.getFullName();
        Customer customer1 = customerService.getCustomerByEmail(loginName);

        System.out.println("OAuth2LoginSuccessHandler: " + name + " | " + email);
        System.out.println(auth2User.getClientName());

        AuthenticationType authenticationType = getAuthenticationType(clientName);
        Customer customer = customerService.getCustomerByEmail(email);
        if (clientName.equals("GitHub")) {
            if (customer1 == null) {
                System.out.println(
                        "OAuth2LoginSuccessHandler: " + name + " | " + loginName + " | " + fullName
                                + " | " + authenticationType);
                customerService.addNewCustomerUponOAuthLoginGithub(loginName, fullName, countryCode,
                        authenticationType);

            } else {
                customerService.updateAuthenticationType(customer1, authenticationType);
            }
        } else {
            if (customer == null) {
                customerService.addNewCustomerUponOAuthLogin(name, email, countryCode,
                        authenticationType);
            } else {
                auth2User.setFullName(customer.getFullName());
                customerService.updateAuthenticationType(customer, authenticationType);
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private AuthenticationType getAuthenticationType(String clientName) {
        if (clientName.equals("Google")) {
            return AuthenticationType.GOOGLE;
        } else if (clientName.equals("GitHub")) {
            return AuthenticationType.GITHUB;
        } else {
            return AuthenticationType.DATABASE;
        }

    }

}
