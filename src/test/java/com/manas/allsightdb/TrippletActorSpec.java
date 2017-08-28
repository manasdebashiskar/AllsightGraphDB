package com.manas.allsightdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.manas.allsightdb.Customer;
import com.manas.allsightdb.Age;
import com.manas.allsightdb.Region;

public class TrippletActorSpec extends JUnitSuite {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    public static class TestObjects {
        private Customer c1;
        private Age a1;
        private Region r1;

        public Commands.InsertTripplets<Customer, Age> items(String seed,
                String seed1) {
            Customer c = new Customer(seed);
            this.c1 = c;
            Age a1 = new Age(seed1);
            this.a1 = a1;
            List<Customer> cl = new ArrayList<Customer>();
            cl.add(c);
            Tuple<Customer, Age> ccl = new Tuple<Customer, Age>(c, a1);
            ArrayList<Tuple<Customer, Age>> accl = new ArrayList<Tuple<Customer, Age>>();
            accl.add(ccl);
            return new Commands.InsertTripplets<Customer, Age>(accl);
        }

        public Commands.GetValuesForKeys<Customer> keys() {
            List<Customer> cl = new ArrayList<Customer>();
            cl.add(this.c1);
            return new Commands.GetValuesForKeys<Customer>(cl);
        }

        public static class Has_AgeGroupActor extends
                TrippletActor<Customer, Age> {
            public Has_AgeGroupActor() {
            }
        }
    }

    @Test
    public void testIt() {
        new TestKit(system) {
            {
                TestObjects to = new TestObjects();
                final Props props = Props.create(
                        TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef subject = system.actorOf(props);

                // can also use JavaTestKit “from the outside”
                final TestKit probe = new TestKit(system);
                Commands.InsertTripplets<Customer, Age> items = to.items("1",
                        "10-20");
                Commands.GetValuesForKeys<Customer> keys = to.keys();

                within(duration("3 seconds"),
                        () -> {
                            subject.tell(items, probe.getRef());
                            // await the correct response
                            subject.tell(keys, probe.getRef());
                            Map<Customer, Age> expMap = new HashMap<Customer, Age>();
                            expMap.put(to.c1, to.a1);
                            probe.expectMsgEquals(expMap);
                            return null;
                        });
            }

        };
    }
}
