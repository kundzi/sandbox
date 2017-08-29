#include <stdio.h>
#include <sys/socket.h>
#include <netinet/ip.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <unistd.h>


struct sockaddr_in local;

int main(int argc, char** argv)
{
    int s = socket(AF_INET, SOCK_DGRAM, 0);
    printf("socket = %d\n", s);

    inet_aton("127.0.0.1", &local.sin_addr);
    local.sin_port = htons(1234); // host to network short
    local.sin_family = AF_INET;

    int result = bind(s, (struct sockaddr*)&local, sizeof(local));
    printf("%d\n", result);

    char buf[BUFSIZ];
    size_t count = read(s, buf, BUFSIZ);
    buf[count - 1] = '\0'; // git rid of new line
    printf("%s, bye\n", buf);

    return EXIT_SUCCESS;
}
