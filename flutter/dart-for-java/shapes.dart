import 'dart:math';

// option 1
Shape shapeFactory(String type) {
  switch (type) {
    case 'circle':
      return Circle(2);
      break;
    case 'square':
      return Square(2);
      break;
    default:
      throw 'Can\'t create $type';
  }
}

abstract class Shape {
  // option 2
  factory Shape(String type) {
    switch (type) {
      case 'circle':
        return Circle(2);
        break;
      case 'square':
        return Square(2);
        break;
      default:
        throw 'Can\'t create $type';
    }
  }

  num get area;
}

class Circle implements Shape {
  final num radius;
  Circle(this.radius);
  num get area => pi * pow(radius, 2);
}

class Square implements Shape {
  final num side;
  Square(this.side);
  num get area => pow(side, 2);
}

main() {
  try {
    print(shapeFactory('circle'));
    print(shapeFactory('square'));
    print(shapeFactory('cat'));
  } catch (err) {
    print(err);
  }

  try {
    print(Shape('circle'));
    print(Shape('square'));
    print(Shape('cat'));
  } catch (err) {
    print(err);
  }
}
