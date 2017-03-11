package edu.hneu.studentsportal.config

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.web.filter.CompositeFilter

import javax.annotation.Resource
import javax.servlet.Filter

@Configuration
@EnableOAuth2Client
@EnableAuthorizationServer
class SocialConfig {

    @Resource
    OAuth2ClientContext oauth2ClientContext
    @Resource
    AuthoritiesExtractor authoritiesExtractor

    @Bean
    FilterRegistrationBean oauth2ClientFilterRegistration(final OAuth2ClientContextFilter filter) {
        new FilterRegistrationBean(
                filter: filter,
                order: -100
        )
    }

    @Bean
    @ConfigurationProperties('google')
    ClientResources google() {
        new ClientResources()
    }

    @Bean
    Filter ssoFilter() {
        new CompositeFilter(
                filters: [oAuth2ClientAuthenticationProcessingFilter()]
        )
    }

    private Filter oAuth2ClientAuthenticationProcessingFilter() {
        def clientAuthenticationProcessingFilter = new OAuth2ClientAuthenticationProcessingFilter('/connect/google')
        clientAuthenticationProcessingFilter.setRestTemplate(oAuth2RestTemplate())
        clientAuthenticationProcessingFilter.setTokenServices(userInfoTokenServices())
        clientAuthenticationProcessingFilter
    }

    @Bean
    UserInfoTokenServices userInfoTokenServices() {
        def tokenServices = new UserInfoTokenServices(google().getResource().getUserInfoUri(), google().getClient().getClientId());
        tokenServices.setAuthoritiesExtractor(authoritiesExtractor)
        tokenServices.setRestTemplate(oAuth2RestTemplate())
        tokenServices
    }

    @Bean
    OAuth2RestTemplate oAuth2RestTemplate() {
        new OAuth2RestTemplate(google().getClient(), oauth2ClientContext)
    }


    private class ClientResources {

        @NestedConfigurationProperty
        AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails()

        @NestedConfigurationProperty
        ResourceServerProperties resource = new ResourceServerProperties()
    }

}

