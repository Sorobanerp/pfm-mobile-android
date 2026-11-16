# `pfm-mobile-android`

> **Repositório #12 da Topologia Oficial Multi-Repo (14 Repositórios) — PFM Core Banking (`Sorobanerp`)**

- **Descrição**: PFM Core Banking — Offline-First Android App with QR Code NFC-e Scanner & Encrypted Sync Queue
- **Stack Homologada (ADR-001)**: `Kotlin, Jetpack Compose, Room/SQLCipher, CameraX ML Kit`
- **Arquitetura**: Hexagonal Architecture (Ports & Adapters) / Zero-Trust Multitenancy (ADR-006)
- **Documentação Executiva, C4, ADRs, PRD & SRE**: [https://sorobanerp.github.io/pfm-docs/](https://sorobanerp.github.io/pfm-docs/)
- **Repositório de Documentação**: [Sorobanerp/pfm-docs](https://github.com/Sorobanerp/pfm-docs)

---

## Estrutura de Diretórios Inicial

```text
pfm-mobile-android/
├── app/src/main/java/io/sorobanerp/pfm/mobile/domain/
├── app/src/main/java/io/sorobanerp/pfm/mobile/data/local/
├── app/src/main/java/io/sorobanerp/pfm/mobile/ui/scanner/
├── app/src/test/java/io/sorobanerp/pfm/mobile/
└── README.md
```

## Governança & Contratos (DoR / DoD V2)

Consulte as especificações completas no portal de documentação:
- [ADRs V2 (ADR-001 a ADR-007)](https://sorobanerp.github.io/pfm-docs/architecture/adrs/)
- [Modelo C4 & Topologia de 14 Repositórios](https://sorobanerp.github.io/pfm-docs/architecture/c4-model/)
- [PRD Core Banking & Regras RN01–RN06](https://sorobanerp.github.io/pfm-docs/product/prd-core-banking/)
- [Backlog Scrum & Cenários BDD Gherkin](https://sorobanerp.github.io/pfm-docs/product/backlog-bdd/)
