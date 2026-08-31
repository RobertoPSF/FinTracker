# Contribuindo

## Adicionar um banco

1. Descobrir o package do app do banco (`adb shell pm list packages | grep -i nome`).
2. Adicionar entrada em `app/src/main/assets/parser_rules.json`:

```json
{ "package": "com.exemplo.banco", "name": "Banco Exemplo", "ignore": ["fatura"] }
```

3. Adicionar keywords/regex específicas só se as genéricas falharem.
4. Adicionar teste em `app/src/test/.../TransactionClassifierTest.kt` com a string real da notificação, **anonimizada** (trocar nomes de pessoas e valores).
5. `./gradlew :app:testDebugUnitTest`.

## Regras

- Nunca commitar notificações com dados pessoais reais.
- Parser continua sem dependência de Android (mantém testabilidade).
- Toda mudança de schema Room exige nova versão + `Migration` em `AppDatabase.MIGRATIONS`.
