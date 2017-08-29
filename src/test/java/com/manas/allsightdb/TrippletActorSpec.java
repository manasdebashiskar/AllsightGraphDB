package com.manas.allsightdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

        public Commands.InsertTripplets<Customer, Age> ageItems(String seed,
                String seed1) {
            Customer c = new Customer(seed);
            Age a1 = new Age(seed1);
            List<Customer> cl = new ArrayList<Customer>();
            cl.add(c);
            Tuple<Customer, Age> ccl = new Tuple<Customer, Age>(c, a1);
            ArrayList<Tuple<Customer, Age>> accl = new ArrayList<Tuple<Customer, Age>>();
            accl.add(ccl);
            return new Commands.InsertTripplets<Customer, Age>(accl);
        }

        public Commands.InsertTripplets<Customer, Region> regionItems(
                String seed, String seed1) {
            Customer c = new Customer(seed);
            Region r1 = new Region(seed1);
            List<Customer> cl = new ArrayList<Customer>();
            cl.add(c);
            Tuple<Customer, Region> ccl = new Tuple<Customer, Region>(c, r1);
            ArrayList<Tuple<Customer, Region>> accl = new ArrayList<Tuple<Customer, Region>>();
            accl.add(ccl);
            return new Commands.InsertTripplets<Customer, Region>(accl);
        }

        public static class Has_AgeGroupActor extends
                TrippletActor<Customer, Age> {
            public Has_AgeGroupActor() {
            }
        }
    }

    @Test
    public void Test1ReturnCustomerBelongingToACertainAgeGroup() {
        new TestKit(system) {
            {
                TestObjects to = new TestObjects();
                final Props props = Props.create(
                        TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef subject = system.actorOf(props);

                // can also use JavaTestKit “from the outside”
                final TestKit probe = new TestKit(system);
                Commands.InsertTripplets<Customer, Age> items = to.ageItems(
                        "1", "10-20");
                List<Customer> cl = new ArrayList<Customer>();
                cl.add(new Customer("1"));
                Commands.GetValuesForKeys<Customer> keys = new Commands.GetValuesForKeys<Customer>(
                        cl);

                within(duration("3 seconds"),
                        () -> {
                            subject.tell(items, probe.getRef());
                            // await the correct response
                            subject.tell(keys, probe.getRef());
                            Map<Customer, Age> expMap = new HashMap<Customer, Age>();
                            expMap.put(items.tuples.get(0).getLeft(),
                                    items.tuples.get(0).getRight());
                            GetValuesForKeysResult<Customer, Age> result = (GetValuesForKeysResult<Customer, Age>) probe
                                    .receiveOne(duration("1 seconds"));
                            assert (result.result == expMap);
                            return null;
                        });
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    public void Test3_CanQueryAgeGroupForCustomerFromAParticularRegionList() {
        new TestKit(system) {
            {
                final TestKit probe = new TestKit(system);
                TestObjects to = new TestObjects();
                String[] ageRange = { "10-20", "20-30", "40-50", "50-60",
                        "60-70" };
                String[] regionRange = { "AA", "BB", "CC", "DD", "EE" };
                final Props ageProps = Props.create(
                        TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef ageActor = system.actorOf(ageProps);
                final Props regionProps = Props.create(
                        TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef regionActor = system.actorOf(regionProps);
                ArrayList<Tuple<Customer, Age>> payload = new ArrayList<Tuple<Customer, Age>>();
                IntStream.range(1, 1000).forEachOrdered(
                        i -> {
                            payload.addAll(to.ageItems(Integer.toString(i),
                                    ageRange[Math.floorMod(i - 1, 5)]).tuples);
                        });
                Commands.InsertTripplets<Customer, Age> inputAgeTuples = new Commands.InsertTripplets<Customer, Age>(
                        payload);

                ArrayList<Tuple<Customer, Region>> regionPayloads = new ArrayList<Tuple<Customer, Region>>();

                IntStream
                        .range(1, 1000)
                        .forEachOrdered(
                                i -> {
                                    regionPayloads.addAll(to.regionItems(
                                            Integer.toString(i),
                                            regionRange[Math.floorMod(i - 1, 5)]).tuples);
                                });
                Commands.InsertTripplets<Customer, Region> inputRegionTuples = new Commands.InsertTripplets<Customer, Region>(
                        regionPayloads);

                within(duration("3 seconds"), () -> {
                    ageActor.tell(inputAgeTuples, ActorRef.noSender());
                    regionActor.tell(inputRegionTuples, ActorRef.noSender());
                    // We ask the regionActor to give back customers from
                    // region "BB" and "CC"
                        List<Region> cl = new ArrayList<Region>();
                        cl.add(new Region("BB"));
                        cl.add(new Region("CC"));
                        Commands.GetKeysForValues<Customer, Region> values = new Commands.GetKeysForValues<Customer, Region>(
                                cl);
                        regionActor.tell(values, probe.getRef());
                        Commands.GetKeysForValuesResult<Customer, Region> regionsWithCustomer = (Commands.GetKeysForValuesResult<Customer, Region>) probe
                                .receiveOne(duration("1 seconds"));
                        List<Customer> list = (List<Customer>) (regionsWithCustomer.result
                                .values().stream().flatMap(s -> s.stream()))
                                .collect(Collectors.toList());
                        // We ask the ageActor to give back ages for the list of
                        // customers.
                        Commands.GetValuesForKeys<Customer> query = new Commands.GetValuesForKeys<Customer>(
                                list);
                        ageActor.tell(query, probe.getRef());
                        Commands.GetValuesForKeysResult<Customer, Age> results = (Commands.GetValuesForKeysResult<Customer, Age>) (probe
                                .receiveOne(duration("1 seconds")));
                        Map<Customer, Age> expectedResult = new HashMap<Customer, Age>();
                        expectedResult.put(new Customer("8"), new Age("40-50"));
                        expectedResult.put(new Customer("7"), new Age("20-30"));
                        expectedResult.put(new Customer("3"), new Age("40-50"));
                        assert (results.result == expectedResult);
                        return null;
                    });
            }

        };
    }

    @SuppressWarnings("unchecked")
    @Test
    public void Test2_RetunRegionsWhereHighestNumberOfCustomerBelongingToAge20_30() {
        new TestKit(system) {
            {
                final TestKit probe = new TestKit(system);
                TestObjects to = new TestObjects();
                String[] ageRange = { "10-20", "20-30", "40-50", "50-60",
                        "60-70" };
                String[] regionRange = { "AA", "BB", "CC", "DD", "EE" };
                final Props ageProps = Props.create(
                        TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef ageActor = system.actorOf(ageProps);
                final Props regionProps = Props.create(
                        TestObjects.Has_AgeGroupActor.class,
                        () -> new TestObjects.Has_AgeGroupActor());
                final ActorRef regionActor = system.actorOf(regionProps);
                ArrayList<Tuple<Customer, Age>> payload = new ArrayList<Tuple<Customer, Age>>();
                IntStream.range(1, 10).forEachOrdered(
                        i -> {
                            payload.addAll(to.ageItems(Integer.toString(i),
                                    ageRange[Math.floorMod(i - 1, 5)]).tuples);
                        });
                Commands.InsertTripplets<Customer, Age> inputAgeTuples = new Commands.InsertTripplets<Customer, Age>(
                        payload);

                ArrayList<Tuple<Customer, Region>> regionPayloads = new ArrayList<Tuple<Customer, Region>>();

                IntStream
                        .range(1, 10)
                        .forEachOrdered(
                                i -> {
                                    regionPayloads.addAll(to.regionItems(
                                            Integer.toString(i),
                                            regionRange[Math.floorMod(i - 1, 5)]).tuples);
                                });
                Commands.InsertTripplets<Customer, Region> inputRegionTuples = new Commands.InsertTripplets<Customer, Region>(
                        regionPayloads);

                within(duration("3 seconds"), () -> {
                    ageActor.tell(inputAgeTuples, ActorRef.noSender());
                    regionActor.tell(inputRegionTuples, ActorRef.noSender());
                    // We ask the regionActor to give back customers from
                    // region "BB" and "CC"
                        List<Age> cl = new ArrayList<Age>();
                        cl.add(new Age("20-30"));
                        Commands.GetKeysForValues<Customer, Age> values = new Commands.GetKeysForValues<Customer, Age>(
                                cl);
                        ageActor.tell(values, probe.getRef());
                        Commands.GetKeysForValuesResult<Customer, Age> ageWithCustomer = (Commands.GetKeysForValuesResult<Customer, Age>) probe
                                .receiveOne(duration("1 seconds"));

                        Map<Integer, Set<Customer>> customerWithAge20_30 = ageWithCustomer.result
                                .entrySet()
                                .stream()
                                .collect(
                                        Collectors.toMap(p -> (Integer) p
                                                .getValue().size(),
                                                p -> (Set<Customer>) p
                                                        .getValue()));

                        Map.Entry<Integer, Set<Customer>> maxEntry = null;

                        for (Map.Entry<Integer, Set<Customer>> entry : customerWithAge20_30
                                .entrySet()) {

                            if (maxEntry == null
                                    || entry.getKey().compareTo(
                                            maxEntry.getKey()) > 0) {
                                maxEntry = entry;
                            }
                        }

                        Commands.GetValuesForKeys<Customer> query = new Commands.GetValuesForKeys<Customer>(
                                maxEntry.getValue().stream()
                                        .collect(Collectors.toList()));

                        // Now we have a list of customers where the count is
                        // max for the target age group 20-30.

                        regionActor.tell(query, probe.getRef());
                        Commands.GetValuesForKeysResult<Customer, Region> results = (Commands.GetValuesForKeysResult<Customer, Region>) (probe
                                .receiveOne(duration("1 seconds")));
                        Map<Customer, Region> expectedResult = new HashMap<Customer, Region>();
                        expectedResult.put(new Customer("2"), new Region("BB"));
                        expectedResult.put(new Customer("7"), new Region("BB"));
                        assert (results.result == expectedResult);

                        return null;
                    });
            }
        };
    }
}
