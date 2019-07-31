/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.ocs.oam.reportserver;
        
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.viettel.ocs.oam.reportserver.task.TaskClassLoader;
import com.viettel.ocs.oam.reportserver.task.TaskConfig;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author giangnd13
 */
public class Main {
    final static Logger logger = Logger.getLogger(Main.class);
    final static String CONFIG_FILE = "src/main/resources/config.xml";
    
    public static void main(String[] args) {
        try {
            List<TaskConfig> configs = readTaskConfigs(CONFIG_FILE);
            Map<String, JobDetail> jobMap = startJobs(configs);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
    
    private static List<TaskConfig> readTaskConfigs(String configFile) throws 
            ParserConfigurationException, SAXException, IOException, JAXBException {
        
        List<TaskConfig> configs = new ArrayList<>();
        File fXmlFile = new File(configFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        doc.getDocumentElement().normalize();
        NodeList taskNodeList = doc.getElementsByTagName("task");
        
        JAXBContext jaxbContext = JAXBContext.newInstance(TaskConfig.class);             
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        for (int idx = 0; idx < taskNodeList.getLength(); idx++) {
            Node taskNode = taskNodeList.item(idx);
            TaskConfig config = (TaskConfig) jaxbUnmarshaller.unmarshal(taskNode);
            configs.add(config);
        }
        return configs;
    }
    
    private static Map<String, JobDetail> startJobs(List<TaskConfig> configs) throws 
            ClassNotFoundException, SchedulerException {
        
        TaskClassLoader classLoader = new TaskClassLoader();
        Map<String, JobDetail> jobMap = new HashMap();
        
        for (TaskConfig config : configs) {
            Class taskClass = classLoader.loadClass(config.getClassName());
            JobDetail job = JobBuilder.newJob(taskClass)
                    .withIdentity(config.getTaskName(), config.getTaskName() + "Group").build();
            job.getJobDataMap().put("config", config);

            Trigger jobTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(config.getTaskName() + "Trigger", config.getTaskName() + "Group")
                    .withSchedule(CronScheduleBuilder.cronSchedule(config.getCronTab()))
                    .build();

            Scheduler cpuJobScheduler = new StdSchedulerFactory().getScheduler();
            cpuJobScheduler.start();
            cpuJobScheduler.scheduleJob(job, jobTrigger);
            
            jobMap.put(config.getTaskName(), job);
        }
        
        return jobMap;
    }
}
