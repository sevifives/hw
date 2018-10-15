package me.sevifives;

import org.flywaydb.core.Flyway;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import me.sevifives.core.Person;
import me.sevifives.core.Task;
import me.sevifives.db.PersonDAO;
import me.sevifives.db.TaskDAO;
import me.sevifives.health.TemplateHealthCheck;
import me.sevifives.resources.HelloWorldResource;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(final String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "HelloWorld";
    }

    @Override
    public void initialize(final Bootstrap<HelloWorldConfiguration> bootstrap) {
    		bootstrap.addBundle(new FlywayBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
            
            @Override
            public FlywayFactory getFlywayFactory(HelloWorldConfiguration configuration) {
                return configuration.getFlywayFactory();
            }
        });
    		
        bootstrap.addBundle(hibernateBundle);
    }
    
    private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
	    new HibernateBundle<HelloWorldConfiguration>(
	    		Person.class,
	    		Task.class
	    		) {
	        @Override
	        public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
	            return configuration.getDataSourceFactory();
	        }
	    };
	
	    

    @Override
    public void run(final HelloWorldConfiguration configuration,
                    final Environment environment) {
    		DataSourceFactory dsf = configuration.getDataSourceFactory();
    		
    		Flyway flyway = Flyway.configure().dataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword()).load();
    		flyway.setSchemas("sevifives");
    		flyway.migrate();
    		
    		final PersonDAO pDao = new PersonDAO(hibernateBundle.getSessionFactory());
    		final TaskDAO tDao = new TaskDAO(hibernateBundle.getSessionFactory());
    	
    		final HelloWorldResource resource = new HelloWorldResource(
    	        configuration.getTemplate(),
    	        configuration.getDefaultName(),
    	        configuration,
    	        tDao, pDao
    	    );
    		
    	    environment.jersey().register(resource);
    	    
    	    final TemplateHealthCheck healthCheck =
	            new TemplateHealthCheck(configuration.getTemplate());
	        environment.healthChecks().register("template", healthCheck);
	        environment.jersey().register(resource);
    }

}
