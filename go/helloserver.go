package main

import (
    "fmt"
    "net/http"
)

type HelloHandler struct{}

func (h HelloHandler) ServeHTTP(w http.ResponseWriter,r *http.Request) {
    fmt.Fprint(w, "Hello!")
}

func main() {
    var h HelloHandler
    http.ListenAndServe("localhost:4000", h)
}