package com.example.nagoyameshi.event;

 import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
 
 @Component
 public class PassmissingEventPublisher{
	 private final ApplicationEventPublisher applicationEventPublisher;
	 public PassmissingEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		 this.applicationEventPublisher = applicationEventPublisher;
		 
	 }
	 
	 public void publishPassmissingEvent(User user, String requestUrl) {
		 System.out.println("Publishing PassmissingEvent for user: " + user.getEmail() + " with URL: " + requestUrl);
		 applicationEventPublisher.publishEvent(new PassmissingEvent(this,user,requestUrl));
	 }
 }
 
 