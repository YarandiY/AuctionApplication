package com.rahnemacollege.model;

import lombok.Data;
import org.springframework.data.rest.core.annotation.RestResource;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(name = "Pictures")
public class Picture {


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "filename")
    private String fileName;
    @NotNull(message = "Invalid date.")
    private Date date;

    @RestResource(exported = false)
    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    public Picture(){}

    public Picture(String fileName,Auction auction){
        this.fileName = fileName;
        this.auction = auction;
        date = new Date(new java.util.Date().getTime());
    }



}
