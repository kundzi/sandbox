#define _XOPEN_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <sys/shm.h>
#include <unistd.h>

int main(int argc, char** argv)
{
    const size_t SZ = 1000;
    key_t mem_key1;
    key_t mem_key2;
    if (1 != sscanf(argv[1], "%d", &mem_key1))
        return -1;
    if (1 != sscanf(argv[2], "%d", &mem_key2))
        return -2;

    int mem_id1 = shmget(mem_key1, SZ, 0666);
    if (mem_id1 < 0)
        return -1;
    int mem_id2 = shmget(mem_key2, SZ, 0666);
    if (mem_id2 < 0)
        return -2; 

    int* ix1 = (int*)shmat(mem_id1, NULL, 0);
    if ((int*)-1 == ix1)
        return -3;
    int* ix2 = (int*)shmat(mem_id2, NULL, 0);
    if ((int*)-1 == ix2)
        return -4;

    // we are done attaching the regions
    key_t sum_mem_key = 19900604;
    int sum_mem_id = shmget(sum_mem_key, SZ, IPC_CREAT | 0666);
    if (sum_mem_key < 0)
        return -5;
    
    int* sumx = (int*)shmat(sum_mem_id, NULL, 0);
    if ((int*)-1 == sumx)
        return -6;

    for (int i = 0; i < 100; ++i) {
        *sumx++ = *ix1++ + *ix2++;
    }
    *sumx = 0;

    /*
     *  ipcs -m 
     *  ipcrm shm 1627649
     *  ./solution 1 2; echo $? # shows result of main()
     */

    printf("%d\n", sum_mem_key);

    // sleep(10);

    return 0;
}
