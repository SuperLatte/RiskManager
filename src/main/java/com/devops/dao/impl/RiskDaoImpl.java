package com.devops.dao.impl;

import com.devops.dao.RiskDao;
import com.devops.entity.Risk;
import com.devops.entity.RiskRecord;
import com.devops.entity.RiskTracing;
import com.devops.utils.ResultSetTranUtil;
import com.devops.utils.TimeGetter;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by super on 2016/11/6.
 */
@Repository
public class RiskDaoImpl implements RiskDao {

    @Autowired
    private Connection connection;
    private ResultSet resultSet;
    private Statement statement;
    private PreparedStatement preparedStatement;

    /**
     * 
     * @throws SQLException
     */
    public RiskDaoImpl() throws SQLException {
        //do nothing because of autowiring
    }

    @Override
    public Risk getRiskByRiskID(String rid) throws SQLException {

        preparedStatement = connection.prepareStatement("SELECT * FROM risk WHERE rid=?");
        preparedStatement.setInt(1, Integer.parseInt(rid));
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            Risk risk = ResultSetTranUtil.tranRisk(resultSet);
            preparedStatement.close();
            return risk;
        }
        return null;
    }

    @Override
    public List<Risk> getRiskByTeamID(String tid) throws SQLException {

        preparedStatement = connection.prepareStatement("SELECT * FROM risk WHERE tid=?");
        preparedStatement.setInt(1, Integer.parseInt(tid));
        resultSet = preparedStatement.executeQuery();

        List<Risk> risks = new ArrayList<>();
        while (resultSet.next()) {
            risks.add(ResultSetTranUtil.tranRisk(resultSet));
        }
        preparedStatement.close();
        return risks;
    }

    @Override
    public List<Risk> getRiskByUserID(String uid) throws SQLException {
        List<Risk> risks = new ArrayList<>();

        preparedStatement = connection.prepareStatement("SELECT level FROM user WHERE uid=?");
        preparedStatement.setInt(1, Integer.parseInt(uid));
        resultSet = preparedStatement.executeQuery();

        resultSet.next();
        int level = resultSet.getInt(1);
        if (level == 0) {

            preparedStatement = connection.prepareStatement("select r.* from risk r, risk_tracing t where r.rid = t.rid and t.uid =?");
            preparedStatement.setInt(1, Integer.parseInt(uid));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                risks.add(ResultSetTranUtil.tranRisk(resultSet));
            }
        } else if (level == 1) {

            preparedStatement = connection.prepareStatement("select r.* from risk r, team t where t.tid=r.tid and t.manager_id=?");
            preparedStatement.setInt(1, Integer.parseInt(uid));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                risks.add(ResultSetTranUtil.tranRisk(resultSet));
            }
        }
        preparedStatement.close();
        return risks;
    }

    @Override
    public List<RiskRecord> getRecords(String rid) throws SQLException {

        preparedStatement = connection.prepareStatement("select * from risk_record where rid=?");
        preparedStatement.setInt(1, Integer.parseInt(rid));
        resultSet = preparedStatement.executeQuery();

        List<RiskRecord> riskRecords = new ArrayList<>();
        while (resultSet.next()) {
            riskRecords.add(ResultSetTranUtil.tranRiskRecord(resultSet));
        }
        preparedStatement.close();
        return riskRecords;
    }

    @Override
    public RiskRecord getRecord(String rrid) throws SQLException {

        preparedStatement = connection.prepareStatement("select * from risk_record where rrid=?");
        preparedStatement.setInt(1, Integer.parseInt(rrid));
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            RiskRecord record = ResultSetTranUtil.tranRiskRecord(resultSet);
            preparedStatement.close();
            return record;
        }
        return null;
    }

    @Override
    public int addRecord(RiskRecord riskRecord) throws SQLException {
        int time = TimeGetter.getCurrentTime();
        riskRecord.setCreateTime(time);

        statement = connection.createStatement();
        String sql = "insert into risk_record(rid, createtime, content, possibility, affection, `trigger`, trace_userid) values("
                + riskRecord.getRid() + ","
                + riskRecord.getCreateTime() + ",'"
                + riskRecord.getContent() + "',"
                + riskRecord.getPossibility() + ","
                + riskRecord.getAffection() + ",'"
                + riskRecord.getTrigger() + "',"
                + riskRecord.getTraceUserid() + ")";
        statement.execute(sql);

        String rid = riskRecord.getRid();
        statement.executeUpdate("update risk set updatetime=" + time + " where rid=" + rid);

        resultSet = statement.executeQuery("SELECT MAX(rrid) from risk_record");
        if (resultSet.next()) {
            int rrid = resultSet.getInt(1);
            statement.close();
            return rrid;
        }
        statement.close();
        return -1;
    }

    @Override
    public int addRisk(Risk risk) throws SQLException {
        int time = TimeGetter.getCurrentTime();
        risk.setUpdateTime(time);
        risk.setCreateTime(time);
        risk.setStatus(1);

        statement = connection.createStatement();
        String sql = "insert into risk(tid, name, createtime, updatetime, status, description) values("
                + risk.getTid() + ",'"
                + risk.getName() + "',"
                + risk.getCreateTime() + ","
                + risk.getUpdateTime() + ","
                + risk.getStatus() + ",'"
                + risk.getDescription() + "')";
        statement.execute(sql);
        resultSet = statement.executeQuery("SELECT MAX(rid) from risk");
        if (resultSet.next()) {
            int rid = resultSet.getInt(1);
            statement.close();
            return rid;
        }
        statement.close();
        return -1;
    }

    @Override
    public boolean addTracing(RiskTracing riskTracing) throws SQLException {
        statement = connection.createStatement();
        String sql = "insert into risk_tracing(rid, uid) values("
                + riskTracing.getRid() + ","
                + riskTracing.getUid() + ")";
        boolean result = statement.execute(sql);
        statement.close();
        return result;
    }

    @Override
    public boolean deleteTracing(RiskTracing riskTracing) throws SQLException {
        preparedStatement = connection.prepareStatement("DELETE FROM risk_tracing where rid=? and uid=?");
        preparedStatement.setInt(1, Integer.parseInt(riskTracing.getRid()));
        preparedStatement.setInt(2, Integer.parseInt(riskTracing.getUid()));

        boolean result = preparedStatement.execute();
        preparedStatement.close();
        return result;
    }

    @Override
    public List<RiskTracing> getTracingByRiskID(String rid) throws SQLException {
        preparedStatement = connection.prepareStatement("SELECT * FROM risk_tracing where rid=?");
        preparedStatement.setInt(1, Integer.parseInt(rid));
        resultSet = preparedStatement.executeQuery();

        List<RiskTracing> riskTracings = new ArrayList<>();
        while (resultSet.next()) {
            RiskTracing riskTracing = new RiskTracing();
            riskTracing.setUid(resultSet.getString("uid"));
            riskTracing.setRid(resultSet.getString("rid"));
            riskTracings.add(riskTracing);
        }
        preparedStatement.close();
        return riskTracings;
    }

    @Override
    public boolean editRisk(Risk risk) throws SQLException {
        int time = TimeGetter.getCurrentTime();
        risk.setUpdateTime(time);

        statement = connection.createStatement();
        String sql = "update risk set "
                + "tid=" + risk.getTid() + ","
                + "name='" + risk.getName() + "',"
                + "updatetime=" + risk.getUpdateTime() + ","
                + "status=" + risk.getStatus() + ","
                + "description=" + risk.getDescription() + " "
                + "where rid=" + risk.getRid();
        boolean result = statement.execute(sql);
        statement.close();
        return result;
    }

}
