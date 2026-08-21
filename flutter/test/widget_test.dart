import 'package:cib_mobile/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('shows the login screen on startup', (tester) async {
    await tester.pumpWidget(const CibApp());
    await tester.pump();

    expect(find.text('CIB Login'), findsOneWidget);
    expect(find.text('Kirish'), findsOneWidget);
    expect(find.text('Ro‘yxatdan o‘tish'), findsOneWidget);
  });
}
