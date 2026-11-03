# Публикация 0.1.16 и установка по URL

Проверено 03.11.2026. Код плагина не изменялся; опубликован ранее проверенный
bundle `0.1.16.202610292155`. Отдельная сборка плагина не выполнялась.

## Публичная поставка

- [Релиз v0.1.16](https://github.com/litonchik/ones_edt_themes/releases/tag/v0.1.16).
- [Сайт обновлений](https://litonchik.github.io/ones_edt_themes/repository/).
- SHA-256 ZIP: `36531d6f4bd329594153a1ded8f321e583a49e4975d033f3085ca26f89db489f`.
- SHA-256 bundle: `0faa1fc675b397a71926c72d5bcc946dd43f8406cbf88973909749edef19eedd`.

GitHub подтвердил SHA-256 обоих вложений при загрузке. ZIP повторно скачан
по публичной ссылке без авторизации; его SHA-256 совпал. Исходники bundle,
feature и target сверены с подготовленной копией проверенной 0.1.16.

Шесть локальных тестов упаковки прошли: сохранение байтов p2-файлов,
отклонение повреждённого архива, выхода за каталог, стороннего bundle,
неверных artifact metadata и перезаписи непустого выходного каталога.
Те же тесты и Java unit tests прошли в GitHub Actions.

[Publish update site](https://github.com/litonchik/ones_edt_themes/actions/runs/41606051910)
прошёл со второй попытки. Первый deploy был заблокирован стандартным правилом
окружения `github-pages`, разрешавшим только `main`. Добавлено разрешение тегов
`v*`; содержимое релиза и workflow между попытками не менялось.

Семь файлов по HTTPS дали HTTP 200 и совпали побайтово с содержимым ZIP:
`p2.index`, `content.jar`, `artifacts.jar`, обе `.xml.xz`, feature и bundle.
[Контрольные суммы HTTP](2026-11-03-update-site/http-verification.txt).

## Проверка в EDT

Windows x64, EDT **2026.1.3+25**, Axiom Java **17.0.18**, Eclipse 4.30.
Использована прежняя изолированная копия EDT из исходного каталога проекта;
на время проверки новая `.work/edt-test` ссылалась на неё через junction.
Новая workspace, test-home и отчёты созданы внутри новой копии проекта.
Установленная пользовательская EDT и рабочие workspace не менялись.

1. `scripts/p2-test.ps1 -Action Install -RepositoryUrl <URL>` — exit 0.
   Использован настоящий p2 director с удалённым HTTPS-репозиторием, без
   подстановки локального ZIP. SHA-256 установленного bundle совпал с релизом.
2. Workbench `Surfaces` — **312 PASS, 0 FAIL, exit 0**:
   страница плагина, обычный текст, QL/QL DCS, два embedded-контрола конструктора
   во всех 11 палитрах, штатный BSL merge viewer. Проверено точное восстановление
   115 исходных настроек. [Отчёт](2026-11-03-update-site/surfaces.txt).
3. Просмотрены снимки [QL](2026-11-03-update-site/query-constructor.png) и
   [сравнения](2026-11-03-update-site/edt-merge.png). Красные annotations QL
   связаны с synthetic metadata-ссылкой `Справочник.Тест`.

В минимальной headless-конфигурации p2 зарегистрирована диагностика активации
`org.eclipse.core.net`: `Bundle has been uninstalled: org.eclipse.core.net.nl_ru`.
Она не привела к отказу установки. В полном workbench сохраняется ранее
описанная CSS-диагностика Properties и сообщения изолированного окружения.
Проверка не объявляет эти сообщения исправленными.

## Очистка

Отдельные Cleanup / Reconcile / p2 Uninstall / Audit — **exit 0**.
Cleanup: **14 PASS**, active profile и активный журнал очищены.
Тестовый fragment и bundle тем больше не зарегистрированы.
Все **1267 пар ID/версия базовых компонентов** совпали с inventory до установки.
Временный junction новой копии удалён; исходная тестовая EDT сохранена.

[Cleanup](2026-11-03-update-site/cleanup.txt) ·
[Установка](2026-11-03-update-site/install.log) ·
[Удаление](2026-11-03-update-site/uninstall.log) ·
[Аудит p2](2026-11-03-update-site/audit.log) ·
[Итоговый inventory](2026-11-03-update-site/final-audit.txt).

## Границы

Установка подтверждена через p2 director и последующий реальный workbench;
ручной проход всех страниц GUI-мастера установки не выполнялся.
Переход с 0.1.16 на следующую опубликованную версию пока проверить нельзя.
URL-установка на других EDT и macOS отдельно не проверялась; прежняя матрица
трёх EDT относится к тому же bundle, установленному из ZIP.
Полный мастер запросов с metadata-проектом остаётся за рамками.

Публикация использует последний стабильный релиз; отдельные версии доступны
в GitHub Releases. Topics `1c`, `1c-enterprise`, `1c-edt`, `edt-plugin`,
`eclipse-plugin`, `themes` добавлены. Появление в OpenYellow не подтверждалось.
