# Changelog — `pfm-mobile-android`

Todas as alterações notáveis neste repositório são documentadas neste arquivo seguindo o padrão [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e versionamento semântico [SemVer 2.0.0](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

### Added
- Tema Material 3 **Organic Financial Pulse** (`Color.kt` + `Theme.kt`) com paridade de tokens Web & Android (`#8CBA51`, `#DEFF8B`, `#FBCEB5`, `#FF5D6C`, `#F7F9F5`).
- Modelos e tela Compose offline-first para **Alerta de Inflação Pessoal por EAN (`US-11`)** e **Gestão de Perfil Familiar RBAC (`US-13`)** com testes unitários (`FamilyProfileAndInflationTest.kt`).
- Padronização de governança DevEx (`README.md`, `CHANGELOG.md`, `CONTRIBUTING.md`) e workflow de validação de **Conventional Commits** em Pull Requests para `develop` e `main`.

---

## [0.2.0] - 2026-11-20

### Added
- Leitor QR Code NFC-e offline-first com Room + SQLCipher, sincronização WorkManager e propagação W3C `traceparent`.

### Changed
- Alinhamento com a arquitetura **PFM Core Banking V3.0 (Production-Ready FinOps & Multi-Cloud)**.

### Security
- Aplicação estrita de políticas Zero-Trust (OIDC sem chaves estáticas, Trivy/Semgrep/Gitleaks gates e isolamento por tenant).

### Infrastructure
- Pipeline Gradle CI com JaCoCo e SonarCloud Quality Gate.

---

## [0.1.0] - 2026-11-15

### Added
- Estrutura inicial do repositório `pfm-mobile-android`, pipelines GitHub Actions e integração ao ecossistema Gitflow `Sorobanerp`.
