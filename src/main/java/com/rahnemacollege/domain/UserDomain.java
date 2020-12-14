package com.rahnemacollege.domain;

import com.rahnemacollege.model.User;
import lombok.Data;
import org.springframework.data.rest.core.annotation.RestResource;


@Data
public class UserDomain {

    private String name;
    private String email;
    @RestResource(exported = false)
    private String picture;


    public UserDomain(String name,
                      String email,
                      String picture) {
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    public UserDomain() {
    }

    public UserDomain(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
