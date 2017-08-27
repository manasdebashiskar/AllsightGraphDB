package com.manas.allsightdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

import com.manas.allsightdb.Commands.GetValuesForKeys;
import com.manas.allsightdb.TrippletClassSpec.Customer;

import scala.concurrent.duration.Duration;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

public class TrippletActorSpec extends JUnitSuite {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static class SomeActor extends AbstractActor {
        ActorRef target = null;

        @Override
        public Receive createReceive() {
            return receiveBuilder().matchEquals("hello", message -> {
                getSender().tell("world", getSelf());
                if (target != null)
                    target.forward(message, getContext());
            }).match(ActorRef.class, actorRef -> {
                target = actorRef;
                getSender().tell("done", getSelf());
            }).build();
        }
    }

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

        public class Customer extends Node {
            String name = "Customer#1";

            public Customer(String seed) {
                this.name = "Customer#" + seed;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result
                        + ((name == null) ? 0 : name.hashCode());
                return result;
            }

            @Override
            public boolean equals(final Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                final Customer other = (Customer) obj;
                if (name == null) {
                    if (other.name != null)
                        return false;
                } else if (!name.equals(other.name))
                    return false;
                return true;
            }

            public String toString() {
                return name;
            }
        }

        public class Age extends Node {
            String group = "10-20";

            public Age(String seed) {
                this.group = seed;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result
                        + ((group == null) ? 0 : group.hashCode());
                return result;
            }

            @Override
            public boolean equals(final Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                final Age other = (Age) obj;
                if (group == null) {
                    if (other.group != null)
                        return false;
                } else if (!group.equals(other.group))
                    return false;
                return true;
            }

            public String toString() {
                return group;
            }
        }

        public class Region extends Node {
            String name = "ONTARIO";

            public Region(String seed) {
                this.name = "Region#" + seed;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result
                        + ((name == null) ? 0 : name.hashCode());
                return result;
            }

            @Override
            public boolean equals(final Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                final Region other = (Region) obj;
                if (name == null) {
                    if (other.name != null)
                        return false;
                } else if (!name.equals(other.name))
                    return false;
                return true;
            }

            public String toString() {
                return name;
            }
        }

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
                // “inject” the probe by passing it to the test subject
                // like a real resource would be passed in production
                // subject.tell(probe.getRef(), getRef());
                Commands.InsertTripplets<TestObjects.Customer, TestObjects.Age> items = to
                        .items("1", "10-20");
                GetValuesForKeys<TestObjects.Customer> keys = to.keys();
                System.out.println(items.toString());
                within(duration("3 seconds"),
                        () -> {
                            subject.tell("hello", getRef());
                            subject.tell(items, probe.getRef());
                            // await the correct response
                            subject.tell(keys, probe.getRef());
                            Map<TestObjects.Customer, TestObjects.Age> expMap = new HashMap<TestObjects.Customer, TestObjects.Age>();
                            expMap.put(to.c1, to.a1);
                            Commands.GetValuesForKeysResult<TestObjects.Customer, TestObjects.Age> exp = new Commands.GetValuesForKeysResult<TestObjects.Customer, TestObjects.Age>(
                                    expMap);
                            probe.expectMsgEquals(expMap);
                            return null;
                        });
                /*
                 * // the run() method needs to finish within 3 seconds
                 * within(duration("3 seconds"), () -> { subject.tell("hello",
                 * getRef());
                 * 
                 * // This is a demo: would normally use expectMsgEquals(). //
                 * Wait time is bounded by 3-second deadline above.
                 * awaitCond(probe::msgAvailable);
                 * 
                 * // response must have been enqueued to us before probe
                 * expectMsg(Duration.Zero(), "world"); // check that the probe
                 * we injected earlier got the msg
                 * probe.expectMsg(Duration.Zero(), "hello");
                 * Assert.assertEquals(getRef(), probe.getLastSender());
                 * 
                 * // Will wait for the rest of the 3 seconds expectNoMsg();
                 * return null; });
                 */
            }

        };
    }
}
