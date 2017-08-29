#include <fcntl.h>
#include <stdio.h>
#include <sys/select.h>
#include <unistd.h>

#define MAX(x,y) ((x) > (y) ? x : y)
#define NELEMS(x) (sizeof(x)/sizeof((x)[0]))
//#define logd(x,...) printf((x), __VA_ARGS__  )
#define logd(x,...) 

int read_sum(char* in1, char* in2)
{
    int fd1 = open(in1, O_RDONLY);
    int fd2 = open(in2, O_RDONLY);
    
    if (fd1 < 0 || fd2 < 0)
        return -1;

    int fdx[] = { fd1, fd2 };

    int sum = 0;
    int num_closed = 0;
    while (num_closed < 2) {
        fd_set fds;
        int maxfd = MAX(fd1, fd2);
        int res;
        char buf[256];

        FD_ZERO(&fds);
        FD_SET(fd1, &fds);
        FD_SET(fd2, &fds);

        select(maxfd + 1, &fds, NULL, NULL, NULL);
        
        for (int i = 0; i < NELEMS(fdx); ++i) {
            int fd = fdx[i]; 
            if (FD_ISSET(fd, &fds)) {
                res = read(fd, buf, sizeof(buf));
                logd("buf: [%s]\n", buf);
                if (res > 0) {
                    int value = 0;
                    res = sscanf(buf, "%d", &value);
                    if (!res) {
                        return -3;
                    }
                    sum += value;
                    logd("read %d from %d sum %d\n", value, fd, sum);
                } else if (res == 0) {
                    ++num_closed;
                } else {
                    return -2;
                }
            }
        } 

        
    }

    close(fd1);
    close(fd2); 
    
    return sum;
}

int main(int argc, char** argv)
{
    int sum = read_sum("in1", "in2");
    printf("%d\n", sum);
    return 0;
}
