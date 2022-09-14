package com.sinu.graphql;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.TypeRuntimeWiring;
import graphql.schema.idl.RuntimeWiring.Builder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlApplication.class, args);
	}

@Component
class CrmRuntimeClientConfigurer implements RuntimeWiringConfigurer{

	private final CrmClient crm;

	CrmRuntimeClientConfigurer(CrmClient crm){
		this.crm=crm;
	}

	@Override
	public void configure(Builder builder) {
		builder.type("Query", b -> b.dataFetcher("customers", env -> crm.getCustomers()));
		
	}
	
}



@Component
class CrmClient{

	private final Map<Customer, Collection<Order>> db = new ConcurrentHashMap<>();

	private final AtomicInteger id = new AtomicInteger();

	
	CrmClient(){
		Flux.fromIterable(List.of("Vishnu","Shiv","Venkat","Ram","Braham", "Alla","Jesus","Durga","Lakshmi","Sarswati","king"))
			.flatMap(this::addCustomer)
			.subscribe(customer -> {

				var list = this.db.get(customer);
				for(var orderId=1; orderId<= (Math.random()*100); orderId++){
					Order order = new Order();
					order.setCustomerId(customer.getId());
					order.setId(orderId);
					list.add(order);
				}

			});
	}


	Flux<Order> gerOrderFor(Integer id){
		return getCustomerById(id)
				.map(this.db::get)
				.flatMapMany(Flux::fromIterable);
	}

	Flux<Customer> getCustomers(){

		return Flux.fromIterable(this.db.keySet());
	}

	Flux<Customer> getCustomersByName(String name){

		return getCustomers().filter(c->c.getName().equals(name));
	}

	Mono<Customer> addCustomer(String name){
		Customer customer = new Customer();
		customer.setId(id());
		customer.setName(name);
		this.db.put(customer, new CopyOnWriteArrayList<>());
		return Mono.just(customer);

	}

	Mono<Customer> getCustomerById(Integer id){
		return getCustomers().filter(c -> c.getId().equals(id)).singleOrEmpty();
	}

	Flux<CustomerEvent> getCustomerEvents(Integer customerId){

		return getCustomerById(customerId)
				.flatMapMany(c->{
					return Flux.fromStream(
						Stream.generate( new Supplier<CustomerEvent>() {

								@Override
								public CustomerEvent get() {
									var event = Math.random() > .5 ? CustomerEventyType.CREATED: CustomerEventyType.CREATED;
									CustomerEvent customerEvent =  new CustomerEvent();
									customerEvent.setCustomer(c);
									customerEvent.setEvent(event);
									return customerEvent;
								}
							})
					);
		}).take(10).delayElements(Duration.ofSeconds(1));

	}

	private int id(){
		return this.id.incrementAndGet();
	}
}


}
