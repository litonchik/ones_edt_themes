# Документация

## Для пользователя

- [Установка, настройка, восстановление и удаление](user-guide.md).
- [История версий](../CHANGELOG.md).
- [Совместимость и ограничения 0.1.16](testing/edt-2026-compatibility-validation.md).
- [Первый публичный релиз 0.1.16](releases/0.1.16.md).
- [GitHub Light Default и переключение светлых/тёмных тем 0.1.17](testing/github-light-validation.md).

## Для разработчика

- [Участие в разработке и сборка](../CONTRIBUTING.md).
- [Среда разработки: факты и история исследования](development-environment.md).
- [Подготовка GitHub Releases и сайта обновлений](releasing.md).
- [Проверка подготовки репозитория](testing/repository-preparation.md).
- [Съёмка галереи палитр для README](testing/readme-gallery-validation.md).
- [Публикация релиза и проверка установки по URL](testing/update-site-validation.md).
- [Настройки EDT и native targets](reference/edt-2026.1-settings.md).
- [Редактор 17 ролей](reference/palette-editor.md).
- [Шрифт кода](reference/code-font.md).
- [Источники и разложение палитр](research/2026-10-14-theme-catalog.md).
- [Лицензии сторонних источников](../THIRD_PARTY_NOTICES.md).

## Проверки

Актуальная проверка трёх EDT 2026, вызовов BSL, QL/DCS и сравнения/объединения:
[0.1.16](testing/edt-2026-compatibility-validation.md).

[Журнал](testing/progress.md) содержит последовательность реализации и фактические результаты.
Каждая версия имеет отдельный отчёт в `testing/*-validation.md`; отобранные
логи и снимки находятся в `testing/<version>/`. Это исторические свидетельства
конкретного запуска, а не обещание проверки любой конфигурации.
Абсолютные пути в старых отчётах относятся к локальной тестовой среде автора.

## Архив

`research` содержит исследования с датами и первичными источниками,
`handoff` — исторические передачи контекста. `superpowers/plans` сохраняет
ранее согласованные планы; Superpowers отключён для проекта 14.10.2026.
Для актуального состояния используйте README, CHANGELOG и последний отчёт проверки.

- [Winter is Coming (Light): источники](research/2026-11-04-winter-light.md) и [проверки 0.1.18](testing/winter-light-validation.md).

- [Night Owl Light: источники](research/2026-11-04-night-owl-light.md) и [проверки 0.1.19](testing/night-owl-light-validation.md).

- [Ayu Light (Bordered): источники](research/2026-11-04-ayu-light.md) и [проверки 0.1.20](testing/ayu-light-validation.md).

- [Noctis Lux: источники](research/2026-11-04-noctis-lux.md) и [проверки 0.1.21](testing/noctis-lux-validation.md).

- [Светлые панели: механизм](reference/light-ui.md) и [проверки 0.1.22](testing/light-ui-validation.md).

- [Фон нумерации: механизм](reference/editor-ruler.md) и [проверки 0.1.23](testing/editor-ruler-validation.md).

- [Visual Studio Light (C/C++): источники](research/2026-11-04-visual-studio-light.md) и [проверки 0.1.24](testing/visual-studio-light-validation.md).

- [Noctis Hibernus: источники](research/2026-11-04-noctis-hibernus.md) и [проверки 0.1.25](testing/noctis-hibernus-validation.md).

- [Noctis Lilac: источники](research/2026-11-04-noctis-lilac.md) и [проверки 0.1.26](testing/noctis-lilac-validation.md).

- [Git-подписи навигатора](reference/git-labels.md) и [проверка 0.1.27](testing/git-labels-validation.md).

- [Выделение и Eclipse Forms](reference/selection-and-forms.md).
- [Проверки исправления 0.1.28](testing/selection-validation.md).
