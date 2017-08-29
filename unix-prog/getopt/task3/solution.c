#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <string.h>

int main(int c, char ** v)
{
	int ok_args = 1;
	opterr = 0;
	struct option opts[] = {
		{"query", required_argument, 0, 'q'},
		{"longinformationrequest", no_argument, 0, 'i'},
		{"version", no_argument, 0, 'v'},
		{0,0,0,0}
	};
	
	char const * short_args = "q:iv";

	int r = -1;

	while (ok_args && (-1 != (r = getopt_long(c, v, short_args, opts, NULL)) ) )
	{
		//printf("%c %d\n", r, r);
		switch (r) 
		{
			case 'q': break;
			case 'i': break;
			case 'v': break;
			case '?':
			default:
				ok_args = 0;
		}
	}

	printf("%c\n", ok_args ? '+' : '-');

	return 0;
}
