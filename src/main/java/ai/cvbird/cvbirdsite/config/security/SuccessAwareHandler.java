package ai.cvbird.cvbirdsite.config.security;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class SuccessAwareHandler extends SavedRequestAwareAuthenticationSuccessHandler {
}
