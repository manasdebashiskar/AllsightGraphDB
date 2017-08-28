package com.manas.allsightdb.utils;

import java.io.*;
import com.manas.allsightdb.*;
import akka.actor.ActorRef;
import java.util.ArrayList;
import com.manas.allsightdb.Commands;

public class FileUtils {
    /**
     * This method needs major optimization. No need to create object on every
     * iterations. For large file we should batch bunch of messages and send it
     * before continuing further.
     * 
     * @param path
     *            path of the file from where the graph database shall be
     *            populated
     * @param hasAge
     *            the actorRef where the customer-age tripplets shall be sent
     * @param hasRegion
     *            the actorRef where the customer-region tripplets shall be sent
     * @throws IOException
     */
    public static void read(String path, ActorRef hasAge, ActorRef hasRegion) throws IOException {
        FileInputStream in = null;

        try {
            in = new FileInputStream(path);
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(in));
            String line;
            // We expect the data to be
            // customer name | age group | region
            while ((line = buffer.readLine()) != null) {
                String[] splits = line.split(",");
                Customer c = new Customer(splits[0]);
                Age a = new Age(splits[1]);
                Region r = new Region(splits[2]);
                ArrayList<Tuple<Customer, Age>> ageTuples = new ArrayList<Tuple<Customer, Age>>();
                ageTuples.add(new Tuple<Customer, Age>(c, a));
                ArrayList<Tuple<Customer, Region>> regionTuple = new ArrayList<Tuple<Customer, Region>>();
                regionTuple.add(new Tuple<Customer, Region>(c, r));
                hasAge.tell(new Commands.InsertTripplets<Customer, Age>(
                        ageTuples), ActorRef.noSender());
                hasRegion.tell(new Commands.InsertTripplets<Customer, Region>(
                        regionTuple), ActorRef.noSender());
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}