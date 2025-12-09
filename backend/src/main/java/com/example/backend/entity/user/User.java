package com.example.backend.entity.user;

import com.example.backend.entity.book.FavoriteBook;
import com.example.backend.entity.book.RecentBook;
import com.example.backend.entity.user.enumeration.AuthProvider;
import com.example.backend.entity.user.enumeration.Language;
import com.example.backend.entity.user.enumeration.Role;
import com.example.backend.entity.utility.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter

@Entity
@Table(name = "USERS")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Setter
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteBook> favoriteBookList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecentBook> recentBookList = new ArrayList<>();

    public static User createUser(AuthProvider authProvider, String email, String username, String password, Language language) {
        return User.builder()
                .authProvider(authProvider)
                .email(email)
                .username(username)
                .password(password)
                .role(Role.ROLE_USER)
                .language(language)
                .build();
    }

}
