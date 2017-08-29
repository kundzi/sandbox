#include <sys/socket.h>
#include <netinet/ip.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>

struct sockaddr_in local;

int server()
{
    int ss = socket(AF_INET, SOCK_STREAM, 0);
    int cs;

    inet_aton("127.0.0.1", &local.sin_addr);
    local.sin_port = htons(1234);
    local.sin_family = AF_INET;

    bind(ss, (struct sockaddr*)&local, sizeof(local));
    listen(ss, 5);

    printf("ss=%d\n", ss);

    cs = accept(ss, NULL, NULL);

    printf("cs=%d\n", cs);

    char buf[BUFSIZ];
    read(cs, buf, BUFSIZ);
    printf("%s\n", buf);
    close(cs);

    return 0;
}    

int client() {
    
    int s = socket(AF_INET, SOCK_STREAM, 0);

    inet_aton("127.0.0.1", &local.sin_addr);
    local.sin_port = htons(1234);
    local.sin_family = AF_INET;

    connect(s, (struct sockaddr*)&local, sizeof(local));

    char buf[BUFSIZ] = "Hello\n";
    write(s, buf, strlen(buf) + 1);
    close(s);

    return 0;
}


int main(int argc, char** argv)
{
    if (2 != argc) {
        return printf("Use: %s [s|c]\n", argv[0]);
    }

    switch (argv[1][0]) {
        case 's':
            server();
            break;
        case 'c':
            client();
            break;
        default:
            return printf("Argument %s is invalid\n", argv[1]);
    }

    return 0;
}
