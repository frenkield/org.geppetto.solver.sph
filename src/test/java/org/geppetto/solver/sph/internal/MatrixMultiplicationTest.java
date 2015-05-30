/**
 * ****************************************************************************
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 * <p/>
 * Contributors:
 * OpenWorm - http://openworm.org/people.html
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 * *****************************************************************************
 */

package org.geppetto.solver.sph.internal;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform.DeviceFeature;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;
import java.io.IOException;
import java.util.Random;
import org.bridj.Pointer;
import org.junit.Test;

import static java.lang.System.out;

public class MatrixMultiplicationTest {

	private CLContext clContext;
	private CLQueue clQueue;
	private CLKernel kernel;
	private float[] input;
	private CLKernel matrixMultiplyKernel;


	private static final String KERNEL_NAME = "multiplyMatrixVector4";
	private static final int LOCAL_SIZE = 128; //(int)Math.pow(2, 17);
	private static final int GLOBAL_SIZE = LOCAL_SIZE * 32; //(int)Math.pow(2, 17);
	private static final int GROUP_SIZE = GLOBAL_SIZE / LOCAL_SIZE; //(int)Math.pow(2, 17);

	@Test
	public void testMatrixMultiply() throws Exception {

		initializeCL(DeviceFeature.GPU);

		int width = 1100;
		int height = 100000;

//		int width = 128;
//		int height = 128;

		float[] product = null;
		
		for (int i = 0; i < 10; i++) {
			product = multiplyMatrixVector(width, height);
		}
		

//		out.println("product");
//		for (int i = 0; i < product.length; i++) {
//			out.println(product[i]);
//		}
	}

	private float[] multiplyMatrixVector(int width, int height) {
		
		float[] matrix = createMatrix(width, height);
		CLBuffer<Float> matrixBuffer = clContext.createFloatBuffer(CLMem.Usage.Input, width * height);
		Pointer<Float> matrixPointer = matrixBuffer.map(clQueue, CLMem.MapFlags.Write);
		matrixPointer.setFloats(matrix);
		matrixBuffer.unmap(clQueue, matrixPointer);

		// -----------------------------------

		float[] vector = createVector(width);
		CLBuffer<Float> vectorBuffer = clContext.createFloatBuffer(CLMem.Usage.Input, width);
		Pointer<Float> vectorPointer = vectorBuffer.map(clQueue, CLMem.MapFlags.Write);
		vectorPointer.setFloats(vector);
		vectorBuffer.unmap(clQueue, vectorPointer);

		// -----------------------------------

		CLBuffer<Float> productBuffer = clContext.createFloatBuffer(CLMem.Usage.Output, height);

		matrixMultiplyKernel.setArg(0, matrixBuffer);
		matrixMultiplyKernel.setArg(1, width);
		matrixMultiplyKernel.setArg(2, height);
		matrixMultiplyKernel.setArg(3, vectorBuffer);
		matrixMultiplyKernel.setArg(4, productBuffer);
		matrixMultiplyKernel.setLocalArg(5, LOCAL_SIZE);

		out.println("starting");
		
		long startTime = System.nanoTime();

		CLEvent completion = matrixMultiplyKernel.enqueueNDRange(clQueue, new int[]{GLOBAL_SIZE},
																 new int[]{LOCAL_SIZE});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		Pointer<Float> productPointer = productBuffer.map(clQueue, CLMem.MapFlags.Read);

		float[] product = productPointer.getFloats();
		productBuffer.unmap(clQueue, productPointer);

		matrixBuffer.release();
		vectorBuffer.release();
		productBuffer.release();
		
		return product;
	}

	private float[] createVector(int height) {
		
		Random random = new Random(1000);
		float[] vector = new float[height];

		for (int i = 0; i < vector.length; i++) {
			float value = random.nextFloat() - 0.5f;
			vector[i] = value;
		}

//		for (int i = 0; i < vector.length; i++) {
//			out.println(vector[i]);
//		}

		return vector;
	}

	private float[] createMatrix(int width, int height) {

		Random random = new Random(0);
		float[] matrix = new float[width * height];

		for (int n = 0; n < height; n++) {
			for (int m = 0; m < width; m++) {
				
				
				float value = random.nextFloat() - 0.5f;

				
//				if (m == n) {
//					value = 2;
//				} else {
//					value = 0;
//				}
				

				matrix[n * width + m] = value;
				
				
			}
		}

//		for (int n = 0; n < height; n++) {
//			for (int m = 0; m < width; m++) {
//				out.print(matrix[n * width + m] + " ");
//			}
//			out.println();
//		}
		
		return matrix;
	}
	
	private void initializeCL(DeviceFeature feature) throws IOException {
		
		clContext = JavaCL.createBestContext(feature);

		out.println("Created OpenCL context" + clContext);
		CLDevice[] devices = clContext.getDevices();

		for (int i = 0; i < devices.length; i++) {
			out.println("Found device - " + i + ": " + devices[i]);
		}

		CLDevice device = devices[0];
		out.println("Using device: " + device);
		out.println("OpenCL version: " + device.getOpenCLCVersion());
		out.println("Driver version: " + device.getDriverVersion());
		out.println("Max workgroup size: " + device.getMaxWorkGroupSize());
		out.println("Max workitems size: " + device.getMaxWorkItemSizes()[0]);
		out.println("Max compute units: " + device.getMaxComputeUnits());

		clQueue = clContext.createDefaultQueue();

		String src = IOUtils.readText(getClass().getResourceAsStream("/MatrixMultiplication.cl"));
		CLProgram program = clContext.createProgram(src);

		matrixMultiplyKernel = program.createKernel(KERNEL_NAME);
	}

	public synchronized float[] solveWithHostMemory() {

		Pointer<Float> ptrIn = Pointer.pointerToFloats(input).order(clContext.getByteOrder());
		Pointer<Float> ptrOut = Pointer.allocateFloats(input.length).order(clContext.getByteOrder());

		CLBuffer<Float> bufIn = clContext.createBuffer(CLMem.Usage.Input, ptrIn, false);
		CLBuffer<Float> bufOut = clContext.createBuffer(CLMem.Usage.Output, ptrOut, false);

		kernel.setArg(0, bufIn);
		kernel.setArg(1, bufOut);
		kernel.setArg(2, input.length);





		long startTime = System.nanoTime();

		CLEvent completion = kernel.enqueueNDRange(clQueue, new int[]{input.length});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);





		bufOut.map(clQueue, CLMem.MapFlags.Read);
		float[] output = ptrOut.getFloats();
		bufOut.unmap(clQueue, ptrOut);

		bufIn.release();
		ptrIn.release();
		
		bufOut.release();
		ptrOut.release();
		
		return output;
	}

	private void solveWithDeviceMemory() {

//		int particleCount = 10;
		
		
		CLBuffer<Float> bufIn = clContext.createFloatBuffer(CLMem.Usage.Input, input.length);
		CLBuffer<Float> bufOut = clContext.createFloatBuffer(CLMem.Usage.Output, input.length);

		Pointer<Float> ptrIn = bufIn.map(clQueue, CLMem.MapFlags.Write);



		ptrIn.setFloats(input);

		
		
		bufIn.unmap(clQueue, ptrIn);

		kernel.setArg(0, bufIn);
		kernel.setArg(1, bufOut);
		kernel.setArg(2, input.length);

		
		
		
		
		long startTime = System.nanoTime();
		
		CLEvent completion = kernel.enqueueNDRange(clQueue, new int[]{1024});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);
		
		
		
		
		
		
		Pointer<Float> ptrOut = bufOut.read(clQueue);

		bufIn.release();
		ptrIn.release();

		bufOut.release();

		ptrOut.release();
	}
}
