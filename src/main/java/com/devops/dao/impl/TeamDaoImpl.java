package com.devops.dao.impl;

import com.devops.dao.TeamDao;
import com.devops.entity.Team;
import com.devops.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.devops.utils.ResultSetTranUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by super on 2016/11/6.
 */
@Repository
public class TeamDaoImpl implements TeamDao{

    @Autowired
    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    /**
     * 
     * @throws SQLException
     */
    public TeamDaoImpl() throws SQLException {
        //do nothing because of autowiring
    }

    @Override
    public Team getTeamByTeamID(String tid) throws SQLException {

        preparedStatement = connection.prepareStatement("select * from team where tid=?");
        preparedStatement.setInt(1, Integer.parseInt(tid));
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            Team team = ResultSetTranUtil.tranTeam(resultSet);
            preparedStatement.close();
            return team;
        }
        return null;
    }

    @Override
    public Team getTeamByManagerID(String uid) throws SQLException {

        preparedStatement = connection.prepareStatement("select * from team where manager_id=?");
        preparedStatement.setInt(1, Integer.parseInt(uid));
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            Team team = ResultSetTranUtil.tranTeam(resultSet);
            preparedStatement.close();
            return team;
        }
        return null;
    }

    @Override
    public List<User> getTeamMembers(String tid) throws SQLException {

        preparedStatement = connection.prepareStatement("SELECT u.* FROM user u, team_relationship r where r.uid=u.uid and r.tid=?");
        preparedStatement.setInt(1, Integer.parseInt(tid));
        resultSet = preparedStatement.executeQuery();

        List<User> user = new ArrayList<>();
        while (resultSet.next()) {
            user.add(ResultSetTranUtil.tranUser(resultSet));
        }
        preparedStatement.close();
        return user;
    }

}
