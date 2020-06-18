package it.uniroma3.siw.taskmanager.controller.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.taskmanager.model.Task;


@Component
public class TaskValidator implements Validator{

	final Integer MAX_TASKNAME_LENGTH = 20;
	final Integer MIN_TASKNAME_LENGTH = 3;
	final Integer MAX_DESCRIPTION_LENGTH = 1000;

	@Override
	public void validate(Object o, Errors errors) {
		Task task = (Task) o;
		String name = task.getName().trim();
		String description = task.getDescription().trim();

		if (name.isEmpty())
			errors.rejectValue("name", "required");
		else if (name.length() < MIN_TASKNAME_LENGTH || name.length() > MAX_TASKNAME_LENGTH)
			errors.rejectValue("name", "size");
		else if ( description.length() > MAX_DESCRIPTION_LENGTH)
			errors.rejectValue("description", "size");
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Task.class.equals(clazz);
	}



}
