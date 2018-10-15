package me.sevifives.db;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import io.dropwizard.hibernate.AbstractDAO;
import me.sevifives.core.Person;

public class PersonDAO extends AbstractDAO<Person> {

	public PersonDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	
	public Optional<Person> findById(Long id) {
        return Optional.ofNullable(get(id));
    }
	
	public Person create(Person person) {
        return persist(person);
    }
	
	public void delete(Person person) {
		currentSession().delete(person);
	}
	
	@SuppressWarnings("unchecked")
    public List<Person> findAll() {
        return list((Query<Person>) namedQuery("me.sevifives.core.Person.findAll"));
    }
}
