package com.smart.user.controler;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepo;
import com.smart.dao.UserDao;
import com.smart.entities.Contact;
import com.smart.entities.User;

@RestController
public class SearchControler {
	
	@Autowired
	private UserDao userRepository;
	@Autowired
	private ContactRepo contactRepository;
	
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){
		System.out.println(query);
		User user = userRepository.getUserByUserName(principal.getName());
		List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		return ResponseEntity.ok(contacts);
	}

}
