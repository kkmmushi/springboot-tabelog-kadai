package com.example.nagoyameshi.event;

 import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
 
 @Component
 public class SignupEventPublisher{
	 private final ApplicationEventPublisher applicationEventPublisher;
	 public SignupEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		 this.applicationEventPublisher = applicationEventPublisher;
		 
	 }
	 
	 public void publishSignupEvent(User user, String requestUrl) {
		 System.out.println("Publishing SignupEvent for user: " + user.getEmail() + " with URL: " + requestUrl);
		 applicationEventPublisher.publishEvent(new SignupEvent(this,user,requestUrl));
	 }
 }
 
 