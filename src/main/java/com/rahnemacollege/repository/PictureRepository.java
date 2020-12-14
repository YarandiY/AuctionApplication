package com.rahnemacollege.repository;

import com.rahnemacollege.model.Picture;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface PictureRepository extends CrudRepository<Picture, Integer> {

    @Transactional
    void deleteByAuction_id(int auction_id);

}
