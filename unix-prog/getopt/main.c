#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <string.h>

int main(int c, char ** v)
{
    int is_brackets = 0;
    int opindex = 0;
    int opchar = 0;
    char string[] = "Сам ты хуй и хуй твоя собака!";
    int length = 0;

    struct option opts[] = {
        {"brackets", no_argument, &is_brackets, 1},
        {"length", required_argument, 0, 'l'},
        {"printauthorname", no_argument, 0, 'p'},
        {0,0,0,0},
    };

    while ( -1 != (opchar = getopt_long(c , v, "bl:pq", opts, &opindex)) )
    {
        switch (opchar)
        {
            case 0:
                    printf("0\n");
                    break;
            case 'l':
                    length = atoi(optarg);
                    printf("length=%d\n", length);
                    break;
            case 'p':
                    printf("(c) Dima Huinin\n");
                    break;
            default:
                    printf("optchar: %c\n", opchar);

        }
    }
    if (strlen(string) > length && 0 != length)
            string[length] = '\0';
    if (is_brackets)
            printf("[%s]\n", string);
    else
            printf("%s\n", string);


    printf("Done %d\n", opterr);

    return 0;
}
