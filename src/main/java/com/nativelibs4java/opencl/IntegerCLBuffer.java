package com.nativelibs4java.opencl;

import org.bridj.PointerIO;

public class IntegerCLBuffer extends CLBuffer<Integer>
{
	public IntegerCLBuffer(CLContext context, long byteCount, long entityPeer, Object owner, PointerIO<Integer> io)
	{
		super(context, byteCount, entityPeer, owner, io);
	}
}
