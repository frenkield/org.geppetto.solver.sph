







__kernel void findNeighbors(__global const float4* particles, uint particleCount,
                            __global float4* neighbors, uint neighborCount) {}


const __global float* M, uint width, uint height, const __global float* V,
                                    __global float* W, __local float* partialDotProduct) {
                                    
    for (uint y = get_group_id(0); y < height; y += get_num_groups(0)) {

        const __global float* row = M + y * width;
        float sum = 0;

        for (uint x = get_local_id(0); x < width; x += get_local_size(0))
            sum += row[x] * V[x];
 
        partialDotProduct[get_local_id(0)] = sum;

        for (uint stride = 1; stride < get_local_size(0); stride *= 2) {

            barrier(CLK_LOCAL_MEM_FENCE);

            uint index = 2 * stride * get_local_id(0);

            if (index < get_local_size(0)) {
                partialDotProduct[index] += partialDotProduct[index + stride];
            }
        }
        
        if (get_local_id(0) == 0)
            W[y] = partialDotProduct[0];

        barrier(CLK_LOCAL_MEM_FENCE);
    }
} 








__kernel void multiplyMatrixVector4(const __global float* M, uint width, uint height, const __global float* V,
                                    __global float* W, __local float* partialDotProduct) {
                                    
    for (uint y = get_group_id(0); y < height; y += get_num_groups(0)) {

        const __global float* row = M + y * width;
        float sum = 0;

        for (uint x = get_local_id(0); x < width; x += get_local_size(0))
            sum += row[x] * V[x];
 
        partialDotProduct[get_local_id(0)] = sum;

        for (uint stride = 1; stride < get_local_size(0); stride *= 2) {

            barrier(CLK_LOCAL_MEM_FENCE);

            uint index = 2 * stride * get_local_id(0);

            if (index < get_local_size(0)) {
                partialDotProduct[index] += partialDotProduct[index + stride];
            }
        }
        
        if (get_local_id(0) == 0)
            W[y] = partialDotProduct[0];

        barrier(CLK_LOCAL_MEM_FENCE);
    }
} 








__kernel void multiplyMatrixVector3(const __global float* M, uint width, uint height, const __global float* V,
                                    __global float* W, __local float* partialDotProduct) {
   
   
   //  printf("%d %d\n", get_num_groups(0), get_local_size(0));
                              
     for (uint y = get_group_id(0); y < height; y += get_num_groups(0)) {
 
         const __global float* row = M + y * width;
         float sum = 0;

         for (uint x = get_local_id(0); x < width; x += get_local_size(0))
             sum += row[x] * V[x];

//             /*
         partialDotProduct[get_local_id(0)] = sum;
 
         barrier(CLK_LOCAL_MEM_FENCE);
 
         if (get_local_id(0) == 0) {
             float dotProduct = 0;
             for (uint t = 0; t < get_local_size(0); ++t)
             dotProduct += partialDotProduct[t];
             W[y] = dotProduct;
         }
         
         barrier(CLK_LOCAL_MEM_FENCE);
  //       */
     }
     
     
} 

__kernel void multiplyMatrixVector(const __global float* M, uint width, uint height, const __global float* V,
                                   __global float* W) {
 
    uint y = get_global_id(0);
 
   // printf("%d\n", y);
    
    if (y >= height) {
        return;
    }
    
    
    const __global float* row = M + y * width;
    float dotProduct = 0;
    
    for (uint x = 0; x < width; ++x)
        dotProduct += row[x] * V[x];
        
    W[y] = dotProduct;
} 



__kernel void multiplyMatrixVector2(const __global float* M, uint width, uint height, const __global float* V,
                                    __global float* W) {
                                    
    for (uint y = get_global_id(0); y < height; y += get_global_size(0)) {

        const __global float* row = M + y * width;
        float dotProduct = 0;

        for (uint x = 0; x < width; ++x)
            dotProduct += row[x] * V[x];

        W[y] = dotProduct;
    }
} 