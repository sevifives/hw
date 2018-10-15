package me.sevifives.db;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import io.dropwizard.hibernate.AbstractDAO;
import me.sevifives.core.Task;

public class TaskDAO extends AbstractDAO<Task> {

	public TaskDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public Optional<Task> findById(Long id) {
		return Optional.ofNullable(get(id));
	}
	public Task create(Task task) {
		return persist(task);
	}
	
	public void delete(Task task) {
		currentSession().delete(task);
	}

	@SuppressWarnings("unchecked")
	public List<Task> findAll() {
		Query<Task> nq = namedQuery("me.sevifives.core.Task.findAll");
		return nq.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public Optional<Task> findByTitleAndPersonId(String title, Long personId) {
		Query<Task> nq = namedQuery("me.sevifives.core.Task.findOneByTitleAndPersonId");
		nq.setParameter("title",title);
		nq.setParameter("personId",personId);
		
		return Optional.ofNullable(nq.getSingleResult());
	}
	
	@SuppressWarnings("unchecked")
	public List<Task> findAllForPersonId(Long personId) {
		Query<Task> nq = namedQuery("me.sevifives.core.Task.findAllForPersonId");
		nq.setParameter("personId",personId);
		
		return nq.getResultList();
	}
}
