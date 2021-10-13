# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.0]
### Added
- Add GitHub actions
### Changed
- Upgrade to Spring Boot 2.4.10
- Upgrade Gradle to 7.2

## [0.2.1] - 2021-01-14
### Fixed
- Removed @Component annotation in edu.kit.datamanager.dao.ByExampleSpecification to allow using service-base without database

## [0.2.0] - 2020-12-15
### Changed
- Renaming of RabbitMQ configuration property repo.messaging.exchange to repo.messaging.sender.exchange

## [0.1.3] - 2020-11-25
### Changed
- Truncating service-assigned times to milliseconds for compatibility reasons

### Fixed
- Fix of wrong HATEOS links in pagination

## [0.1.2] - 2020-09-28
### Changed
- Minor changes in messaging classes

## [0.1.1] - 2020-08-18
### Fixed
- Fix of wrong HATEOS links in pagination

## [0.1] - 2020-06-30
### Added
- First public version

### Changed
- none

### Removed
- none

### Deprecated
- none

### Fixed
- none

### Security
- none

[Unreleased]: https://github.com/kit-data-manager/service-base/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/kit-data-manager/service-base/compare/v0.2.1...v0.3.0
[0.2.1]: https://github.com/kit-data-manager/service-base/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/kit-data-manager/service-base/compare/v0.1.3...v0.2.0
[0.1.3]: https://github.com/kit-data-manager/service-base/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/kit-data-manager/service-base/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/kit-data-manager/service-base/compare/v0.1.0...v0.1.1
[0.1]: https://github.com/kit-data-manager/service-base/releases/tag/v0.1