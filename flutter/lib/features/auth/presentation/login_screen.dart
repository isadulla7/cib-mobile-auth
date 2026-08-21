import 'package:flutter/material.dart';
import '../data/auth_repository.dart';

class LoginScreen extends StatefulWidget {
  final AuthRepository repository;
  final VoidCallback onAuthenticated;
  final VoidCallback onRegister;

  const LoginScreen({
    super.key,
    required this.repository,
    required this.onAuthenticated,
    required this.onRegister,
  });

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final phone = TextEditingController();
  final password = TextEditingController();
  final otp = TextEditingController();
  String? challengeId;
  String? error;
  bool busy = false;

  Future<void> submit() async {
    setState(() {
      busy = true;
      error = null;
    });

    try {
      if (challengeId == null) {
        final digits = phone.text.replaceAll(RegExp(r'\D'), '');
        final normalized = digits.startsWith('998') ? '+$digits' : '+998$digits';
        final result = await widget.repository.login(normalized, password.text);
        if (result.status == 'AUTHENTICATED') {
          widget.onAuthenticated();
          return;
        }
        setState(() => challengeId = result.challengeId);
      } else {
        final result = await widget.repository.verifyLoginOtp(
          challengeId!,
          otp.text.trim(),
        );
        if (result.status == 'AUTHENTICATED') {
          widget.onAuthenticated();
        }
      }
    } catch (e) {
      if (mounted) setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  void dispose() {
    phone.dispose();
    password.dispose();
    otp.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('CIB Login')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          if (challengeId == null) ...[
            TextField(
              controller: phone,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(
                labelText: 'Telefon',
                prefixText: '+998 ',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 14),
            TextField(
              controller: password,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Parol',
                border: OutlineInputBorder(),
              ),
            ),
          ] else ...[
            const Text('Yangi qurilma. SMS orqali yuborilgan OTP kodni kiriting.'),
            const SizedBox(height: 14),
            TextField(
              controller: otp,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'OTP',
                border: OutlineInputBorder(),
              ),
            ),
          ],
          if (error != null) ...[
            const SizedBox(height: 12),
            Text(error!, style: const TextStyle(color: Colors.red)),
          ],
          const SizedBox(height: 18),
          FilledButton(
            onPressed: busy ? null : submit,
            child: Text(challengeId == null ? 'Kirish' : 'OTP tasdiqlash'),
          ),
          if (challengeId == null)
            TextButton(
              onPressed: widget.onRegister,
              child: const Text('Ro‘yxatdan o‘tish'),
            ),
        ],
      ),
    );
  }
}
