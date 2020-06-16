package it.uniroma3.siw.taskmanager.controller;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;

import it.uniroma3.siw.taskmanager.controller.validation.CredentialsValidator;
import it.uniroma3.siw.taskmanager.controller.validation.UserValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.repository.UserRepository;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.UserService;

import java.util.List;

import javax.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The UserController handles all interactions involving User data.
 */
@Controller
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserValidator userValidator;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	SessionData sessionData;

	@Autowired
	CredentialsService credentialsService;

	@Autowired
	CredentialsValidator credentialsValidator;
	
	@Autowired
	UserService userService;

	/**
	 * This method is called when a GET request is sent by the user to URL "/users/user_id".
	 * This method prepares and dispatches the User registration view.
	 *
	 * @param model the Request model
	 * @return the name of the target view, that in this case is "register"
	 */
	@RequestMapping(value = { "/home" }, method = RequestMethod.GET)
	public String home(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("user", loggedUser);
		return "home";
	}

	/**
	 * This method is called when a GET request is sent by the user to URL "/users/user_id".
	 * This method prepares and dispatches the User registration view.
	 *
	 * @param model the Request model
	 * @return the name of the target view, that in this case is "register"
	 */
	@RequestMapping(value = { "/users/me" }, method = RequestMethod.GET)
	public String me(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Credentials credentials = sessionData.getLoggedCredentials();
		System.out.println(credentials.getPassword());
		model.addAttribute("loggedUuser", loggedUser);
		model.addAttribute("credentials", credentials);

		return "userProfile";
	}

	/**
	 * This method is called when a GET request is sent by the user to URL "/users/user_id".
	 * This method prepares and dispatches the User registration view.
	 *
	 * @param model the Request model
	 * @return the name of the target view, that in this case is "register"
	 */
	@RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
	public String admin(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("user", loggedUser);
		return "admin";
	}

	@RequestMapping(value = { "/admin/users" }, method = RequestMethod.GET)
	public String usersList(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("user", loggedUser);
		List<Credentials> allCredentials = this.credentialsService.getAllCredentials();
		model.addAttribute("user", loggedUser);
		model.addAttribute("allCredentials", allCredentials );
		return "allUsers";
	}

	@RequestMapping(value = { "/admin/users/{username}/delete" }, method = RequestMethod.POST)
	public String removeUser(@PathVariable String username ,Model model) {
		this.credentialsService.deleteCredentials(username);
		return "redirect:/admin/users";
	}
	
	@RequestMapping(value = {"/users/me/update"}, method = RequestMethod.GET)
	public String userForm(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Credentials credentials = this.sessionData.getLoggedCredentials();
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("credentialsForm", credentials);
		System.out.println(credentials.toString());
		return "userUpdate";
		
	}
	
	@RequestMapping(value = {"/users/me/update/{id}"}, method = RequestMethod.POST)
	    public String registerUser(@Valid @ModelAttribute("userForm") User newUser,
	                               BindingResult userBindingResult,
	                               @Valid @ModelAttribute("credentialsForm") Credentials newCredentials,
	                               BindingResult credentialsBindingResult,
	                               Model model, @PathVariable("id") Long credentialsId) {

	        // validate user and credentials fields
	        this.userValidator.validate(newUser, userBindingResult);
	        this.credentialsValidator.validate(newCredentials, credentialsBindingResult);
	        
	        // if neither of them had invalid contents, store the User and the Credentials into the DB
	        if(!userBindingResult.hasErrors() && ! credentialsBindingResult.hasErrors()) {
	            // set the user and store the credentials;
	            // this also stores the User, thanks to Cascade.ALL policy
	          
	           Credentials credentialsDb= credentialsService.getCredentials(credentialsId);
	           User userDb = credentialsDb.getUser();
	           userDb.setFirstName(newUser.getFirstName());
	           userDb.setLastName(newUser.getLastName());
	           credentialsDb.setUser(userDb);
	           credentialsDb.setUserName(newCredentials.getUserName());
	           credentialsDb.setPassword(newCredentials.getPassword());
	           credentialsService.saveCredentials(credentialsDb);
	           return "redirect:/home";
	        }
	        return "userUpdate";
	    }
	
	@RequestMapping(value = {"/users/{id}"}, method = RequestMethod.GET)
	public String showWorker(Model model, @PathVariable("id") Long id) {
		User worker = userService.getUser(id);
		model.addAttribute("worker", worker);
		return "worker" ;
		
	}
}