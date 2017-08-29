package main

import (
    "io"
    "os"
    "strings"
)

type rot13Reader struct {
    r io.Reader
}

func (rr *rot13Reader) Read(p []byte) (n int, err error) {
    n, err = rr.r.Read(p)
    for i := 0; i < n; i++ {
        c := p[i]
        if c >= 'a' {
        	p[i] = ((c - 'a'+ 13) % 26) + 'a'    
        } else if c >= 'A' {
            p[i] = ((c - 'A' + 13) % 26) + 'A'
        }
    }
    return
}

func main() {
    s := strings.NewReader(
        "Lbh penpxrq gur pbqr!")
    r := rot13Reader{s}
    io.Copy(os.Stdout, &r)
}