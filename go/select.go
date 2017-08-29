package main

import "fmt"


func serve(c, s, q chan int) {
    for {
        x := 0;
        select {
        case x = <- c:
            fmt.Println("Client", x)
        case x = <- s:
            fmt.Println("Serving", x)
        case <- q:
            fmt.Println("Quit")
            return
        }
    }
}

func fibonacci(c, quit chan int) {
    x, y := 0, 1
    for {
        select {
        case c <- x:
            x, y = y, x+y
        case <-quit:
            fmt.Println("quit")
            return
        }
    }
}

func main() {
// c := make(chan int)
//     quit := make(chan int)
//     go func() {
//         for i := 0; i < 10; i++ {
//             fmt.Println(<-c)
//         }
//         quit <- 0
//     }()
//     fibonacci(c, quit)
    c, s, q := 
        make(chan int),
        make(chan int),
        make(chan int)
    
    go func() {
        for i := 0; i < 13; i++ {
            s <- 13 - i
            c <- i
        }
        q <- 0
    }()
    
    serve(c, s, q)
    
    
}