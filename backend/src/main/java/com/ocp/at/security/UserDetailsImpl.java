package com.ocp.at.security;

import com.ocp.at.entity.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private String id;
    private String matricule;
    private String email;
    private String motDePasse;
    private boolean actif;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(Utilisateur utilisateur) {
        // Collect roles and permissions
        List<GrantedAuthority> authorities = utilisateur.getRoles() == null
                ? java.util.Collections.emptyList()
                : utilisateur.getRoles().stream()
                .flatMap(role -> {
                    Stream<GrantedAuthority> roleAuth = Stream.of(new SimpleGrantedAuthority(role.getNom()));
                    Stream<GrantedAuthority> permAuth = (role.getPermissions() == null
                            ? Stream.<GrantedAuthority>empty()
                            : role.getPermissions().stream()
                                .filter(p -> p != null && p.getNom() != null)
                                .map(p -> new SimpleGrantedAuthority(p.getNom())));
                    return Stream.concat(roleAuth, permAuth);
                })
                .distinct()
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                utilisateur.getId(),
                utilisateur.getMatricule(),
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                utilisateur.isActif(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email; // On utilise l'email comme identifiant de connexion
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return actif;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}
