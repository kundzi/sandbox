#define _XOPEN_SOURCE

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <linux/limits.h>
#include <signal.h>

#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>

void handle(int signo)
{
    if (SIGURG == signo) {
        exit(EXIT_SUCCESS);
    }
}


int main(void)
{
    pid_t pid;
    pid = fork();
    if (pid == -1) {
        return -1;
    } else if ( pid != 0 ) {
        return 0;
    }

    pid_t sid;
    if ( -1 == (sid = setsid()) )
        return -1;
    
    if ( chdir("/") == -1 )
        return -1;

    printf("%d\n", sid);
    printf("%d\n", getpid());


    int i;
    for ( i = 0; i < NR_OPEN; ++i )
        close(i); 

    open("/dev/null", O_RDWR);
    dup(0);
    dup(0);

    struct sigaction sa;
    sa.sa_handler = handle;
    if (sigemptyset(&sa.sa_mask) != 0) {
       return -1;
    }

    if (sigaction(SIGURG, &sa, NULL) != 0) {
        return -2;
    }

    for (;;) {
        pause();
    }

    return 0;
}
