#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>

int main(int argc, char** argv) {
    if (2 != argc) {
        perror("need an address as an argument\n");
        return -1;
    }

    struct hostent *h;
    h = gethostbyname(argv[1]);
    
    if (NULL == h) {
        perror("failed to gethostbyname()\n");
        return -1;
    }

    printf("Canonical name: %s\n", h->h_name);
    printf("Type = %s len %d\n", (h->h_addrtype == AF_INET) ? "ipv4" : "ipv6", h->h_length);

    int i;
    for (i = 0; NULL != h->h_addr_list[i]; ++i) {
        struct in_addr *a = (struct in_addr*) h->h_addr_list[i];
        printf("%s\n", inet_ntoa(*a));
    }


    return EXIT_SUCCESS;
}

