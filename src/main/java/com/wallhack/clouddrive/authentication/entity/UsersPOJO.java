package com.wallhack.clouddrive.authentication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor @NoArgsConstructor
@Entity @Setter @Getter
@Table(name = "users")
public class UsersPOJO implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private Long id;

        @NotNull
        @NotEmpty
        @Size(min = 3, max = 50)
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

}
