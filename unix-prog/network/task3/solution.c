#include <sys/socket.h>
#include <netinet/ip.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

//#define logd(x,...) printf((x), __VA_ARGS__)
#define logd(x,...)


int compare_chars_desc(const void* l, const void* r)
{
    return (*(const char*)r) - (*(const char*)l);
}

struct sockaddr_in local;

int main(int argc, char** argv)
{
    if (2 != argc) {
        printf("port must be provided: %s <port>\n", argv[0]);
        return -1;
    }

    const int port = atoi(argv[1]);
    if (0 == port) {
        printf("not valid port value %s\n", argv[1]);
        return -1;
    }

    const char* LOCALHOST = "127.0.0.1";
    const char* END = "OFF";

    int ss = socket(AF_INET, SOCK_STREAM, 0);
    logd("ss=%d\n", ss);
    
    inet_aton(LOCALHOST, &local.sin_addr);
    local.sin_port = htons(port);
    local.sin_family = AF_INET;

    int bind_result = bind(ss, (struct sockaddr*)&local, sizeof(local));
    logd("bind result %d\n", bind_result);
    int listen_result = listen(ss, 5);
    logd("listen result %d\n", listen_result);

    int cs = accept(ss, NULL, NULL);
    logd("cs=%d\n", cs);

    const size_t SIZE = 10000;
    char buf[SIZE];
    for (;;) {
        const size_t count = read(cs, buf, SIZE);
        logd("read count %zu\n", count);

        int cmp = strncmp(END, buf, strlen(END));
        logd("cmp %d\n", cmp);
        if (0 == cmp)
            break;

        qsort(buf, count, sizeof(char), compare_chars_desc);
        const size_t write_result = write(cs, buf, count);
        logd("wrote count %zu\n", write_result);
    }
    
    return 0;
}
