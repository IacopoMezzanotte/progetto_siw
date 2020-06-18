package it.uniroma3.siw.taskmanager.controller.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.taskmanager.model.Tag;

@Component
public class TagValidator implements Validator {

	final Integer MAX_TAGNAME_LENGTH = 20;
	final Integer MIN_TAGNAME_LENGTH = 3;
	final Integer MAX_DESCRIPTION_LENGTH = 1000;
	final Integer MIN_COLOR_LENGTH = 3;

	@Override
	public boolean supports(Class<?> clazz) {
		return Tag.class.equals(clazz);
	}

	@Override
	public void validate(Object o, Errors errors) {
		Tag tag = (Tag) o;
		String name = tag.getName().trim();
		String description = tag.getDescription().trim();
		String color = tag.getColor().trim();

		if (name.isEmpty())
			errors.rejectValue("name", "required");
		else if (name.length() < MIN_TAGNAME_LENGTH || name.length() > MAX_TAGNAME_LENGTH)
			errors.rejectValue("name", "size");
		else if (color.length() < MIN_COLOR_LENGTH)
			errors.rejectValue("color", "size");
		else if ( description.length() > MAX_DESCRIPTION_LENGTH)
			errors.rejectValue("description", "size");

	}

}
