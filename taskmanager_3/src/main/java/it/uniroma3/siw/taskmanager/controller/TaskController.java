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
import it.uniroma3.siw.taskmanager.controller.validation.TaskValidator;
import it.uniroma3.siw.taskmanager.model.Commento;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CommentoService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TaskService;
import it.uniroma3.siw.taskmanager.service.UserService;


@Controller
public class TaskController {


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

	@Autowired
	private CommentoService commentoService;


	@RequestMapping (value = {"/projects/{id}/addTask"} , method = RequestMethod.GET)
	public String addTask(Model model, @PathVariable("id") Long id) {

		
		Project project = projectService.getProject(id);
		if(project.getOwner().equals(sessionData.getLoggedUser())) {
		Task task = new Task();
		model.addAttribute("task", task);
		model.addAttribute("project", project);
		return "taskForm";
		}
		return "redirect:/projects/{id}";
		
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
		Project project = projectService.getProject(id);
		model.addAttribute("project", project);
		return "taskForm";
	}

	@RequestMapping(value = {"/projects/{projectId}/deleteTask/{taskId}"}, method = RequestMethod.POST)
	public String deleteTask(Model model, @PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId) {

		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		User owner = project.getOwner();

		if(loggedUser.equals(owner)){
			Task task = taskService.getTask(taskId);
			List<User> members = project.getMembers();
			userService.removeTask(owner, members, task);
			project.getTasks().remove(task);
			taskService.deleteTaskById(taskId);
			return "redirect:/projects/{projectId}";	
		}
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
		Task task = taskService.getTask(taskId);

		if(loggedUser.equals(owner)){
			taskValidator.validate(newTask, taskBindingResult);
			if(!taskBindingResult.hasErrors()) {
				task.setName(newTask.getName());
				task.setDescription(newTask.getDescription());
				taskService.saveTask(task);
				return "redirect:/projects/{projectId}" ;
			}
		}
		model.addAttribute("project", project);
		newTask.setId(task.getId());
		model.addAttribute("task", newTask);
		return "updateTask";
	
	}

	@RequestMapping(value ="/projects/{projectId}/task/{taskId}", method = RequestMethod.GET)
	public String showTask(Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {

		Project project = projectService.getProject(projectId);
		Task task = taskService.getTask(taskId);
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

	@RequestMapping( value = {"/projects/{projectId}/task/{taskId}/addComment"}, method = RequestMethod.GET)
	public String addComment(Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {

		Project project = projectService.getProject(projectId);
		User loggedUser = sessionData.getLoggedUser();

		if((project.getMembers().contains(loggedUser))||(project.getOwner().equals(loggedUser))) {
			Task task = taskService.getTask(taskId);
			String commento = new String();
			model.addAttribute("project", project);
			model.addAttribute("task", task);
			model.addAttribute("commento", commento);
			return "addComment";
		}
		return "redirect:/projects/{projectId}/task/{taskId}";
	}

	@RequestMapping( value = {"/projects/{projectId}/task/{taskId}/addComment"}, method = RequestMethod.POST)
	public String addComment(@ModelAttribute("commento") String commento, Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {

		Task task = taskService.getTask(taskId);
		Commento comment = new Commento();
		comment.setCommento(commento);
		comment.setUser(sessionData.getLoggedUser());
		task.addCommento(comment);
		commentoService.saveCommento(comment);
		taskService.saveTask(task);
		return "redirect:/projects/{projectId}/task/{taskId}" ;

	}

}

