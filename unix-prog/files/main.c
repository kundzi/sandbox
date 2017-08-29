#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>

int main(int c, char ** v)
{
    char buf[100];
    size_t sz = 0;
    if (c != 2)
    {
            printf("\nPlease call\n\t%s filename\n", v[0]);
            return 1;
    }
    int fd = open(v[1], O_RDONLY|O_SYMLINK);
    perror("fd");
    printf("fd=%d\n", fd);
    sz = read(fd, buf, 100);

    printf("sz=%ld\n", sz);
}
