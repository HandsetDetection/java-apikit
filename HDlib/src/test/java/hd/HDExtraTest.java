package hd;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;
import api.hd.HDExtra;

public class HDExtraTest {	

	@Test
	public void test_comparePlatformVersionsA() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("9.0.1", "9.1");
		assertTrue(1 >= result);
	}

	@Test
	public void test_comparePlatformVersionsB() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("9.1", "9.0.1");
		assertTrue(-1 <= result);
	}

	@Test
	public void test_comparePlatformVersionsC() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("4.2.1", "9.1");
		assertTrue(1 >= result);
	}
	
	@Test
	public void test_comparePlatformVersionsD() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("4.2.1", "4.2.2");
		assertTrue(1 >= result);
	}

	@Test
	public void test_comparePlatformVersionsE() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("4.2.1", "4.2.12");
		assertTrue(1 >= result);
	}

	@Test
	public void test_comparePlatformVersionsF() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("4.2.1", "4.2");
		assertTrue(-1 <= result);
	}

	@Test
	public void test_comparePlatformVersionsG() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("4.0.21", "40.21");
		assertTrue(1 >= result);
	}

	@Test
	public void test_comparePlatformVersionsH() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("4.1.1", "411");
		assertTrue(1 >= result);
	}

	@Test
	public void test_comparePlatformVersionsI() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("Q7.1", "Q7.2");
		assertTrue(1 >= result);
	}

	@Test
	public void test_comparePlatformVersionsJ() throws IOException {
		HDExtra extra = new HDExtra();
		int result = extra.comparePlatformVersions("Q5SK", "Q7SK");
		assertTrue(1 >= result);
	}
}