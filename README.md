# ArrayBench

```shell
$ java -jar arraybench-1.0-SNAPSHOT.jar --help
usage: ArrayBench [-h] [-l LENGTH]
                  [-p COUNT]
                  [-r COUNT] SORT_FILE

Benching program for ArrayV

positional arguments:
  SORT_FILE              The     .java
                         file  of  the
                         sort

named arguments:
  -h, --help             show     this
                         help  message
                         and exit
  -l LENGTH, --length LENGTH
                         The    length
                         of        the
                         array.
                         Default    is
                         1024
                         (default:
                         1024)
  -p COUNT, --pre-reps COUNT
                         The    number
                         of  times  to
                         run the  sort
                         without
                         benching.
                         This    helps
                         the      JIT.
                         Default is  1
                         (default: 1)
  -r COUNT, --reps COUNT
                         The    number
                         of  times  to
                         run the  sort
                         benching.
                         Default is  3
                         (default: 3)
```
