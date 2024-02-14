package com.neo;

import com.neo.model.User;
import com.neo.repository.impl.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class MongoDBApplication implements ApplicationRunner {
	@Autowired
	public UserRepositoryImpl userRepository;
	public static void main(String[] args) {
		SpringApplication.run(MongoDBApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		User user = new User(2L,"neo","123456");
//      userRepository.saveUser(user);
		User userFind = userRepository.findUserByUserName("neo");
		System.out.print(userFind);
	}
}
