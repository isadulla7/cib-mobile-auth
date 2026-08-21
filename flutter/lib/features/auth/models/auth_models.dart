class TokenPair {
  final String accessToken;
  final String refreshToken;
  final int accessTokenExpiresIn;
  final int refreshTokenExpiresIn;
  final String sessionId;

  TokenPair({
    required this.accessToken,
    required this.refreshToken,
    required this.accessTokenExpiresIn,
    required this.refreshTokenExpiresIn,
    required this.sessionId,
  });

  factory TokenPair.fromJson(Map<String, dynamic> json) => TokenPair(
        accessToken: json['accessToken'] as String,
        refreshToken: json['refreshToken'] as String,
        accessTokenExpiresIn: (json['accessTokenExpiresIn'] as num).toInt(),
        refreshTokenExpiresIn: (json['refreshTokenExpiresIn'] as num).toInt(),
        sessionId: json['sessionId'] as String,
      );
}

class LoginResult {
  final String status;
  final TokenPair? tokens;
  final String? challengeId;
  final int expiresIn;

  LoginResult({
    required this.status,
    this.tokens,
    this.challengeId,
    required this.expiresIn,
  });

  factory LoginResult.fromJson(Map<String, dynamic> json) => LoginResult(
        status: json['status'] as String,
        tokens: json['tokens'] == null
            ? null
            : TokenPair.fromJson((json['tokens'] as Map).cast<String, dynamic>()),
        challengeId: json['challengeId'] as String?,
        expiresIn: (json['expiresIn'] as num?)?.toInt() ?? 0,
      );
}

class UiStep {
  final String flowCode;
  final String stepCode;
  final int order;
  final Map<String, dynamic> config;

  UiStep({
    required this.flowCode,
    required this.stepCode,
    required this.order,
    required this.config,
  });

  factory UiStep.fromJson(Map<String, dynamic> json) => UiStep(
        flowCode: json['flowCode'] as String,
        stepCode: json['stepCode'] as String,
        order: (json['order'] as num).toInt(),
        config: (json['config'] as Map).cast<String, dynamic>(),
      );
}

class AuthConfig {
  final int schemaVersion;
  final int policyVersion;
  final Map<String, dynamic> publicPolicy;
  final List<UiStep> ui;

  AuthConfig({
    required this.schemaVersion,
    required this.policyVersion,
    required this.publicPolicy,
    required this.ui,
  });

  factory AuthConfig.fromJson(Map<String, dynamic> json) => AuthConfig(
        schemaVersion: (json['schemaVersion'] as num).toInt(),
        policyVersion: (json['policyVersion'] as num).toInt(),
        publicPolicy: (json['publicPolicy'] as Map).cast<String, dynamic>(),
        ui: (json['ui'] as List)
            .map((e) => UiStep.fromJson((e as Map).cast<String, dynamic>()))
            .toList(),
      );

  List<UiStep> registrationSteps() {
    final result = ui.where((e) => e.flowCode == 'REGISTRATION').toList();
    result.sort((a, b) => a.order.compareTo(b.order));
    return result;
  }
}
