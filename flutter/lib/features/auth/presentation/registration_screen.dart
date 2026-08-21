import 'package:flutter/material.dart';
import '../data/auth_repository.dart';
import '../models/auth_models.dart';
import 'widgets/dynamic_field.dart';

class RegistrationScreen extends StatefulWidget {
  final AuthRepository repository;
  final VoidCallback onAuthenticated;

  const RegistrationScreen({
    super.key,
    required this.repository,
    required this.onAuthenticated,
  });

  @override
  State<RegistrationScreen> createState() => _RegistrationScreenState();
}

class _RegistrationScreenState extends State<RegistrationScreen> {
  AuthConfig? _config;
  List<UiStep> _steps = [];
  int _index = 0;
  String? _flowId;
  String? _error;
  bool _busy = false;
  final Map<String, TextEditingController> _controllers = {};

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final config = await widget.repository.config();
      if (!mounted) return;
      setState(() {
        _config = config;
        _steps = config.registrationSteps();
      });
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  TextEditingController _controller(String name) {
    return _controllers.putIfAbsent(name, TextEditingController.new);
  }

  Future<void> _submit() async {
    if (_config == null || _steps.isEmpty || _busy) return;
    setState(() {
      _busy = true;
      _error = null;
    });

    try {
      final step = _steps[_index];
      switch (step.stepCode) {
        case 'PHONE':
          final raw = _controller('phone').text.replaceAll(RegExp(r'\D'), '');
          final phone = raw.startsWith('998') ? '+$raw' : '+998$raw';
          final result = await widget.repository
              .startRegistration(phone, _config!.policyVersion);
          _flowId = result['flowId'] as String;
          break;
        case 'OTP':
          await widget.repository.verifyRegistrationOtp(
            _flowId!,
            _controller('otp').text.trim(),
          );
          break;
        case 'IDENTITY':
          await widget.repository.submitIdentity(
            _flowId!,
            _controller('pinfl').text.trim(),
            _controller('passport').text.trim(),
          );
          break;
        case 'PASSWORD':
          await widget.repository.submitPassword(
            _flowId!,
            _controller('password').text,
          );
          break;
        case 'PIN':
          await widget.repository.submitPin(
            _flowId!,
            _controller('pin').text.trim(),
          );
          widget.onAuthenticated();
          return;
        default:
          throw StateError('Unsupported registration step ${step.stepCode}');
      }

      if (mounted) setState(() => _index++);
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  void dispose() {
    for (final c in _controllers.values) {
      c.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_config == null && _error == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (_steps.isEmpty) {
      return Scaffold(body: Center(child: Text(_error ?? 'Config topilmadi')));
    }

    final step = _steps[_index];
    final fields = ((step.config['fields'] as List?) ?? const [])
        .cast<Map>()
        .map((e) => e.cast<String, dynamic>())
        .toList();

    return Scaffold(
      appBar: AppBar(title: const Text('Ro‘yxatdan o‘tish')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            '${_index + 1}/${_steps.length} · ${step.stepCode}',
            style: Theme.of(context).textTheme.labelLarge,
          ),
          const SizedBox(height: 16),
          ...fields.map((field) => DynamicField(
                config: field,
                controller: _controller(field['name'] as String),
              )),
          if (_error != null) ...[
            Text(_error!, style: const TextStyle(color: Colors.red)),
            const SizedBox(height: 12),
          ],
          FilledButton(
            onPressed: _busy ? null : _submit,
            child: _busy
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('Davom etish'),
          ),
        ],
      ),
    );
  }
}
