


__kernel void hashAndSortParticles(__global float4* particles, uint particleCount) {


    uint particleStartIndex = get_local_id(0);

    printf("%d\n", particleStartIndex);

    if (particleStartIndex >= particleCount) {
        return;
    }

    __local float4 sortedParticles[32];
    sortedParticles[particleStartIndex] = particles[particleStartIndex];

    uint localParticleCount = get_local_size(0);
    
    

    for (uint particleIndex = particleStartIndex; particleIndex < particleCount; particleIndex += localParticleCount) {

        __global float4* particle = &particles[particleIndex];
        
        float cellX = floor(particle->x / 10.0f);
        float cellY = floor(particle->y / 10.0f);
        float cellZ = floor(particle->z / 10.0f);
        
        particle->w = cellX + cellY * 10 + cellZ * 100;
        
        
        
//        particles[particleIndex].w = 100;
        
        
//        char4* particleProperties = &particle.w;
        
//        particleProperties[0] = 1;
//        particleProperties[2] = 1;
//        particleProperties[3] = 1;
//        particleProperties[4] = 1;
    }
} 




// barrier(CLK_LOCAL_MEM_FENCE);










__kernel void computeDistances(const __global float4* particles, uint particleCount, __global float* distances,
                               uint distancesPerParticle) {

    uint particleIndex = get_global_id(0);

    if (particleIndex >= particleCount) {
        return;
    }

    float4 particle = particles[particleIndex];

//    printf("particles: %d\n", particleCount);
                                
    for (uint neighborParticleIndex = particleIndex + 1; neighborParticleIndex < particleCount;
         neighborParticleIndex++) {

    
        printf("%d %d\n", particleIndex, neighborParticleIndex);

    
        float4 neighborParticle = particles[neighborParticleIndex];
    
   //     printf("%v3f\n", particle);
     //   printf("%v3f\n", neighborParticle);
        
        float4 distanceVector = particle - neighborParticle;
        distanceVector.w = 0;
                
//        float distanceSquared = dot(distanceVector, distanceVector);
    
   //     printf("%f\n", distanceSquared);

     //   printf("\n");
    }
} 






__kernel void findNeighbors(const __global float4* particles, uint particleCount,
                            __global float4* neighbors, uint neighborCount) {

    printf("particles: %d, neighbors: %d\n", particleCount, neighborCount);
                            
    float4 particle = particles[0];
    uint neighborIndex = 0;
    
    for (uint particleIndex = 1; particleIndex < particleCount; particleIndex++) {
    
        printf("%v4f\n", particle);
        printf("%v4f\n", particles[particleIndex]);
        
        float4 distanceVector = particle - particles[particleIndex];
        distanceVector.w = 0;
                
        float distanceSquared = dot(distanceVector, distanceVector);
    
        printf("%f\n", distanceSquared);

        printf("\n");
    
        if (neighborIndex >= neighborCount) {
            break;
        }
    }
} 


