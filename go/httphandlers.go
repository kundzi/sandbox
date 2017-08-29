package main

import (
    "net/http"
    "fmt"
)

type Struct struct {
    Greeting string
    Punct    string
    Who      string
}

func (s * Struct) ServeHTTP(w http.ResponseWriter, r *http.Request) {
    fmt.Fprint(w, s)
}

type String string

func (s String) ServeHTTP(w http.ResponseWriter, r *http.Request) {
    fmt.Fprint(w, s)
}

func main() {
    http.Handle("/string", String("I'm a frayed knot."))
    http.Handle("/struct", &Struct{"Hello", ":", "Gophers!"})
    
    http.ListenAndServe("localhost:4000", nil)
}