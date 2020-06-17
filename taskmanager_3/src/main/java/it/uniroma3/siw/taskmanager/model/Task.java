package it.uniroma3.siw.taskmanager.model;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Task is a unitary activity managed by the TaskManager.
 * It is generated and owned by a specific User within the context of a specific Project.
 * The task is contained in the Project and is visible to whoever has visibility over its Project.
 * The Task can be marked as "completed".
 */
@Entity
public class Task {

	/**
	 * Unique identifier for this Task
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Name for this task
	 */
	@Column(nullable = false, length = 100)
	private String name;

	/**
	 * Description for this task
	 */
	@Column(length = 1000)
	private String description;
	
	@OneToMany
	private List<Commento> commenti;

	/**
	 * Boolean flag specifying whether this Task is completed or not
	 */
	@Column(nullable = false)
	private boolean completed;

	/**
	 * Timestamp for the instant this Task was created/loaded into the DB
	 */
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimestamp;

	/**
	 * Timestamp for the last update of this Task into the DB
	 */
	@Column(nullable = false)
	private LocalDateTime lastUpdateTimestamp;

	@ManyToMany
	private List<Tag> myTags;
	
	@ManyToMany
	private List<User> workers;

	public Task() {
		this.myTags = new ArrayList<>();
		this.workers = new ArrayList<>();
		this.commenti = new ArrayList<>();
	}
	
	

	public Task(String name,
			String description,
			boolean completed) {
		this();
		this.name = name;
		this.description = description;
		this.completed = completed;
	}

	/**
	 * This method initializes the creationTimestamp and lastUpdateTimestamp of this User to the current instant.
	 * This method is called automatically just before the User is persisted thanks to the @PrePersist annotation.
	 */
	@PrePersist
	protected void onPersist() {
		this.creationTimestamp = LocalDateTime.now();
		this.lastUpdateTimestamp = LocalDateTime.now();
	}

	/**
	 * This method updates the lastUpdateTimestamp of this User to the current instant.
	 * This method is called automatically just before the User is updated thanks to the @PreUpdate annotation.
	 */
	@PreUpdate
	protected void onUpdate() {
		this.lastUpdateTimestamp = LocalDateTime.now();
	}


	// GETTERS AND SETTERS

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

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public LocalDateTime getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(LocalDateTime creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public LocalDateTime getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(LocalDateTime lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public void addTag(Tag tag) {
		if(!myTags.contains(tag))
			this.myTags.add(tag);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		if (creationTimestamp == null) {
			if (other.creationTimestamp != null)
				return false;
		} else if (!creationTimestamp.equals(other.creationTimestamp))
			return false;
		if (lastUpdateTimestamp == null) {
			if (other.lastUpdateTimestamp != null)
				return false;
		} else if (!lastUpdateTimestamp.equals(other.lastUpdateTimestamp))
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
		result = prime * result + ((creationTimestamp == null) ? 0 : creationTimestamp.hashCode());
		result = prime * result + ((lastUpdateTimestamp == null) ? 0 : lastUpdateTimestamp.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public List<Tag> getMyTags() {
		return myTags;
	}

	public void setMyTags(List<Tag> myTags) {
		this.myTags = myTags;
	}



	public List<User> getWorkers() {
		return workers;
	}



	public void setWorkers(List<User> workers) {
		this.workers = workers;
	}
	
	public void addWorkers(User user) {
		this.workers.add(user);
	}



	public List<Commento> getCommenti() {
		return commenti;
	}



	public void setCommento(List<Commento> commenti) {
		this.commenti = commenti;
	}
	
	public void addCommento(Commento commento) {
		this.commenti.add(commento);
	}
	
}
