package boraldan.account.controller.feign.component;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakFeignInterceptor implements RequestInterceptor {


    private final OAuth2AuthorizedClientManager authorizedClientManager;

//    //// конфигурация для варианта   authorization-grant-type: client_credentials
//    @Override
//    public void apply(RequestTemplate template) {
//
//        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
//                .withClientRegistrationId("account-telros")
//                .principal("account-telros")
//                .build());
//
//        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
//
//            template.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
//        }
//    }


//// конфигурация для варианта   authorization-grant-type: authorization_code
    @Override
    public void apply(RequestTemplate template) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                .principal(authentication)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            template.header("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        }
    }


}
