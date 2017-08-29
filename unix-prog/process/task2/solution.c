#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <ctype.h>
#include <string.h>


bool is_pid_name(char const * name)
{
    while (*name) {
        if ( !isdigit(*name) )
            return false;
        ++name;
    }    
    return true;
}

bool is_process_entry(struct dirent * entry)
{
    return 
        entry != NULL
        && entry->d_type == DT_DIR
        && is_pid_name(entry->d_name);
}


int main(int arc, char ** argv)
{
    char const * name = "genenv";
    char const * procdirname = "/proc";
    int counter = 0;

    DIR * procdir = opendir(procdirname);
    if ( procdir == NULL ) {
        return -1;
    }

    struct dirent * entry;
    while ( NULL != (entry = readdir(procdir)) ) {
        if ( is_pid_name(entry->d_name) ) {
            char comm_file_name[256];
            sprintf(comm_file_name, "/proc/%s/comm", entry->d_name);
            FILE * commfile = fopen(comm_file_name, "r");
            if ( !commfile ) {
                return -2;
            }
            char processname[256];
            fscanf(commfile, "%s\n", processname);
            if ( 0 == strcmp(name, processname) ) {
                ++counter;
            }
        }
    }
    closedir(procdir);
    printf("%d\n", counter);

    return 0;
}

