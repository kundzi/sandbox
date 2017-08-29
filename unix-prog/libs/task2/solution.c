#include <stdbool.h>
#include <stdio.h>
#include <dlfcn.h>
#include <stddef.h>
#include <stdlib.h>


int (*secret_function)(int);

bool load_library(const char * libname, const char * funcname)
{
		void* hdl = dlopen(libname, RTLD_LAZY);
		if (hdl) {
				secret_function = (int(*)(int)) dlsym(hdl, funcname);
				return secret_function != NULL;
		} else {
				return false;
		}
}

int main(int argc, char ** argv)
{
		const char * libname = argv[1];
		const char * funcname = argv[2];
		const int argument = atoi(argv[3]);
	   if (load_library(libname, funcname)) {
			const int result = secret_function(argument);
			printf("%d\n", result);
			return 0;
	   } else {
			printf("We are fucked %s %s %d \n", libname, funcname, argument);
			return -1;
	   }	   
}

