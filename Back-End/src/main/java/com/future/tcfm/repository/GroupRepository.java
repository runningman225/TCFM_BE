package com.future.tcfm.repository;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    int countAllByNameAndActive(String name,Boolean bool);
    List<Group> findAllByActive(Boolean active);
    Group findByName(String name);
    Group findByIdGroup(String id);
    Group findByNameAndActive(String id,boolean bool);

}
