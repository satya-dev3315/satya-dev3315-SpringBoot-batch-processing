package com.alibou.batch.config;

import com.alibou.batch.student.Student;
import com.alibou.batch.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;

@SuppressWarnings("unused")   //we have some unused imports above so write this annotation
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

	//when we write requiredArgContructor, injection will b con injecttion, so @autowired is nt required
	
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final StudentRepository repository;

	//create reader
    
    // 1st way to create reader.We require lineMapper() method too with this
    
    //This FlatFileItemReader will read the csv data n give data in form of student obj.This is item reader

//    @Bean
//    public FlatFileItemReader<Student> reader() {
//        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
//        itemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));    //path where ur csv file is there
//        itemReader.setName("csvReader");                                                       //name of ur item-reader
//        itemReader.setLinesToSkip(1);       //in csv first row is having headers line id,age etc, we dont want this to save into db
//        itemReader.setLineMapper(lineMapper()); // tells that each line in csv belongs to one student object
//        return itemReader;
//    }
    
    //used to read each line n give data in form of lineMapper obj

//  private LineMapper<Student> lineMapper() {
//      DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
//
//      DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
//      lineTokenizer.setDelimiter(","); // i am using a csv file, which has comma as delimeter(csv is comma seperated value).ie: my data is sepereted by comma
//      lineTokenizer.setStrict(false);  // for every column in csv data may nt b available.There might b some empty columns also.False means if data nt available give null n dont do strict checking
//      lineTokenizer.setNames("id", "firstName", "lastName", "age"); //give order of csv file
//
//    //take data from line mapper n conver into bean obj
//      
//      BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//      fieldSetMapper.setTargetType(Student.class);   //convert to student obj
//
//      lineMapper.setLineTokenizer(lineTokenizer);
//      lineMapper.setFieldSetMapper(fieldSetMapper);
//      return lineMapper;
//  }
//      
    
    //create reader using 2nd way.No nned to write another method for this
    
    @Bean
    public ItemReader<Student> reader() {
        return new FlatFileItemReaderBuilder<Student>()
                .name("csvReader")
                .resource(new FileSystemResource("src/main/resources/students.csv"))
                .delimited()
                .names("id", "firstName", "lastName", "age")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Student.class);
                }})
                .build();
    }

  //create processor
    
    @Bean
    public StudentProcessor processor() {
        return new StudentProcessor();
    }

    
  //create writer

    @Bean
    public RepositoryItemWriter<Student> writer() {
    	//write upcoming data to db , we need repo, so inject our repo here

        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);  //write repo name
        writer.setMethodName("save");      //which method to insert record in data jpa repo, its save()
        return writer;
    }

  //create step. For step, configure reader,writer,processor
    
    @Bean
    public Step step() {
        return new StepBuilder("my-step", jobRepository)
                .<Student, Student>chunk(4, platformTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    	//chunk means get is step name, chunk means process 10 records at a time,<Student, Student> means input and output is Student only.

    }

  //create job
    
    @Bean
    public Job runJob() {
        return new JobBuilder("my-job", jobRepository) //job-name
                .start(step())   //i have a step, if multiple steps, use it, u create one more step method and configure name steps
                .build();        //return job obj

    }
    
    
  //Our app taking time in processing due to large data.We need to tell batch that execute the batch concurrently
  //For this use below
  //use this task executer in step()  
    
    @Bean
    public TaskExecutor taskExecutor() {
    	SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
    	executor.setConcurrencyLimit(10);  //execute the records parallelly/concurrently
		return executor;
    }

    

    //IMP: 
    //In db some tables will generate automatically which provide info about our job is its success or fail,check belo2 to use it:
    //	select * from batch_step_execution;
}
