package com.future.tcfm.repository;

import com.future.tcfm.model.Expense;
import com.future.tcfm.service.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends MongoRepository<Expense, String>   {
    Expense findByTitle(String title);
    Expense findTop1ByGroupNameAndStatusOrderByCreatedDateDesc(String groupName,Boolean status);
    Integer countByGroupNameAndStatus(String groupName,Boolean bool);
    List<Expense> findByGroupNameLikeAndGroupCurrentPeriodAndStatus(String groupName,int groupCurrentPeriod,Boolean bool);
    List<Expense> findByGroupNameLikeAndStatusOrderByCreatedDateDesc(String groupName,Boolean bool);
    List<Expense> findByGroupNameLikeOrderByCreatedDateDesc(String groupName);
    Page<Expense> findByGroupNameOrderByCreatedDateDesc(String groupName, Pageable pageable);
    Page<Expense> findByGroupNameContainsAndTitleContainsIgnoreCaseOrderByCreatedDateDesc(String groupName,String title,Pageable pageable);
    Page<Expense> findByGroupNameContainsAndStatusOrderByCreatedDateDesc(String groupName,Boolean status,Pageable pageable);
    Page<Expense> findByGroupNameContainsAndPriceLessThanOrderByPriceDesc(String groupName,double price,Pageable pageable);
    Page<Expense> findByGroupNameContainsAndPriceGreaterThanOrderByPriceDesc(String groupName,double price,Pageable pageable);
    Page<Expense> findByGroupNameContainsAndCreatedDateLessThanEqualOrderByStatus(String groupName, long dateInMillis,Pageable pageable);
    Page<Expense> findByGroupNameContainsAndCreatedDateGreaterThanEqualOrderByStatus(String groupName,long dateInMillis,Pageable pageable);


    @Query("{'?0' : { $regex: '?1', $options: 'i' } }")
    Page<Expense> findAll(
            String field,
            String value,
            Pageable pageable);

    Expense findByIdExpense(String id);
    Expense findTopByGroupNameAndStatusOrderByLastModifiedAtDesc(String gName,Boolean bool);
    List<Expense> findTop10ByGroupNameOrderByCreatedDateDesc(String groupName);

}
