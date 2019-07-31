/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.ocs.oam.reportserver.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author X380
 */
public abstract class Task implements Job {
   
    protected abstract void query();
    protected abstract void saveToMySQL();
    protected abstract void sendEmail();
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        TaskConfig config = (TaskConfig) context.getMergedJobDataMap().get("config");
        this.query();
        this.saveToMySQL();
        if (config.getSendEmail()) this.sendEmail();
    }
}
