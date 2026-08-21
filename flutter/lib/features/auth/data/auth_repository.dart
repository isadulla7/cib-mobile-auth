import '../../../core/api/api_client.dart';
import '../../../core/security/token_store.dart';
import '../models/auth_models.dart';

class AuthRepository {
  final ApiClient api;
  final TokenStore tokens;

  AuthRepository(this.api, this.tokens);

  Future<AuthConfig> config() async {
    return AuthConfig.fromJson(await api.getJson('/api/v1/auth/config'));
  }

  Future<Map<String, dynamic>> startRegistration(
      String phone, int policyVersion) {
    return api.postJson('/api/v1/auth/registration/start', {
      'phone': phone,
      'policyVersion': policyVersion,
    });
  }

  Future<Map<String, dynamic>> verifyRegistrationOtp(
      String flowId, String otp) {
    return api.postJson('/api/v1/auth/registration/$flowId/otp/verify', {
      'otp': otp,
    });
  }

  Future<Map<String, dynamic>> submitIdentity(
      String flowId, String pinfl, String passport) {
    return api.postJson('/api/v1/auth/registration/$flowId/identity', {
      'pinfl': pinfl,
      'passport': passport,
    });
  }

  Future<Map<String, dynamic>> submitPassword(
      String flowId, String password) {
    return api.postJson('/api/v1/auth/registration/$flowId/password', {
      'password': password,
    });
  }

  Future<TokenPair> submitPin(String flowId, String pin) async {
    final pair = TokenPair.fromJson(
      await api.postJson('/api/v1/auth/registration/$flowId/pin', {'pin': pin}),
    );
    await _save(pair);
    return pair;
  }

  Future<LoginResult> login(String phone, String password) async {
    final result = LoginResult.fromJson(
      await api.postJson('/api/v1/auth/login', {
        'phone': phone,
        'password': password,
      }),
    );
    if (result.tokens != null) await _save(result.tokens!);
    return result;
  }

  Future<LoginResult> verifyLoginOtp(String challengeId, String otp) async {
    final result = LoginResult.fromJson(
      await api.postJson('/api/v1/auth/login/challenge/$challengeId/otp', {
        'otp': otp,
      }),
    );
    if (result.tokens != null) await _save(result.tokens!);
    return result;
  }

  Future<void> refresh() async {
    final refresh = await tokens.refreshToken();
    if (refresh == null) throw StateError('No refresh token');

    final pair = TokenPair.fromJson(
      await api.postJson('/api/v1/auth/token/refresh', {
        'refreshToken': refresh,
      }),
    );
    await _save(pair);
  }

  Future<void> logout() async {
    final refresh = await tokens.refreshToken();
    if (refresh == null) {
      await tokens.clear();
      return;
    }
    try {
      await api.postJson('/api/v1/auth/token/logout', {
        'refreshToken': refresh,
      });
    } finally {
      await tokens.clear();
    }
  }

  Future<void> _save(TokenPair pair) {
    return tokens.save(
      accessToken: pair.accessToken,
      refreshToken: pair.refreshToken,
    );
  }
}
