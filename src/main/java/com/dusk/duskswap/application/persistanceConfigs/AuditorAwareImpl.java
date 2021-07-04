package com.dusk.duskswap.application.persistanceConfigs;

import com.dusk.duskswap.usersManagement.models.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //System.out.println("AUTH >>>>>>>>>> " + authentication + "     IS? = "+ (authentication instanceof ));
        if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("APPLICATION_DEFAULT_USER");
        }
        String idString = ((UserDetailsImpl)authentication.getPrincipal()).getId().toString();//.getUsername();
        return Optional.of(idString);
    }
}
