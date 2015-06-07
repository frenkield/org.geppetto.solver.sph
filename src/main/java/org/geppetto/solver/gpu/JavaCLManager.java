package org.geppetto.solver.gpu;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import java.io.IOException;

import static java.lang.System.out;

public class JavaCLManager {

	private CLContext clContext;
	private CLQueue clQueue;
	
	public JavaCLManager() throws IOException {

		clContext = JavaCL.createBestContext(CLPlatform.DeviceFeature.GPU);

	//	out.println("Created OpenCL context" + clContext);
		CLDevice[] devices = clContext.getDevices();

		for (int i = 0; i < devices.length; i++) {
			out.println("Found device - " + i + ": " + devices[i]);
		}

		CLDevice device = devices[0];
//		out.println("Using device: " + device);
//		out.println("OpenCL version: " + device.getOpenCLCVersion());
//		out.println("Driver version: " + device.getDriverVersion());
//		out.println("Max workgroup size: " + device.getMaxWorkGroupSize());
//
//		out.println(String.format("Max workitems size: %d, %d, %d", device.getMaxWorkItemSizes()[0],
//								  device.getMaxWorkItemSizes()[1], device.getMaxWorkItemSizes()[2]));
//
//		out.println("Max compute units: " + device.getMaxComputeUnits());
		
		clQueue = clContext.createDefaultQueue();
	}

	public CLContext getClContext() {
		return clContext;
	}

	public CLQueue getClQueue() {
		return clQueue;
	}
}
