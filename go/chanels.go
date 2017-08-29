package main

import "fmt"
import "math"

func sum(a []int, c chan int) {
    sum := 0
    for _, v := range a {
        sum += v
    }
    c <- sum // send sum to c
}

func min(a []int, c chan int) {
    min := math.MaxInt32
    for _, v := range a {
        if min > v {
        	min = v    
        }
    }
    c <- min
}

func main() {
    a := []int{7, 2, 8, -9, 4, 0}

    c := make(chan int)
    go sum(a[:len(a)/2], c)
    go sum(a[len(a)/2:], c)
    x, y := <-c, <-c // receive from c
    
    fmt.Println(x, y, x+y)
    
    go min(a[:len(a)/2 ], c)
    go min(a[ len(a)/2:], c)
    x, y = <- c, <- c
   	fmt.Println(x, y)
}