package com.wallhack.clouddrive.authentication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class UsersPOJO implements UserDetails {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private Long id;

        @NotNull
        @NotEmpty
        @Column(unique = true, nullable = false)
        private String username;

        @NotNull
        @NotEmpty
        @JsonIgnore
        @Column(nullable = false)
        private String password;

        @Override
        public final boolean equals(Object o) {
                if (this == o) return true;
                if (o == null) return false;
                Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o)
                        .getHibernateLazyInitializer()
                        .getPersistentClass() : o.getClass();
                Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this)
                        .getHibernateLazyInitializer()
                        .getPersistentClass() : this.getClass();
                if (oEffectiveClass != thisEffectiveClass) return false;
                UsersPOJO user = (UsersPOJO) o;
                return getId() != null && Objects.equals(getId(), user.getId());
        }

        @Override
        public final int hashCode() {
                return this instanceof HibernateProxy ? ((HibernateProxy) this)
                        .getHibernateLazyInitializer().getPersistentClass()
                        .hashCode() : this.getClass().hashCode();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
        }

        @Override
        public boolean isAccountNonExpired() {
                return UserDetails.super.isAccountNonExpired();
        }

        @Override
        public boolean isAccountNonLocked() {
                return UserDetails.super.isAccountNonLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
                return UserDetails.super.isCredentialsNonExpired();
        }

        @Override
        public boolean isEnabled() {
                return UserDetails.super.isEnabled();
        }

}
