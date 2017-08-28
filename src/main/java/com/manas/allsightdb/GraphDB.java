package com.manas.allsightdb;

import java.io.IOException;
import com.manas.allsightdb.utils.FileUtils;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class GraphDB {
	//TODO: Obtain the actors via reflection.
    public static class Has_AgeGroupActor extends TrippletActor<Customer, Age> {
        public Has_AgeGroupActor() {
        }
    }

    public static class Has_RegionActor extends TrippletActor<Customer, Region> {
        public Has_RegionActor() {
        }
    }
    /**
     * We create the relationship actors.
     * We populate the database from the file.
     * We take the predicate from the user from command line.
     * 
     * @param args
     */
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("AllsightGraphDB");
        final Props ageProps = Props.create(Has_AgeGroupActor.class,
                () -> new Has_AgeGroupActor());
        final Props regionProps = Props.create(Has_RegionActor.class,
                () -> new Has_RegionActor());
        final ActorRef hasAge = system.actorOf(ageProps);
        final ActorRef hasRegion = system.actorOf(regionProps);
        final String query1 = "return customers belonging to age group";
        final String query2 = "return region where highest number of customer" +
        "belonging to age group";
        final String query3 = "return populous age group in a given region";
        new Thread(() -> {
            try {
                FileUtils.read(args[1], hasAge, hasRegion);
                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {

            }
        }).start();
        System.out.println("ENTER to terminate");
        try {
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        system.terminate();
    }
}
