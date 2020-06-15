package it.uniroma3.siw.taskmanager.controller;

import java.util.List;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.ProjectValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.UserService;

@Controller
public class ProjectController {

	@Autowired
	ProjectService projectService;

	@Autowired
	UserService userService;

	@Autowired
	ProjectValidator projectValidator;

	@Autowired
	SessionData sessionData;

	@Autowired
	private CredentialsService credentialsService;

	@RequestMapping(value = {"/projects"}, method = RequestMethod.GET)
	public String myOwnedProject(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		List<Project> projectList = projectService.retrieveProjectOwnedBy(loggedUser);
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectList", projectList);
		return "myOwnedProjects";

	}

	@RequestMapping(value = {"/projects/{projectId}" }, method = RequestMethod.GET)
	public String project(Model model, @PathVariable Long projectId) {

		Project project = projectService.getProject(projectId);
		User loggedUser = sessionData.getLoggedUser();

		if(project==null)
			return "redirect:/projects";

		List<User> members = userService.getMemebers(project);
		if(!project.getOwner().equals(loggedUser) && !members.contains(loggedUser))
			return "redirect:/projects";

		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("members", members);
		return "project";

	}

	@RequestMapping(value = {"/projects/add"}, method = RequestMethod.GET)
	public String createProjectForm(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = new Project();
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectForm", project);
		return "addProject";
	}

	@RequestMapping(value = {"/projects/add"}, method = RequestMethod.POST)
	public String CreateProject(@Valid @ModelAttribute("projectForm")Project project,
			BindingResult projectBindingResult,Model model) {
		User loggedUser = sessionData.getLoggedUser();
		projectValidator.validate(project, projectBindingResult);
		if(!projectBindingResult.hasErrors()) {
			project.setOwner(loggedUser);
			this.projectService.saveProject(project);
			return "redirect:/projects/" + project.getId();
		}
		return "addProject";
	}

	@RequestMapping(value = {"/projects/{id}/share"} , method = RequestMethod.GET)
	public String shareWith(Model model, @PathVariable("id") Long id) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(id);
		if(loggedUser.equals(project.getOwner())) {
			String userName = new String();
			model.addAttribute("project", project);
			model.addAttribute("userName", userName);

			return "shareWith" ;
		}
		else
			return "redirect:/home";
	}

	@RequestMapping(value = {"/projects/{id}/share"}, method = RequestMethod.POST)
	public String shareWith(@ModelAttribute("userName") String userName,
			Model model, @PathVariable("id") Long id) {

		Project projectShared = projectService.getProject(id);
		Credentials credentials = credentialsService.getCredentials(userName);
		if(credentials == null) {
			return "redirect:/projects/{id}/share";
		}
		else {
			User userToShare = credentials.getUser();
			projectService.shareProjectWithUser(projectShared, userToShare);
			userToShare.addProject(projectShared);
			userService.saveUser(userToShare);
			return "redirect:/projects/{id}";
		}
	}

	@RequestMapping(value = {"/projects/{id}/elimina"} , method = RequestMethod.GET)
	public String elimina(Model model ,  @PathVariable("id") Long id ) {
		this.projectService.deleteById(id);
		return "redirect:/projects/";

	}

	@RequestMapping (value = {"/sharedProjects"} , method = RequestMethod.GET)
	public String sharedProjects(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		List<Project> visibleProjects = userService.getVisibleProjects(loggedUser);
		System.out.println(visibleProjects.toString());
		model.addAttribute("visibleProjects" , visibleProjects);
		return "sharedProjects";

	}

	@RequestMapping (value = {"/projects/{id}/addTask"} , method = RequestMethod.GET)
	public String addTask(Model model, @PathVariable("id") Long id) {
		Task task = new Task();
		Project project = projectService.getProject(id);
		model.addAttribute("task", task);
		model.addAttribute("project", project);
		return "taskForm";

	}

	@RequestMapping(value = {"/projects/{id}/addTask"}, method = RequestMethod.POST)
	public String addTask(@ModelAttribute("task") Task task,
			Model model, @PathVariable("id") Long id) {
		Project project = projectService.getProject(id);
		project.addTask(task);
		projectService.saveProject(project);
		return "redirect:/projects/{id}";
	}
	
	@RequestMapping(value = {"/projects/{id}/update"}, method = RequestMethod.GET)
	public String updateProject(Model model, @PathVariable("id") Long id) {
		Project project = projectService.getProject(id);
		model.addAttribute("projectForm", project);
		return "updateProject";
		
	}
	
	@RequestMapping(value = {"/projects/{id}/update"}, method = RequestMethod.POST )
	public String updateProject(@Valid @ModelAttribute("projectForm")Project newProject,
			BindingResult projectBindingResult,Model model, @PathVariable("id") Long id) {
			projectValidator.validate(newProject, projectBindingResult);
			if(!projectBindingResult.hasErrors()) {
				Project project = projectService.getProject(id);
				project.setName(newProject.getName());
				project.setDescription(newProject.getDescription());
				projectService.saveProject(project);
				return "redirect:/projects/{id}";	
			}
			
			return "updateProject";
}
}