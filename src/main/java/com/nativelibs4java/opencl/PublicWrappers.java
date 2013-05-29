package com.nativelibs4java.opencl;

import org.bridj.Pointer;

public class PublicWrappers {
	static final public Pointer<Integer> getPErr()
	{
		return ReusablePointers.get().pErr;
	}
	public static final long getEntity(CLAbstractEntity ent)
	{
		return ent.getEntity();
	}
}
