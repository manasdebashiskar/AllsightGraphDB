package com.manas.allsightdb;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.manas.allsightdb.Commands.*;

public class TrippletActor<K, V> extends AbstractActor {
    private Tripplet<K, V> trippletClass;

    public TrippletActor() {
        trippletClass = new Tripplet<K, V>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    System.out.println(s.toString());
                })
                .match(Commands.InsertTripplets.class, s -> {
                    trippletClass.insertTripplets(s.tuples);
                })
                .match(Commands.GetValuesForKeys.class, s -> {
                    Map<K, V> results = trippletClass.getValuesForKeys(s.keys);
                    s.keys.forEach(p -> System.out.println(p));
                    System.out.println("GetValuesForKeysResult");
                    getSender().tell(results, getSender());
                })
                .match(GetKeysForValues.class,
                        s -> {
                            GetKeysForValuesResult<K, V> result = new GetKeysForValuesResult<K, V>(
                                    trippletClass.getKeysForValues(s.values));
                            getSender().tell(result, getSender());
                        })
                .matchAny(
                        o -> System.out.println("received unknown message" + o))
                .build();
    }
}
