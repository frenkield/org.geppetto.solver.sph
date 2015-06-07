package org.geppetto.solver.gpu;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.util.IOUtils;
import java.io.IOException;
import org.bridj.Pointer;

import static java.lang.System.out;

public class GpuSort {

	private CLContext clContext;
	private CLQueue clQueue;
	private CLKernel bitonicSortKernel;
	
	public GpuSort(CLContext clContext, CLQueue clQueue) throws IOException {

		this.clContext = clContext;
		this.clQueue = clQueue;
		
		String src = IOUtils.readText(getClass().getResourceAsStream("/resource/Sort.cl"));
		CLProgram program = clContext.createProgram(src);

		bitonicSortKernel = program.createKernel("ParallelBitonic_C4");

	}
	
	public void bitonicSort(int[] values) {

		CLBuffer<Integer> valuesBuffer = clContext.createIntBuffer(CLMem.Usage.InputOutput, values.length);
		Pointer<Integer> valuesPointer = valuesBuffer.map(clQueue, CLMem.MapFlags.Write);
		valuesPointer.setInts(values);
		valuesBuffer.unmap(clQueue, valuesPointer);

		// -----------------------------------

		int localWorkSize = 32;
		int globalWorkSize = localWorkSize;

		bitonicSortKernel.setArg(0, valuesBuffer);
		bitonicSortKernel.setArg(1, values.length / 2);
		bitonicSortKernel.setArg(2, 0);
		bitonicSortKernel.setLocalArg(3, localWorkSize * 16);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = bitonicSortKernel.enqueueNDRange(clQueue, new int[]{globalWorkSize},
															  new int[]{localWorkSize});

		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		valuesPointer = valuesBuffer.map(clQueue, CLMem.MapFlags.Read);
		valuesPointer.getInts(values);
		valuesBuffer.unmap(clQueue, valuesPointer);

		valuesBuffer.release();
	}












	public void bitonicSort(float[] values) {

//		(__global data_t * data,
//		int inc0,
//		int dir,
//		__local data_t * aux)

		CLBuffer<Float> valuesBuffer = clContext.createFloatBuffer(CLMem.Usage.InputOutput, values.length);
		Pointer<Float> valuesPointer = valuesBuffer.map(clQueue, CLMem.MapFlags.Write);
		valuesPointer.setFloats(values);
		valuesBuffer.unmap(clQueue, valuesPointer);

		// -----------------------------------

		int localWorkSize = 32;
		int globalWorkSize = localWorkSize;

		bitonicSortKernel.setArg(0, valuesBuffer);
		bitonicSortKernel.setArg(1, values.length);
		bitonicSortKernel.setArg(2, 0);
		bitonicSortKernel.setLocalArg(3, localWorkSize * 16);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = bitonicSortKernel.enqueueNDRange(clQueue, new int[]{globalWorkSize},
															  new int[]{localWorkSize});

//		completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{globalWorkSize},
//																new int[]{localWorkSize});
//
//		completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{globalWorkSize},
//														new int[]{localWorkSize});

		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		valuesPointer = valuesBuffer.map(clQueue, CLMem.MapFlags.Read);
		valuesPointer.getFloats(values);
		valuesBuffer.unmap(clQueue, valuesPointer);

		valuesBuffer.release();
	}








}






