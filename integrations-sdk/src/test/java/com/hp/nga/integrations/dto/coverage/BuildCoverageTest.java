package com.hp.nga.integrations.dto.coverage;

import com.hp.nga.integrations.services.serialization.SerializationService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gullery on 03/01/2016.
 */

public class BuildCoverageTest {

	@Test
	public void testA() {
		TestCoverage[] testCoverages = new TestCoverage[2];

		FileCoverage[] locs = new FileCoverage[2];
		locs[0] = new FileCoverage("path/fileA.java", new LineCoverage[]{
				new LineCoverage(3, 2),
				new LineCoverage(4, 2)
		});
		locs[1] = new FileCoverage("path/fileB.java", new LineCoverage[]{
				new LineCoverage(1, 1),
				new LineCoverage(2, 1)
		});
		testCoverages[0] = new TestCoverage("nameA", "classA", "packageA", "moduleA", locs);

		locs = new FileCoverage[2];
		locs[0] = new FileCoverage("other/path/fileA.java", new LineCoverage[]{
				new LineCoverage(5, 1),
				new LineCoverage(8, 3)
		});
		locs[1] = new FileCoverage("other/path/fileB.java", new LineCoverage[]{
				new LineCoverage(4, 1),
				new LineCoverage(5, 1)
		});
		testCoverages[1] = new TestCoverage("nameB", "classB", "packageB", "moduleB", locs);

		BuildCoverage bc = new BuildCoverage(testCoverages);

		String json = SerializationService.toJSON(bc);

		assertNotNull(json);

		BuildCoverage newBc = SerializationService.fromJSON(json, BuildCoverage.class);
		assertNotNull(newBc);
		assertEquals(2, newBc.getTestCoverages().length);

		assertNotNull(newBc.getTestCoverages()[0]);
		assertEquals("nameA", newBc.getTestCoverages()[0].getTestName());
		assertEquals("classA", newBc.getTestCoverages()[0].getTestClass());
		assertEquals("packageA", newBc.getTestCoverages()[0].getTestPackage());
		assertEquals("moduleA", newBc.getTestCoverages()[0].getTestModule());

		assertEquals(2, newBc.getTestCoverages()[0].getLocs().length);
		assertNotNull(newBc.getTestCoverages()[0].getLocs()[0]);
		assertEquals("path/fileA.java", newBc.getTestCoverages()[0].getLocs()[0].getFile());
		assertEquals(2, newBc.getTestCoverages()[0].getLocs()[0].getLines().length);
		assertNotNull(newBc.getTestCoverages()[0].getLocs()[0].getLines()[0]);
		assertEquals(3, newBc.getTestCoverages()[0].getLocs()[0].getLines()[0].getNumber());
		assertEquals(2, newBc.getTestCoverages()[0].getLocs()[0].getLines()[0].getCount());
	}
}
