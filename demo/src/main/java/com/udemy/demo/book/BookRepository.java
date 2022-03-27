package com.udemy.demo.book;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CrudRepository<Book, Integer>{
    
    List<Book> findByBookStatusAndUserIdNotAndDeletedFalse(BookStatus bookStatus, Integer userId);

    List<Book> findByUserIdAndDeletedFalse(Integer id);
}
