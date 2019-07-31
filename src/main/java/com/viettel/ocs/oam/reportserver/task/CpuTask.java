/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.ocs.oam.reportserver.task;

import org.apache.log4j.Logger;

/**
 *
 * @author X380
 */
public class CpuTask extends Task {
    private final static Logger logger = Logger.getLogger(CpuTask.class);
    
    @Override
    protected void query() {
        if(logger.isInfoEnabled()){
            logger.info("Query CPU information");
        }
    }

    @Override
    protected void saveToMySQL() {
        if(logger.isInfoEnabled()){
            logger.info("Save CPU information to MySQL");
        }
    }

    @Override
    protected void sendEmail() {
        if(logger.isInfoEnabled()){
            logger.info("Send CPU information through email");
        }
    }
}
