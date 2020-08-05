package org.openingo.mp.ext.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * DemoTest
 *
 * @author Qicz
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoTest {

    @Test
    public void save() {
        DemoDO d = new DemoDO();
        d.setAge(12);
        d.setUsername("qicz12");
        d.insert();
    }

    @Test
    public void ne() {
        DemoDO d = new DemoDO();
        d.setAge(12);
        d.setAddr("addr");

        DemoDO qicz = DemoDO.dao(DemoDO.class).by(d).select(DemoDO::getUsername).ne(true, DemoDO::getUsername, "qicz12").doQueryLimitOne();
        System.out.println(qicz);
    }
}
