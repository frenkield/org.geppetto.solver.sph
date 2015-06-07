package org.geppetto.solver.gpu;

import java.io.IOException;
import java.util.Random;
import org.junit.Test;
import static java.lang.System.out;

public class GpuSortTest {

	@Test
	public void testBitonicSort() throws IOException {
		
		JavaCLManager clManager = new JavaCLManager();

		GpuSort gpuSort = new GpuSort(clManager.getClContext(), clManager.getClQueue());
		
		int[] values = new int[128];

		Random random = new Random(1000);

		for (int i = 0; i < values.length; i++) {
			float value = random.nextFloat() - 0.5f;
			
			
			values[i] = values.length - i;
		}

		values[0] = 11000;
		values[1] = 10000;

//		for (int i = 0; i < values.length; i++) {
//			out.println(values[i]);
//		}


		gpuSort.bitonicSort(values);

		for (int i = 0; i < values.length; i++) {
			out.println(values[i]);
		}
		
	}







}
