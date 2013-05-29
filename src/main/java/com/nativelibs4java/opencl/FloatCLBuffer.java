package com.nativelibs4java.opencl;

import org.bridj.PointerIO;

/** Trivial wrapper class to make the constructor visible */
public class FloatCLBuffer extends CLBuffer<Float>
{
	public FloatCLBuffer(CLContext context, long byteCount, long entityPeer, Object owner, PointerIO<Float> io)
	{
		super(context, byteCount, entityPeer, owner, io);
	}
}

