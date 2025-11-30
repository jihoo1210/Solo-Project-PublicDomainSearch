package com.example.backend.entity.user;

import com.example.backend.entity.book.FavoriteBook;
import com.example.backend.entity.book.RecentBook;
import com.example.backend.entity.user.enumeration.AuthProvider;
import com.example.backend.entity.user.enumeration.Role;
import com.example.backend.entity.utility.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

@Entity(name = "USERS")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteBook> favoriteBookList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentBook> recentBookList;

    public static User createUser(AuthProvider authProvider, String email, String username, String password) {
        return User.builder()
                .authProvider(authProvider)
                .email(email)
                .username(username)
                .password(password)
                .role(Role.ROLE_USER)
                .build();
    }
}
