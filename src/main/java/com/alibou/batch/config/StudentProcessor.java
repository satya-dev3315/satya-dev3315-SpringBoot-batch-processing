package com.alibou.batch.config;

import com.alibou.batch.student.Student;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student,Student> { //read input obj as Student n write output as Student

    @Override
    public Student process(Student student) {
//here we can write any processing logic before writing it , like below.We r nt writing any logic for now
//		 if(student.getCountry().equals("United States")) {
//	            return student;
//	        }else{
//	            return null;
//	   
        return student;
    }
}
