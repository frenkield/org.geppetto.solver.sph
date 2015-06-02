


__kernel void hashAndSortParticles(__global float4* particles, uint particleCount, __local float4* localParticles) {


    uint localId = get_local_id(0);
    uint localSize = get_local_size(0);

    uint groupId = get_group_id(0);
    uint numGroups = get_num_groups(0);

    uint globalSize = get_global_size(0);


    uint particleIndex = groupId * localSize + localId;

//    printf("%d %d %d %d %d\n", localId, localSize, groupId, numGroups, globalSize);



    if (particleIndex >= particleCount) {
        return;
    }


    
    localParticles[localId] = particles[particleIndex];
    
//    __global float4* particle = &particles[particleIndex];
    
    float cellX = floor(localParticles[localId].x / 10.0f);
    float cellY = floor(localParticles[localId].y / 10.0f);
    float cellZ = floor(localParticles[localId].z / 10.0f);
    
    localParticles[localId].w = cellX + cellY * 10 + cellZ * 100;


    barrier(CLK_LOCAL_MEM_FENCE);

    particles[particleIndex] = localParticles[localId];

        

        
        
        
        
        
//        particles[particleIndex].w = 100;
        
        
//        char4* particleProperties = &particle.w;
        
//        particleProperties[0] = 1;
//        particleProperties[2] = 1;
//        particleProperties[3] = 1;
//        particleProperties[4] = 1;
    
} 











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


