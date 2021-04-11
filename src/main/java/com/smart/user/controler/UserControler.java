package com.smart.user.controler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.smart.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepo;
import com.smart.dao.UserDao;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserControler {

	@Autowired
	private UserDao userRepositary;

	@Autowired
	private ContactRepo contactRepo;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		User user = userRepositary.getUserByUserName(userName);
		model.addAttribute("user", user);

	}

	@GetMapping("/dashboard")
	public String user(Model model) {
		model.addAttribute("Dashboard ,Contact Dashboard ");
		return "normal_user/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String addContact(Model model) {
		model.addAttribute("contact", new Contact());
		return "normal_user/add_contact";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("userImage") MultipartFile multipartFile, Principal principal, HttpSession session) {

		String userName = principal.getName();
		User user = this.userRepositary.getUserByUserName(userName);
		// processing and uploading file

		// file the file to folder and update the name to contact
		try {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			contact.setImage(fileName);
			FileUploadUtil.saveFile(fileName, multipartFile);
			session.setAttribute("message", new Message("Contact added successfully!", "alert-success"));

		} catch (IOException e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong", "alert-warning"));
		}

		user.getContact().add(contact);

		contact.setUser(user);

		this.userRepositary.save(user);

		System.out.println("Data " + contact);

		System.out.println("Added to database!");

		return "normal_user/add_contact";
	}

	@GetMapping("/view-contacts/{page}")
	public String viewContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		String userName = principal.getName();
		User user = userRepositary.getUserByUserName(userName);
		// Pagination
		// Current page
		// Number of per page-5
		Pageable pageable = PageRequest.of(page, 4);

		Page<Contact> contacts = contactRepo.findContactByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal_user/view_contacts";
	}

	@RequestMapping("/contact/{cId}")
	public String contactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println(cId);
		Optional<Contact> optionalContact = contactRepo.findById(cId);
		Contact contact = optionalContact.get();

		String userName = principal.getName();
		User user = this.userRepositary.getUserByUserName(userName);
		if (user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
		}

		return "normal_user/contact_details";
	}

	// delete contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession httpSession) {
		Optional<Contact> findById = this.contactRepo.findById(cId);
		Contact contact = findById.get();
		contact.setUser(null);
		contactRepo.delete(contact);
		httpSession.setAttribute("message", new Message("Contact deleded successfully!", "alert-success"));

		return "redirect:/user/view-contacts/0";

	}

	// Open update handler form
	@PostMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId, Model model) {
		Contact contact = contactRepo.findById(cId).get();
		model.addAttribute("contact", contact);

		return "normal_user/update_contact";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("userImage") MultipartFile file,
			Principal principal, Model model, HttpSession session) {
		System.out.println(contact.getName());
		try {
			Contact oldContactDetail = contactRepo.findById(contact.getcId()).get();
			if (!file.isEmpty()) {
				// delete file
				
				
				
				
				// update new photo
				String fileName = StringUtils.cleanPath(file.getOriginalFilename());
				contact.setImage(fileName);
				FileUploadUtil.saveFile(fileName, file);
			    session.setAttribute("message", new Message("Contact updated successfully!", "alert-success"));
			   

			} else {
				contact.setImage(oldContactDetail.getImagePath());

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		User user = userRepositary.getUserByUserName(principal.getName());
		contact.setUser(user);
		this.contactRepo.save(contact);
		return "normal_user/update_contact";
	}

}
