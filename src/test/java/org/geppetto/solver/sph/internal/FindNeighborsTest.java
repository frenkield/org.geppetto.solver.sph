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

public class FindNeighborsTest {

	private CLContext clContext;
	private CLQueue clQueue;
	private CLKernel findNeighborsKernel;


	private static final int LOCAL_SIZE = 128; //(int)Math.pow(2, 17);
	private static final int GLOBAL_SIZE = LOCAL_SIZE * 32; //(int)Math.pow(2, 17);
	private static final int GROUP_SIZE = GLOBAL_SIZE / LOCAL_SIZE; //(int)Math.pow(2, 17);

	@Test
	public void test1() throws Exception {

		int particleCount = 32 * 100000;
		out.println("particleCount: " + particleCount);
		
		
		float[] particles = new float[particleCount * 4];

		Random random = new Random(1000);

		for (int i = 0; i < particles.length; i++) {
			
			if (i % 4 == 3) {
				continue;
			}
			
			float value = random.nextFloat() * 100;
			particles[i] = value;
		}


		out.println("before");
//		printParticles(particles);

		for (int i = 0; i < 1; i++) {
			particles = hashAndSortParticlesSimple(particles);
		}
		
		out.println("\nafter");
		printParticles(particles, 100);









//		float offset = 10;
//		float scale = 5;
//		
//		for (int i = 0; i < dimension; i++) {
//			for (int j = 0; j < dimension; j++) {
//				for (int k = 0; k < dimension; k++) {
//
//					int particleIndex = (i + j * dimension + k * dimension * dimension) * 4;
//
//					particles[particleIndex] = i * scale + offset;
//					particles[particleIndex + 1] = j * scale + offset;
//					particles[particleIndex + 2] = k * scale + offset;
//					particles[particleIndex + 3] = 0;
//				}
//			}
//		}

	}

	private void printParticles(float[] particles, int limit) {

		limit *= 4;
		
		for (int i = 0; i < particles.length; i += 4) {
			
			if (i < limit || i > particles.length - limit) {

				out.println(String.format("%d - %f %f %f %f", i / 4, particles[i], particles[i + 1], particles[i + 2],
										  particles[i + 3]));
			}
		}
	}



	private float[] hashParticlesDoubleStride(float[] particles) throws Exception {

		initializeCL(DeviceFeature.GPU, "hashParticlesDoubleStride");

		int particleCount = particles.length / 4;

		CLBuffer<Float> particlesBuffer = clContext.createFloatBuffer(CLMem.Usage.InputOutput, particleCount * 4);
		Pointer<Float> particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Write);
		particlesPointer.setFloats(particles);
		particlesBuffer.unmap(clQueue, particlesPointer);

		// -----------------------------------

		int localWorkSize = 32 * 32;
		int globalWorkSize = localWorkSize * 32;

		findNeighborsKernel.setArg(0, particlesBuffer);
		findNeighborsKernel.setArg(1, particleCount);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{globalWorkSize},
																new int[]{localWorkSize});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Read);
		particles = particlesPointer.getFloats();
		particlesBuffer.unmap(clQueue, particlesPointer);

		particlesBuffer.release();

		return particles;
	}

	
	

	private float[] hashParticlesSingleStride(float[] particles) throws Exception {

		initializeCL(DeviceFeature.GPU, "hashParticlesSingleStride");
		
		int particleCount = particles.length / 4;

		CLBuffer<Float> particlesBuffer = clContext.createFloatBuffer(CLMem.Usage.InputOutput, particleCount * 4);
		Pointer<Float> particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Write);
		particlesPointer.setFloats(particles);
		particlesBuffer.unmap(clQueue, particlesPointer);

		// -----------------------------------

		int localWorkSize = 512;
		int globalWorkSize = localWorkSize * 100;
		
		findNeighborsKernel.setArg(0, particlesBuffer);
		findNeighborsKernel.setArg(1, particleCount);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{globalWorkSize},
																new int[]{localWorkSize});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Read);
		particles = particlesPointer.getFloats();
		particlesBuffer.unmap(clQueue, particlesPointer);

		particlesBuffer.release();

		return particles;
	}














	private float[] hashAndSortParticlesSimple(float[] particles) throws Exception {

		initializeCL(DeviceFeature.GPU, "hashParticlesSimple");
		
		int particleCount = particles.length / 4;

		CLBuffer<Float> particlesBuffer = clContext.createFloatBuffer(CLMem.Usage.InputOutput, particleCount * 4);
		Pointer<Float> particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Write);
		particlesPointer.setFloats(particles);
		particlesBuffer.unmap(clQueue, particlesPointer);

		// -----------------------------------

		findNeighborsKernel.setArg(0, particlesBuffer);
		findNeighborsKernel.setArg(1, particleCount);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{particleCount});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Read);
		particles = particlesPointer.getFloats();
		particlesBuffer.unmap(clQueue, particlesPointer);

		particlesBuffer.release();

		return particles;
	}











	private float[] hashParticlesWithLocalStorage(float[] particles) {

		int particleCount = particles.length / 4;

		CLBuffer<Float> particlesBuffer = clContext.createFloatBuffer(CLMem.Usage.InputOutput, particleCount * 4);
		Pointer<Float> particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Write);
		particlesPointer.setFloats(particles);
		particlesBuffer.unmap(clQueue, particlesPointer);

		// -----------------------------------

		int localWorkSize = 32;

		findNeighborsKernel.setArg(0, particlesBuffer);
		findNeighborsKernel.setArg(1, particleCount);
		findNeighborsKernel.setLocalArg(2, localWorkSize * 16);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{localWorkSize}, new int[]{localWorkSize});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Read);
		particles = particlesPointer.getFloats();
		particlesBuffer.unmap(clQueue, particlesPointer);

		particlesBuffer.release();

		return particles;
	}
















	private float[] computeDistances(int particleCount) {

		float[] particles = createVector(particleCount * 4);
		CLBuffer<Float> particlesBuffer = clContext.createFloatBuffer(CLMem.Usage.Input, particleCount * 4);
		Pointer<Float> particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Write);
		particlesPointer.setFloats(particles);
		particlesBuffer.unmap(clQueue, particlesPointer);

		// -----------------------------------

		int particleCountSquared = 1; //particleCount * particleCount;
		
		CLBuffer<Float> distancesBuffer = clContext.createFloatBuffer(CLMem.Usage.Output, particleCountSquared);

		findNeighborsKernel.setArg(0, particlesBuffer);
		findNeighborsKernel.setArg(1, particleCount);
		findNeighborsKernel.setArg(2, distancesBuffer);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{particleCount});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		Pointer<Float> distancesPointer = distancesBuffer.map(clQueue, CLMem.MapFlags.Read);

		float[] distances = distancesPointer.getFloats();
		distancesBuffer.unmap(clQueue, distancesPointer);

		particlesBuffer.release();
		distancesBuffer.release();

		return distances;
	}












	private float[] findNeighbors(int particleCount, int neighborCount) {

		float[] particles = createVector(particleCount * 4);
		CLBuffer<Float> particlesBuffer = clContext.createFloatBuffer(CLMem.Usage.Input, particleCount);
		Pointer<Float> particlesPointer = particlesBuffer.map(clQueue, CLMem.MapFlags.Write);
		particlesPointer.setFloats(particles);
		particlesBuffer.unmap(clQueue, particlesPointer);

		// -----------------------------------

		CLBuffer<Float> neighborsBuffer = clContext.createFloatBuffer(CLMem.Usage.Output, neighborCount * 4);

		findNeighborsKernel.setArg(0, particlesBuffer);
		findNeighborsKernel.setArg(1, particleCount);
		findNeighborsKernel.setArg(2, neighborsBuffer);
		findNeighborsKernel.setArg(3, neighborCount);

		out.println("starting");

		long startTime = System.nanoTime();

		CLEvent completion = findNeighborsKernel.enqueueNDRange(clQueue, new int[]{1});
		completion.waitFor();

		long elapsedTimeMilliseconds = (System.nanoTime() - startTime) / 1000000;
		out.println("elapsed time = " + elapsedTimeMilliseconds);

		Pointer<Float> neighborsPointer = neighborsBuffer.map(clQueue, CLMem.MapFlags.Read);

		float[] neighbors = neighborsPointer.getFloats();
		neighborsBuffer.unmap(clQueue, neighborsPointer);

		particlesBuffer.release();
		neighborsBuffer.release();

		return neighbors;
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

	private void initializeCL(DeviceFeature feature, String kernelName) throws IOException {

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

		out.println(String.format("Max workitems size: %d, %d, %d", device.getMaxWorkItemSizes()[0],
								  device.getMaxWorkItemSizes()[1], device.getMaxWorkItemSizes()[2]));

		out.println("Max compute units: " + device.getMaxComputeUnits());

		
		
		clQueue = clContext.createDefaultQueue();

		String src = IOUtils.readText(getClass().getResourceAsStream("/FindNeighbors.cl"));
		CLProgram program = clContext.createProgram(src);

		findNeighborsKernel = program.createKernel(kernelName);
	}
}
