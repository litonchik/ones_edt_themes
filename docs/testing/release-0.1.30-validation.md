# Публикация 0.1.30 и установка через GitHub Pages

Проверено 09.11.2026. Пользователь разрешил стабильный релиз и проверку сайта.

## Релиз

- [GitHub Release v0.1.30](https://github.com/litonchik/ones_edt_themes/releases/tag/v0.1.30).
- [Сайт обновлений](https://litonchik.github.io/ones_edt_themes/repository/).
- Исходный коммит и тег: `4e431cff6e8816f64e0ce538d4b62eb892590f51`.
- Bundle: `0.1.30.202611091114`.
- ZIP SHA-256: `b4d7738513fc72967d9c926a38305bbdea05bdda61f33c6a53b6e3c34aaae1fe`.

Опубликован уже проверенный ZIP без пересборки. По сравнению с исходным
проверенным кодом 50d26b4 релизный коммит меняет только документацию.
Режим локального примера отсутствует; индикатор пассивный, без popup,
перехвата фокуса и автоустановки. Внешние действия выполняются по нажатию.
Все 19 палитр, личные варианты, шрифт и наборы значков сохранены.

137 unit tests, 6 проверок упаковки и 90 адресных runtime PASS на трёх
Windows EDT выполнены до публикации. Предыдущая матрица индикатора — 309 PASS.
Эти матрицы при проверке доставки повторно не запускались.
[Матрица и границы](update-production-validation.md).

## GitHub Actions и HTTPS

[Unit tests](https://github.com/litonchik/ones_edt_themes/actions/runs/42611716447)
и [Publish update site](https://github.com/litonchik/ones_edt_themes/actions/runs/42611717040)
— success. CI проверил модель Java 17 и упаковку сайта; плагин не пересобирался.
[Результаты workflows](2026-11-09-release-0.1.30/workflows.json).

GitHub подтвердил размеры и SHA-256 двух вложений. ZIP и SHA-файл повторно
скачаны публично без авторизации и побайтово совпали с локальными.
По каноническим HTTPS URL все семь p2-файлов дали HTTP 200 и совпали с ZIP:
p2.index, content.jar, artifacts.jar, обе .xml.xz, feature и bundle.
release.json и обе HTML-страницы сообщают 0.1.30. Предыдущий ZIP 0.1.29
также скачан публично и сохранил исходный SHA-256.
[HTTP-проверка](2026-11-09-release-0.1.30/http-verification.json),
[метаданные релиза](2026-11-09-release-0.1.30/release.json).

## Фактическое обновление через URL

Windows x64, EDT 2026.1.3+25, Axiom Java 17.0.18. Отдельная тестовая копия
и изолированный workspace; рабочая EDT и проекты пользователя не менялись.

1. В чистую копию установлен предыдущий bundle `0.1.29.202611051520`.
2. p2 director выполнил remove/install того же feature из
   `https://litonchik.github.io/ones_edt_themes/repository/`.
3. Установился `0.1.30.202611091114`; новый локальный ZIP при обновлении
   не подставлялся, использовался только HTTPS-репозиторий.
4. SHA-256 установленного bundle совпал с релизным ZIP:
   `35097cf92b933e5088e294c924ab4ee69acb0891361fa038b34f8a4e086e5224`.
5. Запущена EDT с test-only fragment; Cleanup — 25 PASS,
   включая пустой журнал, отсутствие профиля, остановку монитора и удаление updates keys.

[Сводка обновления](2026-11-09-release-0.1.30/url-update.json).
GUI-мастер «Проверить обновления» при проверке доставки отдельно не проходился.
Новые платформы и macOS для этого релиза отдельно не проверялись.

## Очистка

Cleanup → Reconcile → p2 Uninstall → Audit — PASS. Исходные 1267 entries
bundles.info совпали полностью; theme/probe bundles и файлы отсутствуют.
Временный junction удалён с сохранением цели. Монитор остановлен,
тестовые updates preferences удалены. Подробные .log остаются локально,
публичные сводки не содержат локальных путей и учётных данных.
[Сводка](2026-11-09-release-0.1.30/cleanup.json),
[результат Cleanup](2026-11-09-release-0.1.30/cleanup.txt).
