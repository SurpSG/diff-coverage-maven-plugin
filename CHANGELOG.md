# Diff-Coverage Maven plugin

## 1.1.0

- Added Java 25 class files support

### Dependencies updates
- JaCoCo 0.8.14
- delta-coverage-core 3.6.0

## 1.0.0

- Changed group id from `com.github.surpsg` to `io.github.surpsg`
- Min supported Java version is 17
- Migrated to [Delta Coverage engine](https://github.com/gw-kit/delta-coverage-plugin)
  - Changed reports output paths:
    - HTML `target/site/delta-coverage/aggregated/html`
    - XML `target/site/delta-coverage/aggregated/report.xml`
  - Added Markdown report available in `target/site/delta-coverage/aggregated/report.md`
  - CSV reports is not generated anymore

## 0.3.3

### Fixes
- Update JaCoCo to be able to use Java 21

### Dependencies updates
- JaCoCo 0.8.10
- Updated maven dependency to 3.9.4
- Updated maven wrapper to 3.9.4
- Updated maven plugin tools to 3.9.0


## 0.3.2

- Support Maven v3.9


## 0.3.1

### Fixes
- Fixed build failure when include/exclude is used and subproject doesn't have classes directory #28


## 0.3.0

### Added
- The plugin supports exclude/include classes

### Dependencies updates
- jacoco-filtering-extension 0.9.5
- JaCoCo 0.8.8
- JGit 6.2.0.202206071550-r
