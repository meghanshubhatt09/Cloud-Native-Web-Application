package com.webservices.cloudwebapp.webapp.repository;


import com.webservices.cloudwebapp.webapp.model.Document;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<Document, UUID> {

    // boolean delete(UUID doc_id);
    @Query("SELECT a FROM Document a WHERE a.doc_id = :doc_id")
    Document findDocumentByDoc_id(UUID doc_id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Document e WHERE e.doc_id = :doc_id")
    void deleteByDoc_id(UUID doc_id);
//
//    List<Document> deleteDocumentByDoc_id(UUID doc_id);

}
