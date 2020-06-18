package it.uniroma3.siw.taskmanager.model;

import javax.persistence.*;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Project is an activity managed by the TaskManager.
 * It is generated and owned by a specific User, that can grant visibility over it to multiple other ones.
 * It can contain one or multiple individual Tasks.
 */
@Entity
public class Project {

	/**
	 * Unique identifier for this Project
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Name for this Project
	 */
	@Column(nullable = false, length = 100)
	private String name;

	/**
	 * Description for this Project
	 */
	@Column
	private String description;

	/**
	 * Name for this Project
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	private User owner;

	/**
	 * Name for this Project
	 */
	@ManyToMany(fetch = FetchType.LAZY)                                // fetch is LAZY by default
	private List<User> members;

	/**
	 * Tasks that this project contains
	 */

	@OneToMany(fetch = FetchType.EAGER,        // whenever a Project is retrieved, always retrieve its tasks too
			cascade = CascadeType.ALL)   // tutte le operazioni Crud di project verranno applicate anche a task
	@JoinColumn(name="project_id")
	private Set<Task> tasks;


	@OneToMany(fetch = FetchType.EAGER,
			cascade = CascadeType.ALL)       // whenever a Project is retrieved, always retrieve its tasks too)   // tutte le operazioni Crud di project verranno applicate anche a task
	@JoinColumn(name="project_id")
	private Set<Tag> tags;                   

	public Project() {
		this.members = new ArrayList<>();
		this.tasks = new HashSet<>();
		this.tags = new HashSet<>();
	}

	public Project(String name, String description) {
		this();
		this.name = name;
		this.description = description;
	}

	public void addMember(User user) {
		if (!this.members.contains(user))
			this.members.add(user);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<User> getMembers() {
		return members;
	}

	public void setMembers(List<User> members) {
		this.members = members;
	}

	public Set<Task> getTasks() {
		return this.tasks;
	}

	public void setTasks(Set<Task> tasks) {
		this.tasks = tasks;
	}

	public void addTask(Task task) {
		this.tasks.add(task);
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(Tag tag) {
		this.tags.add(tag);
	}

	@Override
	public String toString() {

		return "Project{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", tasks=" + tasks +
				'}';
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Project other = (Project) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

}
