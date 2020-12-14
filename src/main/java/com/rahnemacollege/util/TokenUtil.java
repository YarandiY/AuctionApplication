package com.rahnemacollege.util;


import java.io.Serializable;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;

public interface    TokenUtil extends Serializable {

    /**
     * retrieve username from given token
     *
     * @param token
     * @return
     */
    String getIdFromToken(String token);

    /**
     * retrieve expiration date from token
     *
     * @param token
     * @return
     */
    Date getExpirationDateFromToken(String token);

    /**
     * generate token for user
     *
     * @param userDetails
     * @return
     */
    String generateToken(UserDetails userDetails);

    /**
     * validate token
     *
     * @param token
     * @param userDetails
     * @return
     */
    Boolean validateToken(String token, UserDetails userDetails);
}