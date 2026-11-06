# Публикация 0.1.29 и установка через GitHub Pages

Проверено 06.11.2026. Пользователь подтвердил локальный результат и разрешил
push, релиз и публикацию сайта обновлений.

## Релиз

- [GitHub Release v0.1.29](https://github.com/litonchik/ones_edt_themes/releases/tag/v0.1.29).
- [Сайт обновлений](https://litonchik.github.io/ones_edt_themes/repository/).
- Исходный коммит и тег: `f1605c8c9e5dd6e1ec1d63861494f81965e87634`.
- Bundle: `0.1.29.202611051520`.
- ZIP SHA-256: `00bcd3c0a15db6d50285717a6d12b7c9c6ad067b9f387538111054f0043788c0`.

Опубликован уже проверенный ZIP без пересборки. Наборы значков имеют названия
Visual Studio Light / Dark, источник и благодарность указаны в README,
MIT notices сохранены. Флаг «Значки Visual Studio в Навигаторе» по умолчанию
выключен; включение — страница «Темы EDT» → Применить.

Предыдущая проверка механизма: 121 unit tests, 8526 runtime PASS на трёх
Windows EDT. После переименования изменился только ThemePreferencePage.class;
51 прочий class и все ресурсы побайтово прежние. Пользователь подтвердил
результат без дополнительных сведений об ОС/сценариях.
[Матрица и границы](chroma-icons-validation.md).

## GitHub Actions и HTTPS

Локально прошли шесть тестов упаковки p2-сайта. Workflow
[Unit tests](https://github.com/litonchik/ones_edt_themes/actions/runs/42192112997)
и [Publish update site](https://github.com/litonchik/ones_edt_themes/actions/runs/42192113960)
завершились success. CI проверил модель Java 17 и подготовку сайта,
плагин в CI не пересобирался.
[Результаты](2026-11-06-release-0.1.29/workflows.json).

GitHub подтвердил размеры и SHA-256 обоих вложений. ZIP и SHA-файл повторно
скачаны по публичным URL без авторизации и побайтово совпали с локальными.
Семь p2-файлов по каноническим HTTPS URL дали HTTP 200 и совпали с ZIP:
p2.index, content.jar, artifacts.jar, обе .xml.xz, feature и bundle.
release.json и обе HTML-страницы сайта показывают 0.1.29.
[HTTP-проверка](2026-11-06-release-0.1.29/http-verification.json),
[метаданные релиза](2026-11-06-release-0.1.29/release.json).

## Фактическое обновление через URL

Windows x64, EDT 2026.1.3+25, Axiom Java 17.0.18. Использована отдельная
тестовая копия и изолированный workspace; рабочая EDT и проекты пользователя
не менялись.

1. В чистую копию установлен предыдущий bundle `0.1.28.202611051018`.
2. p2 director выполнил remove/install того же feature из
   `https://litonchik.github.io/ones_edt_themes/repository/`.
3. Установился bundle `0.1.29.202611051520`; новый локальный ZIP при установке
   не подставлялся, использовался только HTTPS-репозиторий.
4. SHA-256 установленного bundle совпал с проверенным ZIP:
   `3714fef7666c241cccdf90d66bd06ef46feff1748dcc88fb0904825286bc1692`.
5. После обновления запущена реальная EDT с test-only fragment; Cleanup
   завершилась с 24 PASS, включая отключение значков,
   пустой журнал и отсутствие активного профиля.

[Сводка обновления](2026-11-06-release-0.1.29/url-update.json).
GUI-мастер «Проверить обновления» отдельно не проходился. Полная runtime-
матрица значков при проверке доставки не повторялась. Новые платформы
и macOS для этого релиза отдельно не проверялись.

## Очистка

Cleanup → Reconcile → p2 Uninstall → Audit — PASS. Исходные 1267 entries
bundles.info совпали полностью, theme/probe bundles и файлы отсутствуют.
Временный junction удалён с сохранением цели; workspace расположен только
в локальном .work. Подробные .log не публикуются, сводки не содержат
локальных путей и учётных данных.
[Сводка](2026-11-06-release-0.1.29/cleanup.json),
[результат Cleanup](2026-11-06-release-0.1.29/cleanup.txt).
