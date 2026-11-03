# Подготовка репозитория к GitHub

Дата: 18.10.2026. Основа — проверенная версия 0.1.15.
Пользователь подтвердил её работу («работает»), без сведений об ОС/сценариях.
Согласованы завершение очереди на 11 палитрах, перенос сборок под gitignore
и MIT для собственного кода. Monokai Pro (CE) остаётся отложенной.

## Изменения

- README ориентирован на установку и возможности, добавлен существующий снимок тестовой EDT.
- Добавлены CHANGELOG, CONTRIBUTING, индекс документации и инструкция Releases.
- Добавлены корневой MIT, обзор third-party notices, MIT в ресурсах JAR и p2 feature.
  Существующие MIT notices палитр сохранены; преобразование их окончаний строк отключено.
- `dist/` снят с отслеживания, 32 локальных ZIP/файла SHA-256 остались на диске.
  `.work`, `.cache`, `target` и локальные служебные файлы игнорируются.
- Добавлены GitHub Issue/PR templates и workflow unit tests модели (Java 17).
- Product Java, все 11 палитр, snapshot/personal keys и шрифт не изменялись.

## Проверки

`scripts/build.ps1 -Action Verify`: **BUILD SUCCESS**, **77 tests**,
0 failures/errors/skipped. JDK 17.0.18, Maven 3.9.11, Tycho 4.0.13,
локальный target EDT 2026.1.3+25 (Eclipse 4.30.0).
ZIP содержит только собственные bundle/feature, 31 classes major 61,
11 палитр, 10 notices источников и отдельный MIT проекта.
Байты MIT в JAR совпали с корневым LICENSE; полный текст MIT в feature.xml совпал.

Для проверки упаковки локально пересобран ещё не опубликованный 0.1.15:
bundle/feature **0.1.15.202610181940**, ZIP **107 909 bytes**, SHA-256:
`1ffdbecc9c7f5a7acb3742893645fb6efa51fd7b1dc7a9dffe55ab361b221fc4`.
Исходный ZIP палитры и его SHA-256 сохранены под
`.work/github-preparation-original-0.1.15.zip[.sha256]`.
Полный локальный вывод новой сборки — `.work/repository-preparation-verify.log`.

Проверены **87 локальных Markdown/image ссылок** в десяти актуальных документах:
нет отсутствующих целей и ссылок на игнорируемые dist-файлы.
Workflow и YAML front matter обоих Issue templates разобраны установленным
SnakeYAML 1.27 через JDK 17; проверены события push/PR/manual и contents:read.
GitHub Actions удалённо пока не запускался; это остаётся проверкой после первого push.

`git check-ignore` подтвердил dist, .work, .cache и target;
`git ls-files dist` пуст. Все 32 локальных файла dist присутствуют.
Проверка содержимого и истории по шаблонам GitHub PAT, AWS access key ID
и private-key headers совпадений не выявила. Это ограниченная проверка по шаблонам,
а не гарантия отсутствия любых чувствительных данных.

## Границы

Runtime-циклы и p2 install/uninstall повторно не выполнялись:
изменились документация и лицензионная упаковка, поведение не менялось.
364 runtime assertions относятся к исходной сборке 0.1.15 и описаны в
[отчёте палитры](visual-studio-dark-validation.md), вместе с CSS-диагностикой
Properties и открытой ручной матрицей. Новый qualifier не объявляется отдельно
проверенным в runtime или на macOS.

Старые ZIP остаются в истории Git; она не переписывалась.
В metadata коммитов указан рабочий email автора, в исторических отчётах есть
локальные пути. Это нужно учесть при выборе истории для первого публичного push.
GitHub remote не задан, репозиторий/релиз удалённо не создавались.
Порядок последующей публикации — [releasing.md](../releasing.md).
