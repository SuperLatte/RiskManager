package com.devops.dao;

import com.devops.entity.User;

import java.sql.SQLException;

/**
 * Created by super on 2016/11/6.
 */
public interface UserDao {

    public User getUser(String username, String password) throws SQLException;

    public User getUser(String uid) throws SQLException;

}
