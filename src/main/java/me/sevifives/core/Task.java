package me.sevifives.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="tasks",schema="sevifives")
@NamedQueries(
		{
			@NamedQuery(
					name="me.sevifives.core.Task.findAll",
					query="SELECT p FROM Task p"
			),
			
			@NamedQuery(
					name="me.sevifives.core.Task.findAllForPersonId",
					query="SELECT p from Task p WHERE person_id = :personId"
			),
			
			@NamedQuery(
					name="me.sevifives.core.Task.findOneByTitleAndPersonId",
					query="SELECT p from Task p WHERE title = :title AND person_id = :personId"
			)
		}
)
public class Task {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="person_id", nullable=false)
	private Long personId;
	
	@Column(name="title", nullable=false)
	private String title;
	
	@Column(name="description", nullable=false)
	private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
