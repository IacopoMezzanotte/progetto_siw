package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

import javax.validation.Valid;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.TaskValidator;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TaskService;
import it.uniroma3.siw.taskmanager.service.UserService;


@Controller
public class TaskController {

	private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private SessionData sessionData;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskValidator taskValidator;

	@Autowired
	private UserService userService;


	@RequestMapping (value = {"/projects/{id}/addTask"} , method = RequestMethod.GET)
	public String addTask(Model model, @PathVariable("id") Long id) {
		Task task = new Task();
		Project project = projectService.getProject(id);
		model.addAttribute("task", task);
		model.addAttribute("project", project);
		return "taskForm";

	}

	@RequestMapping(value = {"/projects/{id}/addTask"}, method = RequestMethod.POST)
	public String addTask(@Valid@ModelAttribute("task") Task task,
			BindingResult taskBindingResult, Model model, 
			@PathVariable("id") Long id) {

		taskValidator.validate(task, taskBindingResult);
		if(!taskBindingResult.hasErrors()) {
			Project project = projectService.getProject(id);
			project.addTask(task);
			projectService.saveProject(project);
			return "redirect:/projects/{id}";
		}
		else {
			Project project = projectService.getProject(id);
			model.addAttribute("project", project);
		}
		return "taskForm";
	}

	@RequestMapping(value = {"/projects/{projectId}/deleteTask/{taskId}"}, method = RequestMethod.POST)
	public String deleteTask(Model model, @PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		User owner = project.getOwner();
		
		if(loggedUser.equals(owner)){

			Task task = taskService.getTask(taskId);
			owner.getMyTasks().remove(task);
			List<User> members = project.getMembers();
			for(User u : members)
				u.getMyTasks().remove(task);
			project.getTasks().remove(task);
			taskService.deleteTaskById(taskId);
			return "redirect:/projects/{projectId}";	
		}
		else
			return "redirect:/projects/{projectId}";
	}

	@RequestMapping(value = {"/projects/{projectId}/updateTask/{taskId}"}, method = RequestMethod.GET )
	public String updateTask(Model model,  @PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId) {
		Project project = projectService.getProject(projectId);
		Task task = taskService.getTask(taskId);
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		return "updateTask";

	}

	@RequestMapping(value = {"/projects/{projectId}/updateTask/{taskId}"}, method = RequestMethod.POST )
	public String updateTask(@Valid@ModelAttribute("task") Task newTask, 
			BindingResult taskBindingResult, Model model,  
			@PathVariable("projectId") Long projectId, 
			@PathVariable("taskId") Long taskId) {

		User loggedUser = sessionData.getLoggedUser();	
		Project project = projectService.getProject(projectId);
		User owner = project.getOwner();
		if(loggedUser.equals(owner)){
			logger.info(owner.toString());
			Task task = taskService.getTask(taskId);
			taskValidator.validate(task, taskBindingResult);
			if(!taskBindingResult.hasErrors()) {
				task.setName(newTask.getName());
				task.setDescription(newTask.getDescription());
				taskService.saveTask(task);
				logger.info(project.getTasks().toString().toUpperCase());
				return "redirect:/projects/{projectId}" ;
			}
		}
		return "redirect:/projects/{projectId}";
	}

	@RequestMapping(value ="/projects/{projectId}/task/{taskId}", method = RequestMethod.GET)
	public String showTask(Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {
		Project project = projectService.getProject(projectId);
		Task task = taskService.getTask(taskId);
		logger.info(task.toString());
		List<User> workers = task.getWorkers();
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		model.addAttribute("workers", workers);
		return "task";
	}

	@RequestMapping(value = {"/projects/{projectId}/task/{taskId}/addWorker"}, method = RequestMethod.GET)
	public String addWorker(Model model, @PathVariable("projectId") Long projectId, 
			@PathVariable("taskId") Long taskId ) {
		User loggedUser =  sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		if(loggedUser.equals(project.getOwner())) {
			Task task = taskService.getTask(taskId);
			List<User> members = project.getMembers();
			model.addAttribute("members", members);
			model.addAttribute("task", task);
			model.addAttribute("project", project);
			return "addWorker";
		}
		return "redirect:/projects/{id}" ;

	}

	@RequestMapping(value = {"/projects/{projectId}/task/{taskId}/addWorker/{workerId}"}, method = RequestMethod.POST)
	public String addWorker( Model model, @PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId, 
			@PathVariable("workerId") Long workerId  ) {

		Task task = taskService.getTask(taskId);
		User worker = userService.getUser(workerId);
		if(!task.getWorkers().contains(worker)) {
			task.addWorkers(worker);
			worker.addMyTasks(task);
			taskService.saveTask(task);

			return "redirect:/projects/{projectId}/task/{taskId}";
		}
		return "redirect:/projects/{projectId}/task/{taskId}";

	}
	
	@RequestMapping( value = {"/projects/{project.id}/task/{task.id}/addComment"}, method = RequestMethod.GET)
	public String addComment(Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {
		Project project = projectService.getProject(projectId);
		User loggedUser = sessionData.getLoggedUser();
		
		if(project.getMembers().contains(loggedUser)) {
		Task task = taskService.getTask(taskId);
		String commento = new String();
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		model.addAttribute("commento", commento);
		return "addComment";
		}
		return "redirect:/projects/{projectId}/task/{taskId}";
}
	@RequestMapping( value = {"/projects/{project.id}/task/{task.id}/addComment"}, method = RequestMethod.POST)
	public String addComment(@ModelAttribute("commento") String commento, Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {
		Task task = taskService.getTask(taskId);
		task.addCommento(commento);
		taskService.saveTask(task);
		return "redirect:/projects/{projectId}/task/{taskId}" ;
		
	}
	

}

