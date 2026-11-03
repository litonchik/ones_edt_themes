# Скриншоты палитр для README

Дата: 18.10.2026. По замечанию пользователя заменены девять неоднородных
снимков: образец параметров One Dark Pro, отдельные редакторы и окна с белыми полями.
Darcula и Monokai оставлены референсами; SHA-256 обоих файлов не изменился.

## Съёмка

Использована установленная поставка 0.1.15 в отдельной `.work/edt-test`,
workspace `.work/workspace-probe`, EDT 2026.1.3+25 / Eclipse 4.30.0 / Java 17.0.18,
Windows x64. Рабочая EDT и пользовательские проекты не затрагивались.

Добавлены test-only фазы `GalleryPrepare` и `Gallery` и перспектива Gallery:
навигатор слева, BSL по центру, Properties справа. Первый процесс сохраняет
Dark, второй снимает девять стандартных палитр после перезапуска, без personal overrides.
Кадры имеют размер **884×581** и находятся в [docs/screenshots](../screenshots).
Все девять кадров просмотрены визуально; README ссылается на новые файлы.

Причина старых белых полей: размер Shell включал рамку/заголовок, тогда как
GC использовал клиентские координаты. Новый gallery capture использует
`Shell.getClientArea()` и захватывает только клиентскую область тестового окна.
Внешний рабочий стол не захватывается; сохранённые PNG не редактировались.

Проверены одинаковые размеры кадров, видимость всей клиентской области,
отсутствие белых полос снизу/справа, реальные BSL background и glyph
keyword/number/string каждой палитры. Галерея не содержит личных вариантов цветов.
Нативные категории препроцессора сохраняют штатное поведение EDT.

## Результат

`build-workbench-probe.ps1` — javac Java 17, exit 0.
Итоговые запуски: **GalleryPrepare 13 PASS / Gallery 191 PASS**, 0 FAIL,
оба процесса exit 0. [Подготовка](readme-gallery/runtime-galleryprepare.txt),
[съёмка и restore](readme-gallery/runtime-gallery.txt).
Gallery вернула все original keys, очистила active profile и snapshot journal.
Отдельная Cleanup не потребовалась.

P2 Install / Reconcile / Uninstall / Audit — exit 0. После удаления тестовых
bundle/fragment **1267 базовых ID/версий** совпали с рабочей EDT.
[Install](readme-gallery/gallery-p2-install.log),
[Uninstall](readme-gallery/gallery-p2-uninstall.log),
[Audit](readme-gallery/gallery-p2-audit.log),
[bundles](readme-gallery/gallery-bundles-audit.log).

Это проверка съёмки и восстановления тестовых настроек. Полные функциональные
циклы старых схем и unit tests повторно не запускались: product Java и палитры
не изменены. Известная CSS-диагностика Properties и ручная матрица из
[отчёта 0.1.15](visual-studio-dark-validation.md) не объявляются исправленными.

## Воспроизведение

В отдельной тестовой копии установите текущий ZIP через `p2-test.ps1 -Action Install`,
затем выполните:

```powershell
& ./scripts/build-workbench-probe.ps1
& ./scripts/run-workbench-probe.ps1 -Phase GalleryPrepare
& ./scripts/run-workbench-probe.ps1 -Phase Gallery
```

Файлы появляются в `.work/probe-images/gallery-<id>.png`.
При прерывании используйте отдельную фазу `Cleanup` для восстановления настроек.
После успешной съёмки выполните `Reconcile`, p2 `Uninstall` и `Audit`.
