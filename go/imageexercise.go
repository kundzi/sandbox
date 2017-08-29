package main

import (
    "code.google.com/p/go-tour/pic"
    "image"
    "image/color"
)

type Image struct {
	data   [][]uint8
    width  int
    height int
}

func (i Image) ColorModel() color.Model {
    return nil
}

func (i Image) Bounds() image.Rectangle {
	return image.Rect(0, 0, i.width, i.height)
}

func (i Image) At(x, y int) color.Color {
    v := i.data[x][i.height - y - 1]
    return color.RGBA{v, v, 255, 255}
}

func CreateImage(w, h int) Image {
    img := Image {nil, w, h}
    
    img.data = make([][]uint8, w)
    for i := 0; i < w; i++ {
    	img.data[i] = make([]uint8, h)
    }
    
    for x:= 0; x < w; x++ {
        for y := 0; y < h; y++ {
        	img.data[x][h - y - 1] = uint8(x*y)
        }
    }
    
    return img
}

func main() {
    m := CreateImage(256, 256)
    pic.ShowImage(&m)
}