// strace -o trace.txt ./main
// strace -o -e write trace.txt ./main
// more examples http://www.thegeekstuff.com/2011/11/strace-examples/ 

int main()
{
  char name[100];
  printf("What is your name? __");
  gets(name);
  return printf("Hello %s\n",name);
}
