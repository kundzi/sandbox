#define _XOPEN_SOURCE

#include <signal.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/signal.h>

//#define logd(x,...) printf((x), __VA_ARGS__  )
#define logd(x,...) 

void handler(int signo)
{
    logd("got signal: [%d]\n", signo);
    static int num_sig1 = 0;
    static int num_sig2 = 0;
    if (SIGUSR1 == signo) {
        ++num_sig1;
    } else if (SIGUSR2 == signo) {
        ++num_sig2;
    } else if (SIGTERM == signo) {
        printf("%d %d\n", num_sig1, num_sig2);
        exit(EXIT_SUCCESS);
    }
}

int main(int argc, char** argv)
{
    logd("pid [%d]\n", getpid());
    
//    signal(SIGUSR1, handler);
//    signal(SIGUSR2, handler);
//    signal(SIGTERM, handler);
    struct sigaction new_action;
    new_action.sa_handler = handler;
    sigemptyset(&new_action.sa_mask);
    new_action.sa_flags = 0;

    sigaction(SIGUSR1, &new_action, NULL); 
    sigaction(SIGUSR2, &new_action, NULL); 
    sigaction(SIGTERM, &new_action, NULL); 

    for (;;) {
        pause();
    }

    return 0;
}
