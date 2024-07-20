package com.wallhack.clouddrive.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class UsersPOJO{
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private Long id;

        @NotNull
        @NotEmpty
        @Column(unique = true, nullable = false)
        private String username;

        @NotNull
        @NotEmpty
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
