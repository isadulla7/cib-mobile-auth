import 'package:flutter/material.dart';
import 'core/api/api_client.dart';
import 'core/security/token_store.dart';
import 'features/auth/data/auth_repository.dart';
import 'features/auth/presentation/home_screen.dart';
import 'features/auth/presentation/login_screen.dart';
import 'features/auth/presentation/registration_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const CibApp());
}

class CibApp extends StatefulWidget {
  const CibApp({super.key});

  @override
  State<CibApp> createState() => _CibAppState();
}

class _CibAppState extends State<CibApp> {
  late final TokenStore tokenStore;
  late final AuthRepository auth;
  String route = 'login';

  @override
  void initState() {
    super.initState();
    tokenStore = TokenStore();
    const baseUrl = String.fromEnvironment(
      'API_BASE_URL',
      defaultValue: 'http://10.0.2.2:8080',
    );
    final api = ApiClient(baseUrl: baseUrl, tokens: tokenStore);
    auth = AuthRepository(api, tokenStore);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CIB Mobile',
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.blue),
      home: switch (route) {
        'register' => RegistrationScreen(
            repository: auth,
            onAuthenticated: () => setState(() => route = 'home'),
          ),
        'home' => HomeScreen(
            repository: auth,
            onLogout: () => setState(() => route = 'login'),
          ),
        _ => LoginScreen(
            repository: auth,
            onAuthenticated: () => setState(() => route = 'home'),
            onRegister: () => setState(() => route = 'register'),
          ),
      },
    );
  }
}
