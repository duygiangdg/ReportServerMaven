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
public class RamTask extends Task {
    final static Logger logger = Logger.getLogger(RamTask.class);

    @Override
    protected void query() {
        if(logger.isInfoEnabled()){
            logger.info("Query RAM information");
        }
    }

    @Override
    protected void saveToMySQL() {
        if(logger.isInfoEnabled()){
            logger.info("Save RAM information to MySQL");
        }
    }

    @Override
    protected void sendEmail() {
        if(logger.isInfoEnabled()){
            logger.info("Send RAM information through email");
        }
    }
}
