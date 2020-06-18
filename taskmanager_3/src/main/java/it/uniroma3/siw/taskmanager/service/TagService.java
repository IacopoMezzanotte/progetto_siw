package it.uniroma3.siw.taskmanager.service;

import java.util.List;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.taskmanager.model.Tag;
import it.uniroma3.siw.taskmanager.repository.TagRepository;

@Service
public class TagService {

	@Autowired
	protected TagRepository tagRepository;

	@Transactional
	public Tag getTag(long id) {
		Optional<Tag> result = this.tagRepository.findById(id);
		return result.orElse(null);
	}

	@Transactional
	public Tag saveTag(Tag tag) {
		return this.tagRepository.save(tag);
	}


	@Transactional
	public void deleteTagById(Long id) {
		this.tagRepository.deleteById(id);

	}

	@Transactional
	public List<Tag> findAll() {
		List<Tag> tags = (List<Tag>) tagRepository.findAll();
		return tags ;
	}

}
