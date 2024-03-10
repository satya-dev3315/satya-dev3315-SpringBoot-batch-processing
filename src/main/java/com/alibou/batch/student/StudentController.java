package com.alibou.batch.student;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final JobLauncher jobLauncher;
    private final Job job;
    
    @PostMapping("/student")
    public void loadCsvToDatabase() throws Exception{
    	
    // we r launching a job inside this method.For that i need a job launcher.
    //when this job is started
    JobParameters jobParameters = new JobParametersBuilder().
    		addLong("start-at", System.currentTimeMillis()).
    		toJobParameters();
   
    jobLauncher.run(job, jobParameters); //jobLauncher launch our job with param(it gives info at when job is starting)
    }
    
    
    
   
}
