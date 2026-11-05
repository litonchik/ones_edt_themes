# Темы для 1С:EDT

Плагин цветовых палитр интерфейса и редактора BSL для **1С:EDT 2026.1.2, 2026.1.3 и 2026.2.2**.
Восемь светлых и 11 тёмных схем, личные цвета и настройка шрифта кода с предпросмотром.

Текущая версия — **[0.1.28](https://github.com/litonchik/ones_edt_themes/releases/tag/v0.1.28)**.

## Возможности

- Светлая и тёмная основы интерфейса, подсветка BSL и QL/DCS в конструкторе запроса.
- Переключатель **Тёмные / Светлые** фильтрует список палитр и меняет предпросмотр.
- Редактор 17 цветовых ролей: HEX, выбор цвета, образцы и сброс к значениям стиля.
- Отдельные личные варианты каждой схемы в текущей рабочей области.
- Выбор семейства и размера шрифта кода с предпросмотром.
- Применить / ОК / Отмена и восстановление исходных цветов и шрифта.
- Установка по URL сайта обновлений или из p2 ZIP с собственными bundle и feature.

## Палитры

Снимки сделаны в тестовой рабочей области EDT. Нажмите на миниатюру, чтобы открыть полный размер.

| Ayu Dark (Bordered) | Darcula | Dracula |
|---|---|---|
| [<img src="docs/screenshots/ayu-dark.png" alt="EDT в теме Ayu Dark (Bordered)" width="280">](docs/screenshots/ayu-dark.png) | [<img src="docs/testing/screenshots/darcula-workbench.png" alt="EDT в теме Darcula" width="280">](docs/testing/screenshots/darcula-workbench.png) | [<img src="docs/screenshots/dracula.png" alt="EDT в теме Dracula" width="280">](docs/screenshots/dracula.png) |
| **GitHub Dark Default** | **Monokai** | **Night Owl** |
| [<img src="docs/screenshots/github-dark-default.png" alt="EDT в теме GitHub Dark Default" width="280">](docs/screenshots/github-dark-default.png) | [<img src="docs/testing/screenshots/monokai-restarted-workbench.png" alt="EDT в теме Monokai" width="280">](docs/testing/screenshots/monokai-restarted-workbench.png) | [<img src="docs/screenshots/night-owl.png" alt="EDT в теме Night Owl" width="280">](docs/screenshots/night-owl.png) |
| **Noctis Minimus** | **One Dark Pro** | **Shades of Purple** |
| [<img src="docs/screenshots/noctis-minimus.png" alt="EDT в теме Noctis Minimus" width="280">](docs/screenshots/noctis-minimus.png) | [<img src="docs/screenshots/one-dark-pro.png" alt="EDT в теме One Dark Pro" width="280">](docs/screenshots/one-dark-pro.png) | [<img src="docs/screenshots/shades-of-purple.png" alt="EDT в теме Shades of Purple" width="280">](docs/screenshots/shades-of-purple.png) |
| **Visual Studio Dark (C/C++)** | **Winter is Coming (Dark Blue)** | |
| [<img src="docs/screenshots/visual-studio-dark-cpp.png" alt="EDT в теме Visual Studio Dark (C/C++)" width="280">](docs/screenshots/visual-studio-dark-cpp.png) | [<img src="docs/screenshots/winter-is-coming-dark-blue.png" alt="EDT в теме Winter is Coming (Dark Blue)" width="280">](docs/screenshots/winter-is-coming-dark-blue.png) | |

Светлые палитры:

| GitHub Light Default | Winter is Coming (Light) | Night Owl Light |
|---|---|---|
| [<img src="docs/screenshots/github-light-default.png" alt="EDT в теме GitHub Light Default" width="280">](docs/screenshots/github-light-default.png) | [<img src="docs/screenshots/winter-is-coming-light.png" alt="EDT в теме Winter is Coming (Light)" width="280">](docs/screenshots/winter-is-coming-light.png) | [<img src="docs/screenshots/night-owl-light.png" alt="EDT в теме Night Owl Light" width="280">](docs/screenshots/night-owl-light.png) |
| **Ayu Light (Bordered)** | **Noctis Lux** | **Visual Studio Light (C/C++)** |
| [<img src="docs/screenshots/ayu-light.png" alt="EDT в теме Ayu Light (Bordered)" width="280">](docs/screenshots/ayu-light.png) | [<img src="docs/screenshots/noctis-lux.png" alt="EDT в теме Noctis Lux" width="280">](docs/screenshots/noctis-lux.png) | [<img src="docs/screenshots/visual-studio-light-cpp.png" alt="EDT в теме Visual Studio Light (C/C++)" width="280">](docs/screenshots/visual-studio-light-cpp.png) |
| **Noctis Hibernus** | **Noctis Lilac** | |
| [<img src="docs/screenshots/noctis-hibernus.png" alt="EDT в теме Noctis Hibernus" width="280">](docs/screenshots/noctis-hibernus.png) | [<img src="docs/screenshots/noctis-lilac.png" alt="EDT в теме Noctis Lilac" width="280">](docs/screenshots/noctis-lilac.png) | |

Darcula и Dracula — разные схемы. Цвета адаптированы к ролям EDT;
это не полные копии исходных интерфейсов VS Code.

## Установка

Версия **0.1.28** доступна через сайт обновлений и в
[GitHub Releases](https://github.com/litonchik/ones_edt_themes/releases/tag/v0.1.28).
Установленный плагин обновляется через **Справка → Проверить обновления**.

После обновления повторно нажмите **Применить** у выбранной палитры,
чтобы обновить цвета Git-подписей, выделения строк и полей Eclipse Forms.

1. Откройте **Справка → Установить новое ПО… → Добавить…**.
2. Укажите имя **Темы EDT** и адрес сайта обновлений:

   ```text
   https://litonchik.github.io/ones_edt_themes/repository/
   ```

   Выберите компонент **Темы EDT — цветовые палитры**.
3. Завершите установку и перезапустите EDT.
4. Откройте **Окно → Настройки → Внешний вид → Темы EDT**,
   выберите палитру и нажмите **Применить** или **ОК**.

Сначала выберите группу **Тёмные** или **Светлые**, затем палитру.
Переключение группы и предпросмотр применяются к странице настроек; оформление
EDT меняется после **Применить / ОК**.

> **При переходе между светлой и тёмной темой перезапустите EDT после применения.**
> Это относится к обоим направлениям: светлая → тёмная и тёмная → светлая.
> До перезапуска часть панелей, выделение и открытые редакторы могут сохранять
> цвета предыдущей основы интерфейса.

Для следующих версий используйте **Справка → Проверить обновления**.

Для офлайн-установки скачайте `edt-themes-0.1.28.zip` и `.zip.sha256`
из [Releases](https://github.com/litonchik/ones_edt_themes/releases/latest),
затем выберите **Добавить… → Архив…**. ZIP из **Code → Download ZIP** содержит
исходники и не подходит для установки. Проверку SHA-256 описывает подробная инструкция.

[Подробная инструкция, обновление и удаление](docs/user-guide.md).

## Совместимость и ограничения

- Windows x64: EDT **2026.1.2+2 / 2026.1.3+25** с Java **17**; EDT **2026.2.2+21** с Java **25**.
- macOS x64: подтверждена работа на EDT 2026.1.3
- EDT 2025, неизвестные сборки и Linux не разрешены.
- Формы, макеты и другие исключённые содержательные области сохраняют штатное оформление.
- Настройки BSL на уровне проекта могут иметь приоритет над настройками рабочей области.
- После первого применения часть нативных контролов обновляется полностью после перезапуска; при переходе между светлой и тёмной основой перезапуск EDT необходим.
- После смены палитры переоткройте уже открытое сравнение модулей для обновления подсветки.
- В тестовой EDT воспроизводится известная CSS-ошибка заголовка Properties;
  полная ручная матрица с рабочим metadata-проектом остаётся открытой.

Для 0.1.16 выполнены **79 unit tests**; результаты проверки трёх EDT, QL/DCS и семантических цветов:
[отчёт и границы проверки](docs/testing/edt-2026-compatibility-validation.md).
Для кандидата 0.1.17 пройдены **85 unit tests и 2749 runtime-проверок**.
[GitHub Light Default и проверка переключения режимов 0.1.17](docs/testing/github-light-validation.md).
[Winter is Coming (Light), кандидат 0.1.18](docs/testing/winter-light-validation.md).
Для 0.1.19: **93 unit tests, 3285 runtime PASS**.
[Night Owl Light и исправление фона текстовых полей, кандидат 0.1.19](docs/testing/night-owl-light-validation.md).
Для 0.1.20: **97 unit tests, 1539 runtime PASS**.
[Ayu Light (Bordered), кандидат 0.1.20](docs/testing/ayu-light-validation.md).
Для 0.1.21: **101 unit tests, 1551 runtime PASS**.
[Noctis Lux, кандидат 0.1.21](docs/testing/noctis-lux-validation.md).
Для 0.1.22: **101 unit tests, 4581 runtime PASS**.
[Исправление светлых панелей и проверка 0.1.22](docs/testing/light-ui-validation.md).
Для 0.1.23: **101 unit tests, 4512 runtime PASS**.
[Фон нумерации и проверка 0.1.23](docs/testing/editor-ruler-validation.md).
Для 0.1.24: **105 unit tests, 1683 runtime PASS**.
[Visual Studio Light (C/C++), кандидат 0.1.24](docs/testing/visual-studio-light-validation.md).
Для 0.1.25: **109 unit tests, 1695 runtime PASS**.
[Noctis Hibernus, кандидат 0.1.25](docs/testing/noctis-hibernus-validation.md).
Для 0.1.26: **113 unit tests, 1707 runtime PASS**.
[Noctis Lilac, кандидат 0.1.26](docs/testing/noctis-lilac-validation.md).
Для 0.1.27: **116 unit tests, 4701 runtime PASS**.
[Цвета Git-подписей навигатора](docs/testing/git-labels-validation.md).
Для 0.1.28: **116 unit tests, 8140 runtime PASS**.
[Выделение строк и поля Eclipse Forms](docs/testing/selection-validation.md).
[Проверка публикации и установки по URL](docs/testing/update-site-validation.md).

## Разработка

Unit tests модели можно запустить без установленной EDT, с JDK 17 и Maven:

```shell
mvn --batch-mode --no-transfer-progress -f tests/unit/pom.xml clean test
```

Полная сборка p2 требует установленной целевой EDT и проверенного Maven **3.9.11**:

```powershell
& ./scripts/build.ps1 -Action Verify `
    -MavenHome 'C:\apps\apache-maven-3.9.11' `
    -JavaHome 'C:\Program Files\Axiom\AxiomJDK-Pro-17-Full' `
    -EdtHome 'C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64'
```

Укажите свои пути. Результат — `dist/edt-themes-0.1.28.zip` и SHA-256.
`dist`, `target`, `.work` и `.cache` исключены из Git.
GitHub Actions запускает unit tests модели и проверки упаковки сайта обновлений;
он не заменяет проверку EDT. Сайт обновлений публикуется из проверенного ZIP
последнего стабильного релиза, без повторной сборки плагина в CI.

[Участие в разработке](CONTRIBUTING.md) · [Документация](docs/README.md) ·
[История изменений](CHANGELOG.md) · [Подготовка релиза](docs/releasing.md)

## Лицензия

Собственный код проекта — [MIT](LICENSE).
Палитры сохраняют лицензии и авторство своих источников:
[атрибуция](THIRD_PARTY_NOTICES.md). Eclipse, EDT и другие зависимости платформы
не входят в поставку и сохраняют собственные условия использования.

Проект независимый; не является продуктом или официальным расширением фирмы «1С».
