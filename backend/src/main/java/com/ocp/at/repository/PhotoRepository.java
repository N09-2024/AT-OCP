package com.ocp.at.repository;

import com.ocp.at.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {

    List<Photo> findByVisitePrealableIdOrderByOrdreAsc(String visitePrealableId);

    int countByVisitePrealableId(String visitePrealableId);
}
