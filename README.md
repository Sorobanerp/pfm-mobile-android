# PFM Mobile Android — Kotlin Offline-First (Jetpack Compose)

[![Gitflow](https://img.shields.io/badge/Gitflow-develop%20%7C%20main-indigo)](CONTRIBUTING.md)
[![SemVer](https://img.shields.io/badge/SemVer-2.0.0-blue)](CHANGELOG.md)
[![Docs](https://img.shields.io/badge/PFM%20Docs-V3.0-8CBA51)](https://sorobanerp.github.io/pfm-docs/)

---

## 1. Visão Geral & Papel na Arquitetura

Aplicativo Android nativo **Offline-First** em **Kotlin + Jetpack Compose + Room/SQLCipher + CameraX**: leitura instantânea de QR Code NFC-e sem internet, fila local criptografada e sincronização resiliente via WorkManager.

- **Repositório:** `Sorobanerp/pfm-mobile-android`
- **Vínculo com `pfm-docs`:** `ADR-001, ADR-003, ADR-004 | US-07, US-14`

```mermaid
flowchart LR
    CAM["CameraX + ML Kit QR NFC-e"] --> ROOM["Room + SQLCipher (Offline-First)"]
    ROOM -->|WorkManager Sync| BFF["Envoy / pfm-ingestion-bff-go"]
```

---

## 2. Pré-requisitos & Variáveis de Ambiente

### Pré-requisitos
- JDK `17` / `21` e Android SDK (API 34+)
- Gradle Wrapper (`./gradlew`)

### Variáveis de Ambiente (Sem Segredos Hardcoded)
| Variável / BuildConfig | Padrão Local | Descrição |
| :--- | :---: | :--- |
| `API_BASE_URL` | `http://10.0.2.2:18080/` | URL do Edge Gateway acessível pelo Emulador Android |

> **Segurança (Zero-Trust):** Nunca commite tokens, senhas, chaves AWS ou strings de conexão com credenciais reais. Utilize `.env` local (ignorado no `.gitignore`) ou AWS Secrets Manager / GitHub OIDC.

---

## 3. Como Executar Localmente

```bash
./gradlew assembleDebug
# Instalar no emulador conectado:
./gradlew installDebug
```

---

## 4. Como Testar e Validar

```bash
./gradlew testDebugUnitTest jacocoTestReport
./gradlew lintDebug
```

---

## 5. Como Provisionar / Validar na Nuvem

1. Fluxo Gitflow: `feat/*` $\rightarrow$ `develop` $\rightarrow$ `release/vX.Y.Z` $\rightarrow$ `main`.
2. O pipeline gera APK/AAB assinado e valida cobertura de testes $\ge 95\%$.

Consulte [CONTRIBUTING.md](CONTRIBUTING.md) para regras de Conventional Commits e [CHANGELOG.md](CHANGELOG.md) para histórico de versões.
