package com.manas.allsightdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.manas.allsightdb.Customer;
import com.manas.allsightdb.Age;
import com.manas.allsightdb.Region;
import com.manas.allsightdb.Commands.GetValuesForKeysResult;

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

        public Commands.InsertTripplets<Customer, Age> ageItems(String seed, String seed1) {
            Customer c = new Customer(seed);
            Age a1 = new Age(seed1);
            List<Customer> cl = new ArrayList<Customer>();
            cl.add(c);
            Tuple<Customer, Age> ccl = new Tuple<Customer, Age>(c, a1);
            ArrayList<Tuple<Customer, Age>> accl = new ArrayList<Tuple<Customer, Age>>();
            accl.add(ccl);
            return new Commands.InsertTripplets<Customer, Age>(accl);
        }

        public Commands.InsertTripplets<Customer, Region> regionItems(String seed, String seed1) {
            Customer c = new Customer(seed);
            Region r1 = new Region(seed1);
            List<Customer> cl = new ArrayList<Customer>();
            cl.add(c);
            Tuple<Customer, Region> ccl = new Tuple<Customer, Region>(c, r1);
            ArrayList<Tuple<Customer, Region>> accl = new ArrayList<Tuple<Customer, Region>>();
            accl.add(ccl);
            return new Commands.InsertTripplets<Customer, Region>(accl);
        }

        public static class Has_AgeGroupActor extends TrippletActor<Customer, Age> {
            public Has_AgeGroupActor() {
            }
        }
    }

    @Test
    public void CanQueryTrippletActor() {
        new TestKit(system) {
            {
                TestObjects to = new TestObjects();
                final Props props = Props.create(TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef subject = system.actorOf(props);

                // can also use JavaTestKit “from the outside”
                final TestKit probe = new TestKit(system);
                Commands.InsertTripplets<Customer, Age> items = to.ageItems("1", "10-20");
                List<Customer> cl = new ArrayList<Customer>();
                cl.add(new Customer("1"));
                Commands.GetValuesForKeys<Customer> keys = new Commands.GetValuesForKeys<Customer>(cl);

                within(duration("3 seconds"), () -> {
                    subject.tell(items, probe.getRef());
                    // await the correct response
                    subject.tell(keys, probe.getRef());
                    Map<Customer, Age> expMap = new HashMap<Customer, Age>();
                    expMap.put(items.tuples.get(0).getLeft(), items.tuples.get(0).getRight());
                    GetValuesForKeysResult<Customer, Age> result = (GetValuesForKeysResult<Customer, Age>) probe
                            .receiveOne(duration("1 seconds"));
                    assert (result.result == expMap);
                    return null;
                });
            }
        };
    }

    @Test
    public void CanQueryMultipleActor() {
        new TestKit(system) {
            {
                TestObjects to = new TestObjects();
                String[] ageRange = { "10-20", "20-30", "40-50", "50-60", "60-70" };
                String[] regionRange = { "AA", "BB", "CC", "DD", "EE" };
                final Props ageProps = Props.create(TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef ageActor = system.actorOf(ageProps);
                final Props regionProps = Props.create(TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef regionActor = system.actorOf(regionProps);
                Commands.InsertTripplets<Customer, Age> items;
                ArrayList<Tuple<Customer, Age>> payload = new ArrayList<Tuple<Customer, Age>>();
                IntStream.range(1, 10).forEachOrdered(i -> {
                    payload.addAll(to.ageItems(Integer.toString(i), ageRange[Math.floorMod(i - 1, 5)]).tuples);
                });
                Commands.InsertTripplets<Customer, Age> inputAgeTuples = new Commands.InsertTripplets<Customer, Age>(
                        payload);

                ArrayList<Tuple<Customer, Region>> regionPayloads = new ArrayList<Tuple<Customer, Region>>();

                IntStream.range(1, 10).forEachOrdered(i -> {
                    regionPayloads
                            .addAll(to.regionItems(Integer.toString(i), regionRange[Math.floorMod(i - 1, 5)]).tuples);
                });
                System.out.println(regionPayloads);
                Commands.InsertTripplets<Customer, Region> inputRegionTuples = new Commands.InsertTripplets<Customer, Region>(
                        regionPayloads);

                within(duration("3 seconds"), () -> {
                    ageActor.tell(inputAgeTuples, ActorRef.noSender());
                    regionActor.tell(inputRegionTuples, ActorRef.noSender());
                    // We ask the regionActor to give back customers from a
                    // region "BB" and "CC"
                    List<Region> cl = new ArrayList<Region>();
                    cl.add(new Region("BB"));
                    cl.add(new Region("CC"));
                    Commands.GetValuesForKeys<Region> values = new Commands.GetValuesForKeys<Region>(cl);
                    // regionActor.tell(), arg1);
                    return null;
                });
            }

        };
    }

}
