//#include "hello.h"


//  gcc -o hello.o -c hello.c # compileobjectfiles
//  gcc -o main.o -c main.c
//  gcc -o hello hello.o main.o


// gcc -o libHello.so -shared -fPIC hello.c #compilesharedlib
// nm libHello.sh #showsymbols
// gcc main.c -L. -lHello -o hello #compilewithsharedlib
// export LD_LIBRARY_PATH=. #runwithlibspath
// export DYLD_FALLBACK_LIBRARY_PATH= #formac


// try cpp!
// c++filt __Z13hello_messagePKc #showsignature

// otool -L hello #dependenciesLDD

#include <stddef.h>
#include <stdbool.h>
#include <stdio.h>
#include <dlfcn.h>


void (*hello_message)(const char *); 

bool init_library() 
{
    void* hdl = dlopen("./libHello.so", RTLD_LAZY);
    if (hdl) {
        hello_message = (void(*)(const char * ))dlsym(hdl, "hello_message");
        return hello_message != NULL;
    } else {
        return false;
    }
}

int main()
{
    if (init_library()) {
        hello_message("Vasya"); // segmentation fault  ./hello
    } else {
        printf("Library was not loaded\n");
    }
    return 0;
}
