package com.rahnemacollege.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "Users")
@Embeddable
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    private String name;
    //    @Email(message = "Invalid email address.")
    @Column(name = "email", unique = true)
    private String email;
    @JsonIgnore
    private String password;
    private String picture;


    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
//    @RestResource(exported = false)
    @JoinTable(name = "users_bookmarks",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "bookmarks_id", referencedColumnName = "id"))
    private Set<Auction> bookmarks = new HashSet<Auction>();

    @OneToOne(mappedBy = "user")
    private ResetRequest resetRequest;

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.picture = null;
        this.bookmarks = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, picture, bookmarks, resetRequest);
    }
}
