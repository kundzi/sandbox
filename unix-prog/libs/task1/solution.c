#include <string.h>

int stringStat(const char * string, int mulitplier, int *count)
{
        *count += 1;
        return strlen(string)*mulitplier;
}
