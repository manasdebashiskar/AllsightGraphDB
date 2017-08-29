# Introduction
Architecture of AllsightGraphDB is based off of **actors**. The actor that manages relationship actors is called **graphActor**. The relationship themselves are maintained by **TrippletActor**s.

# How to run
If you have `sbt` installed on your machine and java 8 you are good to go.
Steps involved are 

1) git clone https://github.com/manasdebashiskar/AllsightGraphDB.git
2) cd to AllsightGraphDB
3) sbt test -- To test the tests written with junit and akka test kit.

    Test1. Return customer belonging to a certain age group

    Test2. Retun Region where highest number of customer belonging to age 20-30.

    Test3. Return populous Age group in a given regions.

![sbt results] (https://github.com/manasdebashiskar/AllsightGraphDB/blob/master/sbt_test_result.png??raw=true)

# Graph DB internals
Before diving into the internal of this program may be we should talk a little bit about what counts while dealing with *Graph*.

```            
                  1
                /   \
             2        3
           /  \       / \
          4    5     6   7
            \/        \/
            8          9 

```

The edge of the graph are otherwise known as relations. Two adjacent nodes are related to each other by diamonds.[2, 4, 5 ,8] and [3,6,7,9] make two such diamonds.

So 4 and 5 are related to each other by 2 and 8. Nodes or Edges themselves don't depict the relation among nodes. The only small-enough entity that depicts relationship is a tripplet.

A **Tripplet** is two nodes connected by a relationship. Both Node and relations have property. Armed with this information let's now discuss about the internals.

At the heart of internal is a collection class called **Tripplet**.
Each relationship is maintained by one such collection class. The class provides 3 API's that allows us to do everything that we need to do.

## APIs
### public void insertTripplets(ArrayList<Tuple<K,V>> tuples)
-- This API is used for populating the collection class.
It takes a list of tuples and inserts it to the map. The left of the tuple is the **customer** and the right of the tuple is **Age**(Customer and Age are two node here.)
To give a better "KeysForValues" query we maintain an inverted index map as well.

### public Map<K, V> getValuesForKeys(List<K> keys)
-- This API as the name suggests takes a bunch of keys and returns a map of key value pair. Using this API we can ask questions like return the Age groups for customers [C1, C2]. And the answer is in the form of a map Map[C1 -> A1, C2 -> A2].

### public Map<V, Set < K >> getKeysForValues(List<V> values)
-- This API uses the inverted index for efficiency. Client can use this API to ask information like return the list of customers for list of age group. 

# Actor of Tripplet
As we expect queries to come in different volume and different order, we shall wrap the **Tripplet** class inside an actor. It is very simple to create another actor with it's specific collection by extending the actor thus below.
```
    public static class Has_AgeGroupActor extends
        TrippletActor<Customer, Age> 
    {
            public Has_AgeGroupActor() {
            }
    }
```
This actor implements the receive method and is ready to receive the three API's **Tripplet** class exposes.

```
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Commands.InsertTripplets.class, s -> {
                    ...
                })
                .match(Commands.GetValuesForKeys.class, s -> {
                    ...
                })
                .match(GetKeysForValues.class,
                        s -> {
                            ...
                        })
                .matchAny(
                        o -> "received unknown message"..
                .build();
    }
```
## Commands
The actor above take few commands.
```
public class Commands {
    interface Command {

    }

    public static class GetValuesForKeys<K> implements Command {
        ...
    }

    public static class GetValuesForKeysResult<K, V> implements Command {
        ...
    }

    public static class GetKeysForValues<K, V> implements Command {
        ...
    }

    public static class GetKeysForValuesResult<K, V> implements Command {
        ...
    }

    public static class InsertTripplets<K, V> implements Command {
        ...
    }

}
```
## graphActor
**graphActor** is the main actor that we create using *actorOf*. It maintains the error kernel. Rest of the actors shall be created from it's context. Hence we can create millions of them if need be. Also, in future it is possible to use Akka clustering and spin of more actors in other machines; hence making it scalable.
It uses a pinned dispatcher. For cluster mode we can change it to other alternatives.

# Design of Actors
The **graphActor** gets instantiated first and creates the relationship actors like **Has_Age** and  **Has_Region** actors.
When started the in-memory database gets populated with information from a file that the program gets from command line.

### Input file structure
It is expected that the input file shall be [customer name| age group | region category]
### Search structure
Currently search structure is hard coded.
User gets to chose from the following sets of queries
1) Return customer belonging to a certain age group
2) Retun Region where highest number of customer belonging to age 20-30.
3) Return populous Age group in a given regions.