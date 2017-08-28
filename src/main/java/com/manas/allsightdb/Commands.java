package com.manas.allsightdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.manas.allsightdb.Tuple;

public class Commands {
    interface Command {

    }

    public static class GetValuesForKeys<K> implements Command {
        public final List<K> keys;

        public GetValuesForKeys(List<K> keys) {
            this.keys = keys;
        }
    }

    public static class GetValuesForKeysResult<K, V> implements Command {
        public final Map<K, V> result;

        public GetValuesForKeysResult(Map<K, V> result) {
            this.result = result;
        }
    }

    public static class GetKeysForValues<k,V> implements Command {
        public final List<V> values;

        public GetKeysForValues(List<V> values) {
            this.values = values;
        }
    }

    public static class GetKeysForValuesResult<K, V> implements Command {
        public final Map<V, Set<K>> result;

        public GetKeysForValuesResult(Map<V, Set<K>> result) {
            this.result = result;
        }
    }

    public static class InsertTripplets<K, V> implements Command {
        public final ArrayList<Tuple<K, V>> tuples;

        public InsertTripplets(ArrayList<Tuple<K, V>> tuples) {
            this.tuples = tuples;
        }
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            for (Tuple<K,V> tuple : tuples)
                buffer.append(tuple.toString());
            return buffer.toString();
        }
    }

}
