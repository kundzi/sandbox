#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

typedef struct _pids_t {
    pid_t pid;
    pid_t ppid;
} pids_t;

int children_of(pid_t pid, pids_t pids[], int size)
{
    //printf("Looking for children of [%d]\n", pid);
    int count = 0;
    int i;
    for (i = 0; i < size; ++i) {
        if ( pids[i].ppid == pid) {
            //printf("%d %d\n", pids[i].pid, pids[i].ppid);
            pid_t childpid = pids[i].pid;
            pids[i].pid = -1;
            pids[i].ppid = -1;
            count += 1 + children_of(childpid, pids, size);
        }
    }
    return count;
}

int main(int argc, char ** argv)
{
    FILE * fp = popen("cat /proc/[0-9]*/status | grep -E '^Pid|^PPid'", "r");
    
    if ( !fp )
        return -1;

    pids_t pids[512];
    int i;
    for (i = 0; i < 512; i++) {
        pids[i].pid = -1;
        pids[i].ppid = -1;
    }

    int size = 0;
    while ( (EOF != fscanf(fp, "Pid: %d\n", &pids[size].pid)) 
         && (EOF != fscanf(fp, "PPid: %d\n", &pids[size].ppid)) ) {
        //printf("[pid %d ppid %d]\n", pids[size].pid, pids[size].ppid);
        size++;
    }
    fclose(fp);

    pid_t parent = atoi(argv[1]); 
    int count = 1 + children_of(parent, pids, size); 
    printf("%d\n", count);
    return 0;
}
