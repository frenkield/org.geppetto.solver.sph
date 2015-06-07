//#if CONFIG_USE_VALUE
//typedef uint2 data_t;
//#define getKey(a) ((a).x)
//#define getValue(a) ((a).y)
//#define makeData(k,v) ((uint2)((k),(v)))
//#else

typedef uint data_t;
#define getKey(a) (a)
#define getValue(a) (0)
#define makeData(k,v) (k)

//#endif


#define ORDERV(x,a,b) { \
    bool swap = reverse ^ (getKey(x[a])<getKey(x[b])); \
    data_t auxa = x[a]; data_t auxb = x[b]; \
    x[a] = (swap)?auxb:auxa; x[b] = (swap)?auxa:auxb; \
}
      
#define B2V(x,a) { ORDERV(x,a,a+1) }

#define B4V(x,a) { for (int i4=0;i4<2;i4++) { ORDERV(x,a+i4,a+i4+2) } B2V(x,a) B2V(x,a+2) }


__kernel void ParallelBitonic_C4(__global data_t* data, int inc0, int dir, __local data_t* aux) {
  
    int t = get_global_id(0);
  
 // printf("%d\n", data[t]);
  
  
  int wgBits = 4 * get_local_size(0) - 1;
  int inc, low, i;
  bool reverse = true;
  data_t x[4];

  inc = inc0 >> 1;
  low = t & (inc - 1);
  i = ((t - low) << 2) + low;
  
  
//  printf("%d % d %d\n", dir, i, reverse);
  
  for (int k = 0; k < 4; k++) {
      x[k] = data[i + k * inc];
   //   printf("%d\n", i + k * inc);
  }

     // printf("before %d %d %d %d\n", x[0], x[1], x[2], x[3]);
  
  
  B4V(x,0);

//  for (int k = 0; k < 4; k++) {
     // printf("after %d %d %d %d\n", x[0], x[1], x[2], x[3]);
  //}

  
  for (int k=0;k<4;k++) aux[(i+k*inc) & wgBits] = x[k];
  barrier(CLK_LOCAL_MEM_FENCE);

  for ( ;inc > 1; inc >>= 2) {
  
    low = t & (inc - 1);
    i = ((t - low) << 2) + low;

    for (int k=0;k<4;k++) x[k] = aux[(i+k*inc) & wgBits];

    B4V(x,0);

    barrier(CLK_LOCAL_MEM_FENCE);
    for (int k=0;k<4;k++) aux[(i+k*inc) & wgBits] = x[k];

    barrier(CLK_LOCAL_MEM_FENCE);
  }

  // Final iteration, local input, global output, INC=1
  i = t << 2;

  for (int k=0;k<4;k++) x[k] = aux[(i+k) & wgBits];


  B4V(x,0);



  for (int k=0;k<4;k++) data[i+k] = x[k];
}