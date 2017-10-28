package com.hackathon.hackathon.webparser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebParserTest {
    @Autowired
    WebParser webParser;

    @Test
    public void assertWiring(){
        assertNotNull(webParser);
    }




    @Test
    public void saveFile() throws Exception {
        webParser.saveTextFileFromWebsite("http://allegro.pl/show_item.php?item=7018835713&sh_dwh_token=d0b6dd74a8394345128e20d296cb1c5e");
    }

}