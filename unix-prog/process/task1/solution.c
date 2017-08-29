#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>


int get_path_for_pid(char * path, pid_t pid)
{
    return sprintf(path, "/proc/%d/status", pid);
}

pid_t get_parent_pid(pid_t pid)
{
    char path[256];
    if ( get_path_for_pid(path, pid) == -1) {
        return -1; // error here
    }
    FILE * proc_file = fopen(path, "r");
    if (!proc_file) {
        return -2; // error with file
    }
    // finding a line
    char * ppid_line = NULL;
    char line[512];
    while ( fgets(line, sizeof line, proc_file) != NULL) {
        if ( strstr(line, "PPid") ) {
            ppid_line = line;
            break; 
        }
    }
    if (!ppid_line) {
        return -3; // error line not found
    }
    
    pid_t ppid;
    if ( 1 != sscanf(ppid_line, "PPid: %d\n", &ppid) ) {
        return -4;
    }
    return ppid;
}

int main(int arc, char ** argv)
{
    pid_t pid = getpid();
    pid_t ppid = get_parent_pid(pid);
    printf("%d\n", ppid);

    return 0;
}

