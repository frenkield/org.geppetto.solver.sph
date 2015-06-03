


__kernel void hashParticlesDoubleStride(__global float4* particles, uint particleCount) {


    uint localId = get_local_id(0);
    uint localSize = get_local_size(0);
    uint groupId = get_group_id(0);
    uint numGroups = get_num_groups(0);

    uint rowLength = 2048;
    uint rowCount = particleCount / rowLength;
    uint startRow = groupId;

    uint particlesPerGroup = particleCount / numGroups;


//    if (startIndex == 0) {
    //    printf("%d %d %d\n", rowLength, rowCount, startRow);   
//    }


    for (uint rowIndex = startRow; rowIndex < rowCount; rowIndex += numGroups) {
            
        for (uint columnIndex = localId; columnIndex < rowLength; columnIndex += localSize) {

            uint particleIndex = rowIndex * rowLength + columnIndex;
    
    
//            if (particleIndex > 15000 && particleIndex < 16000) {
//                printf("%d %d %d\n", particleIndex, rowIndex, columnIndex);   
//            }
    
            __global float4* particle = &particles[particleIndex];
            
            float cellX = floor(particle->x / 10.0f);
            float cellY = floor(particle->y / 10.0f);
            float cellZ = floor(particle->z / 10.0f);
            
            particle->w = cellX + cellY * 10 + cellZ * 100;
    
        }
    }
}







__kernel void hashParticlesSingleStride(__global float4* particles, uint particleCount) {


    uint localId = get_local_id(0);
    uint localSize = get_local_size(0);

    uint groupId = get_group_id(0);
    uint numGroups = get_num_groups(0);


    uint particlesPerGroup = particleCount / numGroups;
    
    uint startIndex = particlesPerGroup * groupId + localId;
    uint endIndex = startIndex + particlesPerGroup - localId;
    
    
    if (startIndex == 0) {
        printf("%d %d %d %d %d\n", particlesPerGroup, startIndex, endIndex, numGroups, localSize);   
    }
    
    for (uint particleIndex = startIndex; particleIndex < endIndex; particleIndex += localSize) {

        __global float4* particle = &particles[particleIndex];
        
        float cellX = floor(particle->x / 10.0f);
        float cellY = floor(particle->y / 10.0f);
        float cellZ = floor(particle->z / 10.0f);
        
        particle->w = cellX + cellY * 10 + cellZ * 100;

    }
} 













__kernel void hashParticlesSimple(__global float4* particles, uint particleCount) {

    uint localId = get_local_id(0);
    uint localSize = get_local_size(0);

    uint groupId = get_group_id(0);
    uint numGroups = get_num_groups(0);

    uint globalId = get_global_id(0);
    uint globalSize = get_global_size(0);

    uint particleIndex = globalId;

    if (particleIndex < 5 || particleIndex > 3199997) {
        printf("%d %d %d %d\n", particleIndex, localId, localSize, numGroups);
    }

    __global float4* particle = &particles[particleIndex];
    
    float cellX = floor(particle->x / 10.0f);
    float cellY = floor(particle->y / 10.0f);
    float cellZ = floor(particle->z / 10.0f);
    
    particle->w = cellX + cellY * 10 + cellZ * 100;
} 
















__kernel void hashAndSortParticles(__global float4* particles, uint particleCount, __local float4* localParticles) {


    uint localId = get_local_id(0);
    uint localSize = get_local_size(0);

    uint groupId = get_group_id(0);
    uint numGroups = get_num_groups(0);

    uint globalSize = get_global_size(0);

    uint startIndex = groupId * localSize + localId;

    for (uint particleIndex = localId; particleIndex < particleCount; particleIndex += localSize) {

    //    printf("%d %d %d %d %d\n", localId, localSize, groupId, numGroups, globalSize);
    
    
    
    //    if (particleIndex >= particleCount) {
    //        return;
    //    }
    //
        
        
        __global float4* particle = &particles[particleIndex];
        
        float cellX = floor(particle->x / 10.0f);
        float cellY = floor(particle->y / 10.0f);
        float cellZ = floor(particle->z / 10.0f);
        
        particle->w = cellX + cellY * 10 + cellZ * 100;
    
    
    }





//    localParticles[localId] = particles[particleIndex];

//    float cellX = floor(localParticles[localId].x / 10.0f);
//    float cellY = floor(localParticles[localId].y / 10.0f);
//    float cellZ = floor(localParticles[localId].z / 10.0f);
//    
//    localParticles[localId].w = cellX + cellY * 10 + cellZ * 100;
//
//
//    barrier(CLK_LOCAL_MEM_FENCE);
//
//    particles[particleIndex] = localParticles[localId];

        

        
        
        
        
        
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


