package main

import (
    "fmt"
    "time"
    "strings"
)

func say(s string) {
    for i := 0; i < 5; i++ {
        time.Sleep(100 * time.Millisecond)
        fmt.Println(s)
    }
}

func shout(s string) {
    for i := 0; i < 3; i++ {
    	time.Sleep(200 * time.Millisecond)
        fmt.Println(strings.ToUpper(s))
    }
}

func main() {
    go say("world")
    go shout("come on!")
    say("hello")
}