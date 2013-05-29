package com.nativelibs4java.opencl;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.library.OpenCLLibrary;

public class PublicWrappers {
	static final public Pointer<Integer> getPErr()
	{
		return ReusablePointers.get().pErr;
	}
	public static final long getEntity(CLAbstractEntity ent)
	{
		return ent.getEntity();
	}
	public static final OpenCLLibrary getJavaCLCL()
	{
		return JavaCL.CL;
	}
}
