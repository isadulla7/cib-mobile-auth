import 'package:flutter/material.dart';
import '../data/auth_repository.dart';

class HomeScreen extends StatelessWidget {
  final AuthRepository repository;
  final VoidCallback onLogout;

  const HomeScreen({
    super.key,
    required this.repository,
    required this.onLogout,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('CIB'),
        actions: [
          IconButton(
            onPressed: () async {
              await repository.logout();
              onLogout();
            },
            icon: const Icon(Icons.logout),
          )
        ],
      ),
      body: const Center(child: Text('Authenticated')),
    );
  }
}
