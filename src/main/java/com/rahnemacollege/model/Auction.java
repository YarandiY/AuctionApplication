package com.rahnemacollege.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table(name = "Auctions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Auction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;
    private String title;
    private String description;
    @Column(name = "base_price")
    private long basePrice;
    @ManyToOne
    @JoinColumn(name = "category_id")
    @RestResource(exported = false)
    private Category category;
    private Date date;
    private int state = 0;
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private User winner;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @Column(name = "max_number")
    private int maxNumber;

    @Override
    public boolean equals(Object o) {
        if (o instanceof Auction)
            if (((Auction) o).id == id)
                return true;
        return false;
    }


    public Auction() {
    }

    public Auction(String title, String description, long base_price, Category category, Date date, User owner,
                   int max_number) {
        this.title = title;
        this.description = description;
        this.basePrice = base_price;
        this.date = date;
        this.category = category;
        this.state = 0;
        this.winner = null;
        this.owner = owner;
        this.maxNumber = max_number;
    }


}
