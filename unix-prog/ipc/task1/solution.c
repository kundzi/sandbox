#include <stdio.h>

int main(int argc, char** argv)
{
    char* prog = argv[1];
    char* progarg = argv[2];

    char command[256];

    if (sprintf(command, "./%s %s", prog, progarg) <= 0)
        return 1;

    FILE * output = popen(command, "r");

    if (!output)
        return 2;
    
    int count = 0;
    int ch;

    while ( EOF != (ch = fgetc(output))) {
        if ('0' == ch) ++count;        
    }

    pclose(output);
    
    printf("%d\n", count);

    return 0;
}
