package com.levik.hibernate;

import com.levik.hibernate.entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("default")) {
            doInTx(emf, em -> {
                var person = new Person();
                person.setFirstName("Yevgen");
                person.setLastName("P");
                em.persist(person);
                System.out.println("New person " + person);
            });

            doInTx(emf, em -> {
                Person person = em.find(Person.class, 1L);
                System.out.println(person);
            });

            doInTx(emf, em -> {
                Person person = em.find(Person.class, 1L);
                person.setFirstName("Updated " + person.getFirstName() + " " + System.nanoTime());
            });


            System.out.println("Found two the same parsons but first time us Long and second Integer should be true");
            doInTx(emf, em -> {
                Person person1 = em.find(Person.class, 1L);
                Person person2 = em.find(Person.class, "1");

                System.out.println(person1 == person2);
            });

/*            doInTx(emf, em -> {
                Person person = em.getReference(Person.class, 1L);
                em.remove(person);
            });*/
        }

    }

    static void doInTx(EntityManagerFactory factory, Consumer<EntityManager> work) {
        var entityManager = factory.createEntityManager();
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            work.accept(entityManager);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
        finally {
            entityManager.close();
        }
    }
}
