package com.rahnemacollege.repository;

import com.rahnemacollege.model.Bid;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BidRepository extends CrudRepository<Bid, Integer> {

    Optional<Bid> findTopByAuction_idOrderByIdDesc(Integer auctionId);
}
