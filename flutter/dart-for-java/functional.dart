// class WannabeFunction {
//   call(String a, String b, String c) => '$a $b $c!';
// }

// main() {
//   var wf = new WannabeFunction();
//   var out = wf("Hi","there,","gang");
//   print('$out');
// }

String scream(int length) => "A${'a' * length}h!";

main() {
  final List<int> values = [1, 2, 3, 5, 10, 50];

  values.skip(1).take(3).map(scream).forEach(print);

  final int sum =
      values.fold(0, (previousValue, element) => previousValue + element);
  print(sum);

  final String joined = values.join(' blyat ');
  print(joined);
}
