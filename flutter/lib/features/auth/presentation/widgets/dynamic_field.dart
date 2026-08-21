import 'package:flutter/material.dart';

class DynamicField extends StatelessWidget {
  final Map<String, dynamic> config;
  final TextEditingController controller;

  const DynamicField({
    super.key,
    required this.config,
    required this.controller,
  });

  @override
  Widget build(BuildContext context) {
    final type = config['type']?.toString() ?? 'text';
    final label = _text(config['label']);
    final obscure = type == 'password' || type == 'pin';
    final keyboard = switch (type) {
      'phone' || 'otp' || 'pinfl' || 'pin' => TextInputType.number,
      _ => TextInputType.text,
    };

    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: TextField(
        controller: controller,
        obscureText: obscure,
        keyboardType: keyboard,
        maxLength: (config['maxLength'] as num?)?.toInt(),
        decoration: InputDecoration(
          labelText: label,
          prefixText: type == 'phone' ? '+998 ' : null,
          border: const OutlineInputBorder(),
          counterText: '',
        ),
      ),
    );
  }

  String _text(dynamic value) {
    if (value is Map) {
      return value['uz']?.toString() ??
          value['en']?.toString() ??
          value.values.first.toString();
    }
    return value?.toString() ?? '';
  }
}
