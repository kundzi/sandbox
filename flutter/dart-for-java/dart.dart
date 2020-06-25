class Bicycle {
  Bicycle(this.cadance, this.gear, this._p);

  int cadance;
  int _speed = 0;
  int gear;
  int _p;

  int get speed => _speed;

  void applyBrake(int decrement) {
    _speed -= decrement;
  }

  void speedUp(int increment) {
    _speed += increment;
  }

  @override
  String toString() => 'Bicycle: $_speed km/h $_p';
}

void main(List<String> args) {
  print("object");
  var bike = Bicycle(2, 1, 1);
  final finalBike = Bicycle(6, 1, 2);
  print(bike);
  print(finalBike);
}

