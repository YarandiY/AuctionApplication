create table resetRequests
(
    user_id INT         NOT NULL AUTO_INCREMENT,
    date    DATE        NOT NULL,
    token   VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id)
        REFERENCES Users (id)
        ON update CASCADE ON delete RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

