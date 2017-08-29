#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/ip.h>
#include <arpa/inet.h>
#include <string.h>
#include <unistd.h>

// #define logd(x,...) printf((x), __VA_ARGS__)
#define logd(x,...)

int main(int argc, char** argv)        
{
    if (2 != argc) {
        printf("Port must be specified: %s <port>\n", argv[0]);
        return -1;
    }

    const static size_t SIZE = 10000;
    const static char* END = "OFF\n";
    const static char* LOCALHOST = "127.0.0.1";
    const int port = atoi(argv[1]);

    if (0 == port) {
        printf("%s is not a valid port value\n", argv[1]);
        return -1;
    }

    struct sockaddr_in local;

    const int s = socket(AF_INET, SOCK_DGRAM, 0);
    logd("using socket %d\n", s);

    inet_aton(LOCALHOST, &local.sin_addr);
    local.sin_port = htons(port);
    local.sin_family = AF_INET;

    int bind_result = bind(s, (struct sockaddr*)&local, sizeof(local));
    logd("bind result %d\n", bind_result);

    char buf[SIZE];

    for (;;) {
        size_t count_read = read(s, buf, SIZE);
        buf[count_read] = '\0';
        logd("read %zu\n", count_read);
        if (0 == strcmp(buf, END)) {
            break;
        } else {
            printf("%s\n", buf);
        }
    }

    return 0; 
}
