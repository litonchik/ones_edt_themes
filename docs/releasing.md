# Подготовка публикации и релиза

## Состав репозитория

В Git хранятся исходники, Maven/Tycho-конфигурация, ресурсы, лицензии,
документация и отобранные свидетельства проверок. `dist/`, `target/`,
`.work/` и `.cache/` игнорируются. Снятие `dist` с отслеживания не удаляет
локальные ZIP и не удаляет их из старых коммитов.

Перед первым push проверьте автора/email в локальной истории и содержимое
исторических отчётов: в них есть абсолютные пути тестовой среды.
Публичный репозиторий: <https://github.com/litonchik/ones_edt_themes>.
Его история начата с подготовленной копии 0.1.16; прежняя история сохранена
локально в исходном каталоге `ones_edt_theme_plugin`.

## Сборка

1. Используйте JDK 17, Maven 3.9.11 и точную EDT 2026.1.3+25.
2. Выполните `scripts/build.ps1 -Action Verify` со своими путями, как в README.
3. Проверьте ZIP через `scripts/verify-repository.ps1 -Zip <путь>`.
   Он должен содержать только собственные bundle/feature и лицензии.
4. Для изменений поведения выполните соразмерный runtime-цикл в тестовой EDT,
   включая restart/restore и Cleanup при незавершённом сценарии.
5. Сохраните отчёт с версиями платформы, результатами, размером ZIP и SHA-256.

Повторная сборка получает новый qualifier и SHA-256. Исторические отчёты
`docs/testing/<version>` описывают конкретные прежние сборки, а не любой ZIP
с тем же коротким номером версии. После изменения только документации или
лицензионной упаковки не объявляйте повторно пройденными runtime-проверки.

## GitHub Release

1. Зафиксируйте проверенные исходники и отметьте нужный commit тегом версии
   при публикации. Для уже опубликованной версии изменения выпускайте новым номером.
2. В Releases создайте релиз с описанием изменений, целевой EDT и ограничений.
3. Прикрепите **p2 ZIP** `dist/edt-themes-<version>.zip` и соответствующий
   `dist/edt-themes-<version>.zip.sha256`. Автоматические Source code ZIP/tar.gz
   не являются установочными p2-архивами.
4. Сопоставьте SHA-256 загруженного ZIP с локальной контрольной суммой.
5. Проверьте CI unit tests и тесты упаковки сайта после push. CI не содержит установленной EDT
   и не подтверждает совместимость/визуальное поведение плагина.

При первой публикации 0.1.16 используется уже проверенный ZIP с bundle
`0.1.16.202610292155`, SHA-256
`36531d6f4bd329594153a1ded8f321e583a49e4975d033f3085ca26f89db489f`.
Изменение документации и workflow не требует пересборки этого bundle.

## Сайт обновлений

Адрес: <https://litonchik.github.io/ones_edt_themes/repository/>.

В Settings → Pages источником публикации выбран **GitHub Actions**.
Workflow `Publish update site` запускается после публикации стабильного релиза
либо вручную через Actions → Run workflow. Он всегда выбирает последний
стабильный релиз GitHub, поэтому повторный запуск старого события не выбирает
архив непосредственно из этого события. Draft и prerelease не публикуются.

Workflow скачивает `edt-themes-<version>.zip` и `.zip.sha256`, проверяет SHA-256,
имена/версии собственных bundle и feature, p2 metadata и безопасность путей ZIP.
Плагин в CI не пересобирается. Содержимое p2 переносится без изменения байтов;
`release.json` на сайте фиксирует исходную версию и контрольную сумму ZIP.
Прошлые релизы остаются в Releases; стабильный сайт содержит текущий релиз.

Локальная подготовка сайта без публикации (Python 3, только стандартная библиотека):

```powershell
python scripts/prepare-update-site.py --zip dist/edt-themes-0.1.16.zip `
    --checksum dist/edt-themes-0.1.16.zip.sha256 --version 0.1.16 `
    --output .work/site-preview
python -m unittest discover -s tests/publication -v
```

Выходной каталог должен быть пустым. Скрипт не удаляет прежнюю публикацию.
Публикация выполняется отдельным deploy job с `pages:write` и `id-token:write`;
загрузка и подготовка имеют только `contents:read`.

После публикации проверьте HTTP-доступность `p2.index`, `content.jar`,
`artifacts.jar`, feature и bundle, затем установку через p2 в отдельную копию EDT:

```powershell
./scripts/p2-test.ps1 -Action Install `
    -RepositoryUrl https://litonchik.github.io/ones_edt_themes/repository/
```

Используйте только тестовую копию внутри `.work`. После проверки выполните
отдельную фазу Cleanup, если применяли настройки, затем Uninstall и Audit.
Базовый inventory EDT до и после должен совпасть. Добавление сайта в EDT
сохраняет возможность штатной проверки обновлений; фактический переход на
следующую версию проверяется, когда появляется следующий релиз.

## Источники

Проверены 18.10.2026:
[actions/checkout](https://github.com/actions/checkout),
[actions/setup-java](https://github.com/actions/setup-java),
[текст MIT](https://choosealicense.com/licenses/mit/).
Workflow использует checkout v7 и setup-java v6 с Java 17 и правом contents:read.

Проверены 03.11.2026:
[GitHub Pages через Actions](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages),
[API GitHub Releases](https://docs.github.com/en/rest/releases/releases),
[API GitHub Pages](https://docs.github.com/en/rest/pages/pages),
[добавление software site в Eclipse](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.user/tasks/tasks-127.htm).
