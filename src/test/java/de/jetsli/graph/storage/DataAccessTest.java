/*
 *  Copyright 2012 Peter Karich info@jetsli.de
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetsli.graph.storage;

import de.jetsli.graph.util.Helper;
import java.io.File;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Peter Karich
 */
public abstract class DataAccessTest {

    public abstract DataAccess createDataAccess(String location);
    private File folder = new File("./target/tmp/");
    protected String location;

    @Before
    public void setUp() {
        Helper.deleteDir(folder);
        folder.mkdirs();
        location = folder.getAbsolutePath() + "/dataacess";
    }

    @After
    public void tearDown() {
        Helper.deleteDir(folder);
    }

    @Test
    public void testLoadFlush() {
        DataAccess da = createDataAccess(location);
        assertFalse(da.loadExisting());
        da.alloc(300);
        da.setInt(7, 123);
        assertEquals(123, da.getInt(7));
        da.setInt(10, Integer.MAX_VALUE / 3);
        assertEquals(Integer.MAX_VALUE / 3, da.getInt(10));        
        da.flush();        
        
        // not always true for mmap case
        // assertEquals(0, da.getInt(2));
        // assertEquals(0, da.getInt(3));
        assertEquals(123, da.getInt(7));
        assertEquals(Integer.MAX_VALUE / 3, da.getInt(10));
        da.close();
        
        // cannot load data twice
        assertFalse(da.loadExisting());
        
        da = createDataAccess(location);
        assertTrue(da.loadExisting());
        assertEquals(123, da.getInt(7));
        da.close();
    }

    @Test
    public void testLoadClose() {
        DataAccess da = createDataAccess(location);
        assertFalse(da.loadExisting());
        // throw some undefined exception if no alloc was called
        try {
            da.setInt(2, 321);
        } catch (Exception ex) {
        }
        
        da.alloc(300).setInt(2, 321);
        // close works the same as flush but one cannot use the same object anymore as probably
        // some underlying resources are freed
        da.close();
        da = createDataAccess(location);
        assertTrue(da.loadExisting());
        assertEquals(321, da.getInt(2));
        da.close();
    }
}