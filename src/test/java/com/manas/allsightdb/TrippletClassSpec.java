package com.manas.allsightdb;

import java.util.List;
import org.scalatest.junit.JUnitSuite;
import java.util.ArrayList;
import java.util.Map;
import org.junit.*;
import java.util.Set;
import com.manas.allsightdb.Customer;
import com.manas.allsightdb.Age;
public class TrippletClassSpec extends JUnitSuite {

    public class Has_AgeGroup extends Tripplet<Customer, Age> {
        Has_AgeGroup() {
            super();
        }
    }
    public class Has_Region extends Tripplet<Customer, Region> {
        Has_Region() {
            super();
        }
    }
    @Test
    public void TrippletClassSpec() {
        Has_AgeGroup ageRelation = new Has_AgeGroup();
        Customer c = new Customer("1");
        Age a1 = new Age("10-20");
        List<Customer> cl = new ArrayList<Customer>();
        cl.add(c);
        Tuple<Customer, Age> ccl = new Tuple<Customer, Age>(c, a1);
        ArrayList<Tuple<Customer, Age>> accl = new ArrayList<Tuple<Customer, Age>>();
        accl.add(ccl);
        ageRelation.insertTripplets(accl);
        Map<Customer, Age> result = ageRelation.getValuesForKeys(cl);
        assert (result.get(c) == a1);
        List<Age> ag1 = new ArrayList<Age>();
        ag1.add(a1);
        Map<Age, Set<Customer>> result1 = ageRelation
                .getKeysForValues(ag1);
        assert (result1.get(a1).contains(c) == true);
    }
}
