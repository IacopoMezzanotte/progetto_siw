package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

import java.util.Set;

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
import it.uniroma3.siw.taskmanager.controller.validation.TagValidator;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Tag;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TagService;
import it.uniroma3.siw.taskmanager.service.TaskService;

@Controller
public class TagController {
	
	private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectService projectService;

	@Autowired
	private TagService tagService;

	@Autowired
	private SessionData sessionData;

	@Autowired
	private TagValidator tagValidator;

	@Autowired
	private TaskService taskService;

	@RequestMapping(value = {"/projects/{id}/addTag"}, method = RequestMethod.GET)
	public String addTag(Model model, @PathVariable("id") Long id) {
		Project project = projectService.getProject(id);
		Tag tag = new Tag();
		model.addAttribute("project", project);
		model.addAttribute("tag", tag);
		return "tagForm";
	}

	@RequestMapping(value = {"/projects/{id}/addTag"}, method = RequestMethod.POST)
	public String addTag(@Valid@ModelAttribute("tag")Tag newTag, BindingResult tagBindingResult
			, Model model, @PathVariable("id") Long id) {

		Project project = projectService.getProject(id);
		tagValidator.validate(newTag, tagBindingResult);
		if(!tagBindingResult.hasErrors()) {

			project.addTag(newTag);
			projectService.saveProject(project);
			return "redirect:/projects/{id}";
		}
		model.addAttribute("project", project);
		model.addAttribute("tag",newTag);
		return "tagForm";
	}

	@RequestMapping(value = {"/projects/{projectId}/deleteTag/{tagId}"}, method = RequestMethod.POST)
	public String deleteTag(Model model, @PathVariable("projectId") Long projectId, @PathVariable("tagId") Long tagId) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		User owner = project.getOwner();

		if(loggedUser.equals(owner)){

			project.getTags().remove(tagService.getTag(tagId));
			Set<Task> tasks = project.getTasks();
			for(Task t : tasks) 
				t.getMyTags().remove(tagService.getTag(tagId));
			tagService.deleteTagById(tagId);
			return "redirect:/projects/{projectId}";	
		}
		else
			return "redirect:/projects/{projectId}";
	}

	@RequestMapping(value= {"/projects/{projectId}/tag/{tagId}"}, method = RequestMethod.GET)
	public String showTag(Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("tagId") Long tagId) {
		Tag tag = tagService.getTag(tagId);
		model.addAttribute("tag", tag);
		return "tag";

	}

	@RequestMapping(value = {"/projects/{projectId}/updateTag/{tagId}"}, method = RequestMethod.GET )
	public String updateTask(Model model,  @PathVariable("projectId") Long projectId, @PathVariable("tagId") Long tagId) {
		Project project = projectService.getProject(projectId);
		Tag tag = tagService.getTag(tagId);
		model.addAttribute("project", project);
		model.addAttribute("tag", tag);
		return "updateTag";

	}

	@RequestMapping(value = {"/projects/{projectId}/updateTag/{tagId}"}, method = RequestMethod.POST )
	public String updateTask(@Valid@ModelAttribute("tag") Tag newTag, 
			BindingResult tagBindingResult, Model model,  
			@PathVariable("projectId") Long projectId, 
			@PathVariable("tagId") Long tagId) {

		User loggedUser = sessionData.getLoggedUser();	
		Project project = projectService.getProject(projectId);
		User owner = project.getOwner();
		Tag tag = tagService.getTag(tagId);
		logger.info("ID TAG" + tag.getId());
		if(loggedUser.equals(owner)){
			tagValidator.validate(newTag, tagBindingResult);
			if(!tagBindingResult.hasErrors()) {
				tag.setName(newTag.getName());
				tag.setDescription(newTag.getDescription());
				tag.setColor(newTag.getColor());
				tagService.saveTag(tag);

				return "redirect:/projects/{projectId}" ;
			}
		}

		model.addAttribute("project", project);
		newTag.setId(tag.getId());
		model.addAttribute("tag",newTag);
		return "updateTag";
	}

	@RequestMapping(value = {"/projects/{projectId}/task/{taskId}/addTag"}, method = RequestMethod.GET)
	public String addTagToTask(Model model, @PathVariable("projectId") Long projectId, 
			@PathVariable("taskId") Long taskId) {

		Project project = projectService.getProject(projectId);
		User loggedUser = sessionData.getLoggedUser();
		if(project.getOwner().equals(loggedUser)) {
			List<Tag> tags = tagService.findAll();
			Task task = taskService.getTask(taskId);
			model.addAttribute("task", task);
			model.addAttribute("project", project);
			model.addAttribute("tags", tags);
			return "addTag";
		}
		return "redirect:/projects/{ptojectId}";

	}

	@RequestMapping(value = {"/projects/{projectId}/task/{taskId}/addTag/{tagId}"}, method = RequestMethod.POST)
	public String addTagToTask(Model model, @PathVariable("projectId") Long projectId, 
			@PathVariable("taskId") Long taskId, @PathVariable("tagId") Long tagId) {

		Task task = taskService.getTask(taskId);
		Tag tag = tagService.getTag(tagId);
		task.addTag(tag);
		taskService.saveTask(task);
		return "redirect:/projects/{projectId}/task/{taskId}";

	}

}