package com.example.livestream_apd.infrastructure.security;


import com.example.livestream_apd.domain.entity.User;
import com.example.livestream_apd.domain.repository.UserRepository;
import com.example.livestream_apd.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(User.class) &&
                parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        log.info("CurrentUserMethodArgumentResolver.resolveArgument called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("No authentication found in SecurityContext");
            return null;
        }

        log.info("Authentication found: {}, Principal: {}",
                authentication.isAuthenticated(),
                authentication.getPrincipal().getClass().getName());

        if (authentication.getPrincipal() instanceof UserDetailsServiceImpl.UserPrincipal userPrincipal) {
            String email = userPrincipal.getEmail();
            log.info("Attempting to load User entity for email: {}", email);

            return userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.warn("User not found for email: {}", email);
                        return null;
                    });
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            log.info("Attempting to load User entity for username: {}", username);

            return userRepository.findByEmail(username)
                    .orElseGet(() -> {
                        log.warn("User not found for username: {}", username);
                        return null;
                    });
        }

        log.warn("Principal is not an instance of UserDetails or UserPrincipal: {}",
                authentication.getPrincipal().getClass().getName());
        return null;
    }
}
