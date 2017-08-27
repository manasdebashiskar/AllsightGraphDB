package com.manas.allsightdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * 
 * @author manasdebashiskar
 *
 * @param <K>
 *            A generic class to keep the Key, Node or the vertex
 * @param <V>
 *            A generic class to keep the value, Again a type of Node or vertex.
 *            Currently only one to many relationship is supported. Tripplet
 *            shall have two useful methods that shall let user get values for
 *            keys and vice versa.
 */
public class Tripplet<K,V> {
    private HashMap<K, V> tripplets;
    private HashMap<V, HashSet<K>> invertedTripplets;

    public Tripplet() {
        tripplets = new HashMap<K, V>();
        invertedTripplets = new HashMap<V, HashSet<K>>();
    }

    /**
     * @param keys
     *            The nodes for which we need the values.
     * @return Map<Node, Node>
     */
    public Map<K, V> getValuesForKeys(List<K> keys) {
        return tripplets.entrySet().stream()
                .filter(map -> keys.contains(map.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    /**
     * 
     * @param values
     *            The nodes for which we need the keys.
     * @return Map<Node, List<Node>> the values and List of Keys they map to.
     *         This is just for efficiency. Where the tripplet HashMap is
     *         inverted to give a better search result on values.
     */
    public Map<V, Set<K>> getKeysForValues(List<V> values) {
        return invertedTripplets.entrySet().stream()
                .filter(map -> values.contains(map.getKey()))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    public void insertTripplets(ArrayList<Tuple<K,V>> tuples) {
        for(Tuple<K,V> tuple: tuples) {
            K key = tuple.getLeft();
            V value = tuple.getRight();
            //Insert each tuple to tripplet collection.
            tripplets.put(key, value);
            HashSet<K> valueSet = new HashSet<K>();
            //Create a set containing the keys.
            valueSet.add(key);
            //result shall contain the existing value set or just the current value.
            HashSet<K> result = invertedTripplets.getOrDefault(value, valueSet);
            result.add(key);
            invertedTripplets.put(value, result);
        }
    }
}
